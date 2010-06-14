/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

package jsr166y.forkjoin;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import sun.misc.Unsafe;
import java.lang.reflect.*;

/**
 * Abstract base class for tasks that run within a ForkJoinPool.  A
 * ForkJoinTask is a thread-like entity that is much lighter weight
 * than a normal thread.  Huge numbers of tasks and subtasks may be
 * hosted by a small number of actual threads in a ForkJoinPool,
 * at the price of some usage limitations.
 *
 * <p> The <tt>ForkJoinTask</tt> class is not directly subclassable
 * outside of this package. Instead, you can subclass one of the
 * supplied abstract classes that support various styles of fork/join
 * processing.  Normally, a concrete ForkJoinTask subclass declares
 * fields comprising its parameters, established in a constructor, and
 * then defines a <tt>compute</tt> method that somehow uses the
 * control methods supplied by this base class. While these methods
 * have <tt>public</tt> access, most may only be called from within
 * other ForkJoinTasks. Attempts to invoke them in other contexts
 * result in exceptions or errors including ClassCastException.  The
 * only generally accessible methods are those for cancellation and
 * status checking. The only way to invoke a "main" driver task is to
 * submit it to a ForkJoinPool. Normally, once started, this will in
 * turn start other subtasks.  Nearly all of these base support
 * methods are <tt>final</tt> because their implementations are
 * intrinsically tied to the underlying lightweight task scheduling
 * framework, and so cannot be overridden.
 *
 * <p> ForkJoinTasks play similar roles as <tt>Futures</tt> but
 * support a more limited range of use.  The "lightness" of
 * ForkJoinTasks is due to a set of restrictions (that are only
 * partially statically enforceable) reflecting their intended use as
 * purely computational tasks -- calculating pure functions or
 * operating on purely isolated objects.  The only coordination
 * mechanisms supported for ForkJoinTasks are <tt>fork</tt>, that
 * arranges asynchronous execution, and <tt>join</tt>, that doesn't
 * proceed until the task's result has been computed. (A simple form
 * of cancellation is also supported).  The computation defined in the
 * <tt>compute</tt> method should not in general perform any other
 * form of blocking synchronization, should not perform IO, and should
 * be independent of other tasks. Minor breaches of these
 * restrictions, for example using shared output streams, may be
 * tolerable in practice, but frequent use will result in poor
 * performance, and the potential to indefinitely stall if the number
 * of threads not waiting for external synchronization becomes
 * exhausted. This usage restriction is in part enforced by not
 * permitting checked exceptions such as IOExceptions to be
 * thrown. However, computations may still encounter unchecked
 * exceptions, that are rethrown to callers attempting join
 * them. These exceptions may additionally include
 * RejectedExecutionExceptions stemming from internal resource
 * exhaustion such as failure to allocate internal task queues.
 *
 * <p>ForkJoinTasks should perform relatively small amounts of
 * computations, othewise splitting into smaller tasks. As a very
 * rough rule of thumb, a task should perform more than 100 and less
 * than 10000 basic computational steps. If tasks are too big, then
 * parellelism cannot improve throughput. If too small, then memory
 * and internal task maintenance overhead may overwhelm processing.
 * The {@link ForkJoinWorkerThread} class supports a number of
 * inspection and tuning methods that can be useful when developing
 * fork/join programs.
 *
 * <p>ForkJoinTasks are <tt>Serializable</tt>, which enables them to
 * be used in extensions such as remote execution frameworks. However,
 * it is in general safe to serialize tasks only before or after, but
 * not during execution. Serialization is not relied on during
 * execution itself.
 */
public abstract class ForkJoinTask<V> implements Serializable {
    /*
     * The main implementations of execution methods are provided by
     * ForkJoinWorkerThread, so internals are package protected.  This
     * class is mainly responsible for maintaining task status field
     * and exception mechanics.
     */

    /**
     * Table of exceptions thrown by tasks, to enable reporting by
     * callers. Because exceptions are rare, we don't directly keep
     * them with task objects, but instead us a weak ref table.  Note
     * that cancellation exceptions don't appear in the table, but are
     * instead recorded as status values.
     *
     * Todo: Use ConcurrentReferenceMap
     */
    static final WeakHashMap<ForkJoinTask<?>, Throwable> exceptionTable =
        new WeakHashMap<ForkJoinTask<?>, Throwable>();

    static synchronized void setException(ForkJoinTask<?> t, Throwable ex) {
        exceptionTable.put(t, ex);
    }

    static synchronized Throwable getException(ForkJoinTask<?> t) {
        return exceptionTable.get(t);
    }

    static synchronized void clearException(ForkJoinTask<?> t) {
        exceptionTable.remove(t);
    }

    /**
     * Disallow direct construction outside this package.
     */
    ForkJoinTask() {
    }

    /**
     * Status, taking values:
     *   sero:     initial
     *   negative: COMPLETED. CANCELLED, or HAS_EXCEPTION
     *   positive: ignored wrt completion, may be used by subclasses
     *
     * Status is set negative when a task completes.  For normal
     * completion, the status field is set with a cheaper ordered
     * write (as opposed to volatile write) because ownership
     * maintenance in Worker queues ensures that it is never subject
     * to read-after-write anomalies. (Implementing this requires
     * direct use of Unsafe to avoid overhead.)
     */
    volatile int status;
    static final int COMPLETED     = -1; // order matters
    static final int CANCELLED     = -2;
    static final int HAS_EXCEPTION = -3;

    // within-package utilities

    /**
     * Immediately executes this task unless already complete,
     * trapping all possible exceptions.  Returns true if executed and
     * completed normally, else false.  This method cannot be
     * implemented outside this package because we must guarantee that
     * implementations trap all exceptions.
     * @return true if executed and completed normally, else false
     */
    abstract boolean exec();

    /**
     * Sets status to indicate this task is done.
     */
    final void setDone() {
        _unsafe.putOrderedInt(this, statusOffset, COMPLETED);
    }

    final boolean casStatus(int cmp, int val) {
        return cmp == status &&
            _unsafe.compareAndSwapInt(this, statusOffset, cmp, val);
    }

    final void setStatus(int s) {
        _unsafe.putOrderedInt(this, statusOffset, s);
    }

    final void incrementStatus() {
        for (;;) {
            int s = status; // force fail if already negative
            if (s < 0 || _unsafe.compareAndSwapInt(this, statusOffset, s, s+1))
                break;
        }
    }

    /**
     * Sets status to indicate exceptional completion. Note that we
     * allow races across normal and exceptional completion.
     */
    final void setDoneExceptionally(Throwable rex) {
        setException(this, rex);
        status = HAS_EXCEPTION;
    }

    /**
     * Sets status to cancelled only if in initial state. Uses same
     * implementation as cancel() but used internally to safely set
     * status on pool shutdown etc even if cancel is overridden.
     */
    final void setCancelled() {
        _unsafe.compareAndSwapInt(this, statusOffset, 0, CANCELLED);
    }

    /**
     * Workaround for not being able to rethrow unchecked exceptions.
     */
    static final void rethrowException(Throwable ex) {
        if (ex != null)
            _unsafe.throwException(ex);
    }

    /**
     * Version of setDoneExceptionally that screens argument
     */
    final void checkedSetDoneExceptionally(Throwable rex) {
        if (!(rex instanceof RuntimeException) && !(rex instanceof Error))
            throw new IllegalArgumentException(rex);
        setDoneExceptionally(rex);
    }

    /**
     * Returns result or throws exception.
     * Only call when isDone known to be true.
     */
    final V reportAsForkJoinResult() {
        int s = status;
        if (s == CANCELLED)
            throw new CancellationException();
        if (s == HAS_EXCEPTION)
            rethrowException(getException(this));
        return rawResult();
    }

    /**
     * Returns result or throws exception using j.u.c.Future conventions
     * Only call when isDone known to be true.
     */
    final V reportAsFutureResult() throws ExecutionException {
        Throwable ex;
        int s = status;
        if (s == CANCELLED)
            throw new CancellationException();
        if (s == HAS_EXCEPTION && (ex = getException(this)) != null)
            throw new ExecutionException(ex);
        return rawResult();
    }

    // public methods

    /**
     * Arranges to asynchronously execute this task, which will later
     * be directly or indirectly joined by the caller of this method.
     * While it is not necessarily enforced, it is a usage error to
     * fork a task more than once unless it has completed and been
     * reinitialized.  This method may be invoked only from within
     * other ForkJoinTask computations. Attempts to invoke in other
     * contexts result in exceptions or errors including
     * ClassCastException.
     */
    public final void fork() {
        ((ForkJoinWorkerThread)(Thread.currentThread())).pushTask(this);
    }

    /**
     * Returns the result of the computation when it is ready.
     * Monitoring note: Callers of this method need not block, but may
     * instead assist in performing computations that may directly or
     * indirectly cause the result to be ready.
     * This method may be invoked only from within other ForkJoinTask
     * computations. Attempts to invoke in other contexts result
     * in exceptions or errors including ClassCastException.
     *
     * @return the computed result
     * @throws Throwable (a RuntimeException, Error, or unchecked
     * exception) if the underlying computation did so.
     */
    public final V join() {
        int s = status;
        if (s >= 0) {
            ((ForkJoinWorkerThread)(Thread.currentThread())).helpJoinTask(this);
            s = status;
        }
        return s < COMPLETED? reportAsForkJoinResult() : rawResult();
    }

    /**
     * Equivalent in effect to the sequence <tt>fork(); join();</tt>
     * but likely to be more efficient.
     * @throws Throwable (a RuntimeException, Error, or unchecked
     * exception) if the underlying computation did so.
     * @return the computed result
     */
    public V forkJoin() {
        exec();
        return join();
    }

    /**
     * Returns true if the computation performed by this task has
     * completed (or has been cancelled).
     * @return true if this computation has completed
     */
    public final boolean isDone() {
        return status < 0;
    }

    /**
     * Returns true if this task was cancelled.
     * @return true if this task was cancelled
     */
    public final boolean isCancelled() {
        return status == CANCELLED;
    }

    /**
     * Returns true if task currently has COMPLETED status. This
     * method is not public because this fact may asynchronously
     * change, which we can handle internally but not externally.
     */
    final boolean completedNormally() {
        return status == COMPLETED;
    }

    /**
     * Returns true if task threw and exception or was cancelled
     */
    final boolean completedAbnormally() {
        return status < COMPLETED;
    }

    /**
     * Asserts that the results of this task's computation will not be
     * used. If a cancellation occurs before this task is processed,
     * then its <tt>compute</tt> method will not be executed,
     * <tt>isCancelled</tt> will report true, and <tt>join</tt> will
     * result in a CancellationException being thrown. Otherwise,
     * there are no guarantees about whether <tt>isCancelled</tt> will
     * report true, whether <tt>join</tt> will return normally or via
     * an exception, or whether these behaviors will remain consistent
     * upon repeated invocation. This method may be overridden in
     * subclasses, but if so, must still ensure that these minimal
     * properties hold.
     *
     * <p> This method is designed to be invoked by <em>other</em>
     * tasks. To abruptly terminate the current task, you should just
     * return from its computation method, or <tt>throw new
     * CancellationException()</tt>, or in AsyncActions, invoke
     * <tt>finishExceptionally()</tt>.
     */
    public void cancel() {
        // Using 0 here succeeds if task never touched, and maybe otherwise
        _unsafe.compareAndSwapInt(this, statusOffset, 0, CANCELLED);
    }

    /**
     * Returns the exception thrown by method <tt>compute</tt>, or a
     * CancellationException if cancelled, or null if none or if the
     * method has not yet completed.
     * @return the exception, or null if none
     */
    public final Throwable getException() {
        int s = status;
        if (s >= COMPLETED)
            return null;
        if (s == CANCELLED)
            return new CancellationException();
        return getException(this);
    }

    /**
     * Returns the result that would be returned by <tt>join</tt>, or
     * null if this task is not known to have been completed.  This
     * method is designed to aid debugging, as well as to support
     * extensions. Its use in any other context is strongly
     * discouraged.
     * @return the result, or null if not completed.
     */
    public abstract V rawResult();

    /**
     * Resets the internal bookkeeping state of this task, allowing a
     * subsequent <tt>fork</tt>. This method allows repeated reuse of
     * this task, but only if reuse occurs when this task has either
     * never been forked, or has been forked, then completed and all
     * outstanding joins of this task have also completed. Effects
     * under any other usage conditions are not guaranteed, and are
     * almost surely wrong. This method may be useful when executing
     * pre-constructed trees of subtasks in loops.
     */
    public void reinitialize() {
        if (status == HAS_EXCEPTION)
            clearException(this);
        status = 0;
    }

    /**
     * Joins this task, without returning its result or throwing an
     * exception. This method may be useful when processing
     * collections of tasks when some have been cancelled or otherwise
     * known to have aborted. This method may be invoked only from
     * within other ForkJoinTask computations. Attempts to invoke in
     * other contexts result in exceptions or errors including
     * ClassCastException.
     */
    public final void quietlyJoin() {
        ((ForkJoinWorkerThread)(Thread.currentThread())).helpJoinTask(this);
    }

    // Serialization support

    private static final long serialVersionUID = -7721805057305804111L;

    /**
     * Save the state to a stream.
     *
     * @serialData the current run status and the exception thrown
     * during execution, or null if none.
     * @param s the stream
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        s.defaultWriteObject();
        s.writeObject(getException(this));
    }

    /**
     * Reconstitute the instance from a stream.
     * @param s the stream
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        Object ex = s.readObject();
        if (ex != null)
            setException(this, (Throwable)ex);
    }

    // Temporary Unsafe mechanics for preliminary release

    static final Unsafe _unsafe;
    static final long statusOffset;

    static {
        try {
            if (ForkJoinWorkerThread.class.getClassLoader() != null) {
                Field f = Unsafe.class.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                _unsafe = (Unsafe)f.get(null);
            }
            else
                _unsafe = Unsafe.getUnsafe();
            statusOffset = _unsafe.objectFieldOffset
                (ForkJoinTask.class.getDeclaredField("status"));
        } catch (Exception ex) { throw new Error(ex); }
    }

}
