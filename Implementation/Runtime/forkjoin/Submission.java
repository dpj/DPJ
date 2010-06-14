/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

package jsr166y.forkjoin;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

/**
 * Adapter class to allow tasks submitted to a pool to act as Futures.
 * Methods are implemented in the same way as in the RecursiveTask
 * class, but with extra bookkeeping and signalling to cover three
 * kinds of adaptation:
 *
 * (1) Unlike internal fork/join processing, get() must block if the
 * caller is a normal thread (not FJ worker thread). We use a simpler
 * variant of the mechanics used in FutureTask, but bypass them and
 * use helping joins if the caller is itself a ForkJoinWorkerThread.
 *
 * (2) Regular Futures encase RuntimeExceptions within
 * ExecutionExceptions, while internal tasks just throw them directly,
 * so these must be trapped and wrapped.
 *
 * (3) External submissions are tracked for the sake of managing
 * worker threads. The pool submissionStarting and submissionCompleted
 * methods perform the associated bookkeeping. This requires some care
 * with cancellation and early termination -- the completion signal
 * can be issued only if a start signal ever was.
 *
 */
final class Submission<V> extends ForkJoinTask<V> implements Future<V> {

    // Status values for sync. We need to keep track of RUNNING status
    // just to make sure callbacks to pool are balanced.
    static final int INITIAL = 0;
    static final int RUNNING = 1;
    static final int DONE    = 2;

    /**
     * Stripped-down variant of FutureTask.sync
     */
    static final class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 4982264981922014374L;

        public int tryAcquireShared(int acquires) {
            return getState() == DONE? 1 : -1;
        }

        public boolean tryReleaseShared(int releases) { return true; }
        public void reset() { setState(INITIAL); }
        public boolean isDone() { return getState() == DONE; }

        public boolean transitionToRunning() {
            return compareAndSetState(INITIAL, RUNNING);
        }

        /** Set status to DONE, release waiters, and return old state */
        public int transitionToDone() {
            for (;;) {
                int c = getState();
                if (c == DONE || compareAndSetState(c, DONE)) {
                    releaseShared(0);
                    return c;
                }
            }
        }
    }

    private final ForkJoinTask<V> task;
    private final ForkJoinPool pool;
    private final Sync sync;
    private V result;

    Submission(ForkJoinTask<V> t, ForkJoinPool p) {
        task = t;
        pool = p;
        sync = new Sync();
    }

    /**
     * Run the inner tssk.
     */
    private void runTask() {
        try {
            if (sync.transitionToRunning()) {
                pool.submissionStarting();
                if (status >= 0) {
                    result = task.forkJoin();
                    setDone();
                }
            } 
        } catch(Throwable rex) {
            setDoneExceptionally(rex);
        } finally {
            if (sync.transitionToDone() == RUNNING)
                pool.submissionCompleted();
        }
    }

    public V forkJoin() {
        runTask();
        return reportAsForkJoinResult();
    }

    final boolean exec() {
        runTask();
        return completedNormally();
    }

    public V rawResult() {
        return result;
    }

    /**
     * ForkJoinTask version of cancel
     */
    public void cancel() {
        try {
            if (!sync.isDone()) {
                setCancelled(); // avoid recursive call to cancel
                task.cancel();
            }
        } finally {
            // Claim completion even if async cancel
            if (sync.transitionToDone() == RUNNING)
                pool.submissionCompleted();
        }
    }

    /**
     * Future version of cancel
     */
    public boolean cancel(boolean ignore) {
        this.cancel();
        return isCancelled();
    }

    public V get() throws InterruptedException, ExecutionException {
        // If caller is FJ worker, help instead of block, but fall
        // through.to acquire, to preserve Submission sync guarantees
        Thread t = Thread.currentThread();
        if (t instanceof ForkJoinWorkerThread)
            quietlyJoin();
        sync.acquireSharedInterruptibly(1);
        return reportAsFutureResult();
    }

    public V get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
        long nanos = unit.toNanos(timeout);
        Thread t = Thread.currentThread();
        if (t instanceof ForkJoinWorkerThread) {
            if(!((ForkJoinWorkerThread)t).doTimedJoinTask(this, nanos))
                throw new TimeoutException();
            //  Preserve Submission sync guarantees
            sync.acquireSharedInterruptibly(1);
        }
        else if (!sync.tryAcquireSharedNanos(1, nanos))
            throw new TimeoutException();
        return reportAsFutureResult();
    }

    /**
     * Interrupt-less get for ForkJoinPool.invoke
     */
    public V awaitInvoke() {
        Thread t = Thread.currentThread();
        if (t instanceof ForkJoinWorkerThread)
            quietlyJoin();
        sync.acquireShared(1);
        return reportAsForkJoinResult();
    }

    public void reinitialize() { // Required, but of dubious value.
        result = null;
        sync.reset();
        super.reinitialize();
    }

    /**
     * Within-package utility to access underlying task
     */
    ForkJoinTask<V> getSubmittedTask() {
        return task;
    }

    /**
     * Externally set completion
     */
    void finishTask(V value) {
        if (sync.transitionToRunning())
            pool.submissionStarting();
        result = value;
        setDone();
        if (sync.transitionToDone() == RUNNING)
            pool.submissionCompleted();
    }

    /**
     * Externally set exceptional completion
     */
    void finishTaskExceptionally(Throwable rex) {
        if (sync.transitionToRunning())
            pool.submissionStarting();
        setDoneExceptionally(rex);
        if (sync.transitionToDone() == RUNNING)
            pool.submissionCompleted();
    }

}


