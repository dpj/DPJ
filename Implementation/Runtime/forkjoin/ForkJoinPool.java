/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

package jsr166y.forkjoin;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.atomic.*;
import sun.misc.Unsafe;
import java.lang.reflect.*;


/**
 * Host for a group of ForkJoinWorkerThreads that perform
 * ForkJoinTasks. A ForkJoinPool also provides the entry point for
 * tasks submitted from non-ForkJoinTasks, as well as management and
 * monitoring operations.  Normally a single ForkJoinPool is used for
 * a large number of submitted tasks. Otherwise, use would not always
 * outweigh the construction overhead of creating a large set of
 * threads and the associated startup bookkeeping.
 *
 * <p> Class ForkJoinPool does not implement the ExecutorService
 * interface because it only executes ForkJoinTasks, not arbitrary
 * Runnables. However, for the sake of uniformity, it supports
 * analogous lifecycle control methods such as shutdown.
 *
 * <p>A ForkJoinPool may be constructed with any number of worker
 * threads, and worker threads may be added and removed dynamically.
 * However, as a general rule, using a pool size of the number of
 * processors on a given system (as arranged by the default
 * constructor) will result in the best performance. Resizing may be
 * expensive and may cause transient imbalances and slowdowns.
 *
 * <p>In addition to execution and lifecycle control methods, this
 * class provides status check methods (for example
 * <tt>getStealCount</tt>) that are intended to aid in developing,
 * tuning, and monitoring fork/join applications.
 */
public class ForkJoinPool implements ForkJoinExecutor {

    /*
     * This is an overhauled version of the framework described in "A
     * Java Fork/Join Framework" by Doug Lea, in, Proceedings, ACM
     * JavaGrande Conference, June 2000
     * (http://gee.cs.oswego.edu/dl/papers/fj.pdf). It retains most of
     * the basic structure, but includes a number of algorithmic
     * improvements, along with integration with other
     * java.util.concurrent components.
     */

    /**
     * Factory for creating new ForkJoinWorkerThreads.  A
     * ForkJoinWorkerThreadFactory must be defined and used for
     * ForkJoinWorkerThread subclasses that extend base functionality
     * or initialize threads with different contexts.
     */
    public static interface ForkJoinWorkerThreadFactory {
        /**
         * Returns a new worker thread operating in the given pool.
         *
         * @param pool the pool this thread works in
         * @throws NullPointerException if pool is null;
         */
        public ForkJoinWorkerThread newThread(ForkJoinPool pool);
    }

    /**
     * The default ForkJoinWorkerThreadFactory, used unless overridden
     * in ForkJoinPool constructors.
     */
    public static class  DefaultForkJoinWorkerThreadFactory
        implements ForkJoinWorkerThreadFactory {
        public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            return new ForkJoinWorkerThread(pool);
        }
    }

    private static final DefaultForkJoinWorkerThreadFactory
        defaultForkJoinWorkerThreadFactory =
        new DefaultForkJoinWorkerThreadFactory();


    /**
     * Permission required for callers of methods that may start or
     * kill threads.
     */
    private static final RuntimePermission modifyThreadPermission =
        new RuntimePermission("modifyThread");

    /**
     * If there is a security manager, makes sure caller has
     * permission to modify threads.
     */
    private static void checkPermission() {
        SecurityManager security = System.getSecurityManager();
        if (security != null)
            security.checkPermission(modifyThreadPermission);
    }

    /**
     * Generator for assigning sequence numbers as thread names.
     */
    private static final AtomicInteger poolNumberGenerator =
        new AtomicInteger();

    /**
     * Array holding all worker threads in the pool. Array size must
     * be a power of two. Acts similarly to a CopyOnWriteArrayList --
     * updates are protected by workerLock.  But it additionally
     * allows in-place nulling out or replacements of slots upon
     * termination.  All uses of this array should first assign as
     * local, and must screen out nulls. Note: ForkJoinWorkerThreads
     * directly access this array.
     */
    volatile ForkJoinWorkerThread[] workers;

   /**
     * Pool wide synchronization control. Workers are enabled to look
     * for work when the barrier's count is incremented. If they fail
     * to find some, they may wait for next count. Synchronization
     * events occur only in enough contexts to maintain overall
     * liveness:
     *
     *   - Submission of a new task
     *   - Termination of pool or worker
     *
     * So, signals and waits occur relatively rarely during normal
     * processing, which minimizes contention on this global
     * synchronizer. Even so, the PoolBarrier is designed to minimize
     * blockages by threads that have better things to do.
     */
    private final PoolBarrier poolBarrier;

    /**
     * Lock protecting access to workers.
     */
    private final ReentrantLock workerLock;

    /**
     * Condition for awaitTermination.
     */
    private final Condition termination;

    /**
     * Lifecycle control.
     */
    private final RunState runState;

    /**
     * The number of submissions that are running in pool.
     */
    private final AtomicInteger runningSubmissions;

    /**
     * The uncaught exception handler used when any worker
     * abrupty terminates
     */
    private Thread.UncaughtExceptionHandler ueh;

    /**
     * Creation factory for worker threads.
     */
    private final ForkJoinWorkerThreadFactory factory;

    /**
     * Head and tail of embedded submission queue. (The queue is
     * embedded to avoid hostile memory placements.)
     */
    private volatile SQNode sqHead;
    private volatile SQNode sqTail;

    /**
     * Number of workers that are (probably) executing tasks.
     * Atomically incremented when a worker gets a task to run, and
     * decremented when worker has no tasks and cannot find any.  This
     * is updated only via CAS. It is inlined here rather than a
     * stand-alone field to minimize memory thrashing when it is
     * heavily contended during ramp-up/down of tasks.
     */
    private volatile int activeCount;

    /**
     * The current targetted pool size. Updated only under worker lock
     * but volatile to allow concurrent reads.
     */
    private volatile int poolSize;

    /**
     * The number of workers that have started but not yet terminated
     * Accessed only under workerLock.
     */
    private int runningWorkers;

    /**
     * Pool number, just for assigning useful names to worker threads
     */
    private final int poolNumber;

    /**
     * Return a good size for worker array given pool size.
     * Currently requires size to be a power of two.
     */
    private static int workerSizeFor(int ps) {
        return ps <= 1? 1 : (1 << (32 - Integer.numberOfLeadingZeros(ps-1)));
    }

    /**
     * Create new worker using factory.
     * @param index the index to assign worker
     */
    private ForkJoinWorkerThread createWorker(int index) {
        ForkJoinWorkerThread w = factory.newThread(this);
        w.setDaemon(true);
        w.setWorkerPoolIndex(index);
        w.setName("ForkJoinPool-" + poolNumber + "-worker-" + index);
        Thread.UncaughtExceptionHandler h = ueh;
        if (h != null)
            w.setUncaughtExceptionHandler(h);
        return w;
    }

    /**
     * Creates a ForkJoinPool with a pool size equal to the number of
     * processors available on the system and using the default
     * ForkJoinWorkerThreadFactory,
     * @throws SecurityException if a security manager exists and
     *         the caller is not permitted to modify threads
     *         because it does not hold {@link
     *         java.lang.RuntimePermission}<tt>("modifyThread")</tt>,
     */
    public ForkJoinPool() {
        this(Runtime.getRuntime().availableProcessors(),
             defaultForkJoinWorkerThreadFactory);
    }

    /**
     * Creates a ForkJoinPool with the indicated number of Worker
     * threads, and using the default ForkJoinWorkerThreadFactory,
     * @param poolSize the number of worker threads
     * @throws IllegalArgumentException if poolSize less than or
     * equal to zero
     * @throws SecurityException if a security manager exists and
     *         the caller is not permitted to modify threads
     *         because it does not hold {@link
     *         java.lang.RuntimePermission}<tt>("modifyThread")</tt>,
     */
    public ForkJoinPool(int poolSize) {
        this(poolSize, defaultForkJoinWorkerThreadFactory);
    }

    /**
     * Creates a ForkJoinPool with a pool size equal to the number of
     * processors available on the system and using the given
     * ForkJoinWorkerThreadFactory,
     * @param factory the factory for creating new threads
     * @throws NullPointerException if factory is null
     * @throws SecurityException if a security manager exists and
     *         the caller is not permitted to modify threads
     *         because it does not hold {@link
     *         java.lang.RuntimePermission}<tt>("modifyThread")</tt>,
     */
    public ForkJoinPool(ForkJoinWorkerThreadFactory factory) {
        this(Runtime.getRuntime().availableProcessors(), factory);
    }

    /**
     * Creates a ForkJoinPool with the indicated number of worker
     * threads and the given factory.
     *
     * <p> You can also add and remove threads while the pool is
     * running. But it is generally more efficient and leads to more
     * predictable performance to initialize the pool with a
     * sufficient number of threads to support the desired concurrency
     * level and leave this value fixed.
     *
     * @param poolSize the number of worker threads
     * @param factory the factory for creating new threads
     * @throws IllegalArgumentException if poolSize less than or
     * equal to zero
     * @throws NullPointerException if factory is null
     * @throws SecurityException if a security manager exists and
     *         the caller is not permitted to modify threads
     *         because it does not hold {@link
     *         java.lang.RuntimePermission}<tt>("modifyThread")</tt>,
     */
    public ForkJoinPool(int poolSize, ForkJoinWorkerThreadFactory factory) {
        if (poolSize <= 0)
            throw new IllegalArgumentException();
        if (factory == null)
            throw new NullPointerException();
        checkPermission();
        this.poolSize = poolSize;
        this.factory = factory;
        this.poolNumber = poolNumberGenerator.incrementAndGet();
        this.poolBarrier = new PoolBarrier();
        this.runState = new RunState();
        this.runningSubmissions = new AtomicInteger();
        this.workerLock = new ReentrantLock();
        this.termination = workerLock.newCondition();
        SQNode dummy = new SQNode(null);
        this.sqHead = dummy;
        this.sqTail = dummy;
        createAndStartWorkers(poolSize);
    }

    /**
     * Initial worker array and worker creation and startup. (This
     * must be done under lock to avoid interference by some of the
     * newly started threads while creating others.)
     */
    private void createAndStartWorkers(int ps) {
        final ReentrantLock lock = this.workerLock;
        lock.lock();
        try {
            ForkJoinWorkerThread[] ws = 
                new ForkJoinWorkerThread[workerSizeFor(ps)];
            workers = ws;
            for (int i = 0; i < ps; ++i)
                ws[i] = createWorker(i);
            for (int i = 0; i < ps; ++i) {
                ws[i].start();
                ++runningWorkers;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Performs the given task; returning its result upon completion
     * @param task the task
     * @return the task's result
     * @throws NullPointerException if task is null
     * @throws RejectedExecutionException if pool is shut down
     */
    public <T> T invoke(ForkJoinTask<T> task) {
        return doSubmit(task).awaitInvoke();
    }

    /**
     * Arranges for (asynchronous) execution of the given task,
     * returning a {@link Future} that may be used to obtain results
     * upon completion. (The only supported operations on this object
     * are those defined in the <tt>Future</tt> interface.)
     * @param task the task
     * @return a Future that can be used to get the task's results.
     * @throws NullPointerException if task is null
     * @throws RejectedExecutionException if pool is shut down
     */
    public <T> Future<T> submit(ForkJoinTask<T> task) {
        return doSubmit(task);
    }

    /**
     * Arranges for (asynchronous) execution of the given task.
     * @param task the task
     * @throws NullPointerException if task is null
     * @throws RejectedExecutionException if pool is shut down
     */
    public <T> void execute(ForkJoinTask<T> task) {
        doSubmit(task);
    }

    /**
     * Common code for invoke and submit
     */
    private <T> Submission<T> doSubmit(ForkJoinTask<T> task) {
        if (task == null)
            throw new NullPointerException();
        if (runState.isAtLeastShutdown())
            throw new RejectedExecutionException();
        Submission<T> job = new Submission<T>(task, this);
        addSubmission(job);
        poolBarrier.signal();
        return job;
    }

    /**
     * Returns the targeted number of worker threads in this pool.
     * This value does not necessarily reflect transient changes as
     * threads are added, removed, or abruptly terminate.
     *
     * @return the number of worker threads in this pool
     */
    public int getPoolSize() {
        return poolSize;
    }

    /**
     * Equivalent to {@link #getPoolSize}.
     *
     * @return the number of worker threads in this pool
     */
    public int getParallelismLevel() {
        return poolSize;
    }

    /**
     * Returns the number of worker threads that have started but not
     * yet terminated.  This result returned by this method may differ
     * from <tt>getPoolSize</tt> when threads are added, removed, or
     * abruptly terminate.
     *
     * @return the number of worker threads
     */
    public int getRunningWorkerCount() {
        int r;
        final ReentrantLock lock = this.workerLock;
        lock.lock();
        try {
            r = runningWorkers;
        } finally {
            lock.unlock();
        }
        return r;
    }

    /**
     * Sets the handler for internal worker threads that terminate due
     * to unrecoverable errors encountered while executing tasks.
     * Unless set, the current default or ThreadGroup handler is used
     * as handler.
     *
     * @param h the new handler
     * @return the old handler, or null if none
     * @throws SecurityException if a security manager exists and
     *         the caller is not permitted to modify threads
     *         because it does not hold {@link
     *         java.lang.RuntimePermission}<tt>("modifyThread")</tt>,
     */
    public Thread.UncaughtExceptionHandler
        setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler h) {
        checkPermission();
        Thread.UncaughtExceptionHandler old = null;
        final ReentrantLock lock = this.workerLock;
        lock.lock();
        try {
            old = ueh;
            ueh = h;
            ForkJoinWorkerThread[] ws = workers;
            for (int i = 0; i < ws.length; ++i) {
                ForkJoinWorkerThread w = ws[i];
                if (w != null)
                    w.setUncaughtExceptionHandler(h);
            }
        } finally {
            lock.unlock();
        }
        return old;
    }

    /**
     * Returns the handler for internal worker threads that terminate
     * due to unrecoverable errors encountered while executing tasks.
     * @return the handler, or null if none
     */
    public Thread.UncaughtExceptionHandler getUncaughtExceptionHandler() {
        Thread.UncaughtExceptionHandler h;
        final ReentrantLock lock = this.workerLock;
        lock.lock();
        try {
            h = ueh;
        } finally {
            lock.unlock();
        }
        return h;
    }

    /**
     * Tries to adds the indicated number of new worker threads to the
     * pool. This method may be used to increase the amount of
     * parallelism available to tasks. The actual number of
     * threads added may be less than requested if the pool
     * is terminating or terminated
     * @return the number of threads added
     * @throws SecurityException if a security manager exists and
     *         the caller is not permitted to modify threads
     *         because it does not hold {@link
     *         java.lang.RuntimePermission}<tt>("modifyThread")</tt>,
     */
    public int addWorkers(int numberToAdd) {
        int nadded = 0;
        checkPermission();
        final ReentrantLock lock = this.workerLock;
        lock.lock();
        try {
            if (!runState.isAtLeastStopping()) {
                ForkJoinWorkerThread[] ws = workers;
                int len = ws.length;
                int newLen = len + numberToAdd;
                int newSize = workerSizeFor(newLen);
                ForkJoinWorkerThread[] nws =
                    new ForkJoinWorkerThread[newSize];
                System.arraycopy(ws, 0, nws, 0, len);
                for (int i = len; i < newLen; ++i)
                    nws[i] = createWorker(i);
                workers = nws;
                for (int i = len; i < newLen; ++i) {
                    nws[i].start();
                    ++runningWorkers;
                }
                poolSize += numberToAdd;
                nadded = numberToAdd;
            }
        } finally {
            lock.unlock();
        }
        return nadded;
    }

    /**
     * Tries to remove the indicated number of worker threads from the
     * pool. The workers will exit the next time they are idle. This
     * method may be used to decrease the amount of parallelism
     * available to tasks. The actual number of workers removed
     * may be less than requested if the pool size would become
     * zero or the pool is terminating or terminated.
     * @return the number removed.
     * @throws SecurityException if a security manager exists and
     *         the caller is not permitted to modify threads
     *         because it does not hold {@link
     *         java.lang.RuntimePermission}<tt>("modifyThread")</tt>,
     */
    public int removeWorkers(int numberToRemove) {
        int nremoved = 0;
        checkPermission();
        final ReentrantLock lock = this.workerLock;
        lock.lock();
        try {
            // shutdown in rightmost order to enable shrinkage in
            // workerTerminated
            ForkJoinWorkerThread[] ws = workers;
            int k = ws.length;
            while (!runState.isAtLeastStopping() &&
                   --k > 0 && // don't kill ws[0]
                   nremoved < numberToRemove) {
                ForkJoinWorkerThread w = ws[k];
                if (w != null) {
                    RunState rs = w.getRunState();
                    if (rs.transitionToShutdown()) {
                        --poolSize;
                        ++nremoved;
                    }
                }
            }
        } finally {
            lock.unlock();
        }
        return nremoved;
    }

    /**
     * Tries to add or remove workers to attain the given pool size.
     * This may fail to attain the given target if the pool is
     * terminating or terminated.
     * @param newSize the target poolSize
     * @return the pool size upon exit of this method
     * @throws IllegalArgumentException if newSize less than or
     * equal to zero
     * @throws SecurityException if a security manager exists and
     *         the caller is not permitted to modify threads
     *         because it does not hold {@link
     *         java.lang.RuntimePermission}<tt>("modifyThread")</tt>,
     */
    public int setPoolSize(int newSize) {
        checkPermission();
        if (newSize <= 0)
            throw new IllegalArgumentException();
        final ReentrantLock lock = this.workerLock;
        lock.lock();
        try {
            int ps = poolSize;
            if (newSize > ps)
                addWorkers(newSize - ps);
            else if (newSize < ps)
                removeWorkers(ps - newSize);
        } finally {
            lock.unlock();
        }
        return poolSize;
    }

    /**
     * Callback from terminating worker.
     * @param w the worker
     * @param ex the exception causing abrupt termination, or null if
     * completed normally
     */
    final void workerTerminated(ForkJoinWorkerThread w, Throwable ex) {
        try {
            final ReentrantLock lock = this.workerLock;
            lock.lock();
            try {
                // Unless stopping, null slot, and if rightmost slots
                // now null, shrink
                if (!runState.isAtLeastStopping()) {
                    int idx = w.getWorkerPoolIndex();
                    ForkJoinWorkerThread[] ws = workers;
                    int len = ws.length;
                    if (idx >= 0 && idx < len && ws[idx] == w) {
                        ws[idx] = null;
                        int newlen = len;
                        while (newlen > 0 && ws[newlen-1] == null)
                            --newlen;
                        if (newlen < len) {
                            int newSize = workerSizeFor(newlen);
                            if (newSize < len) {
                                ForkJoinWorkerThread[] nws =
                                    new ForkJoinWorkerThread[newSize];
                                System.arraycopy(ws, 0, nws, 0, newlen);
                                workers = nws;
                                poolBarrier.signal();
                            }
                        }
                    }
                }
                if (--runningWorkers == 0) {
                    terminate(); // no-op if already stopping
                    runState.transitionToTerminated();
                    termination.signalAll();
                }
            } finally {
                lock.unlock();
            }
        } finally {
            if (ex != null)
                ForkJoinTask.rethrowException(ex);
        }
    }

    // lifecycle control

    /**
     * Initiates an orderly shutdown in which previously submitted
     * tasks are executed, but no new tasks will be accepted.
     * Invocation has no additional effect if already shut down.
     * Tasks that are in the process of being submitted concurrently
     * during the course of this method may or may not be rejected.
     * @throws SecurityException if a security manager exists and
     *         the caller is not permitted to modify threads
     *         because it does not hold {@link
     *         java.lang.RuntimePermission}<tt>("modifyThread")</tt>,
     */
    public void shutdown() {
        checkPermission();
        runState.transitionToShutdown();
        tryTerminateOnShutdown();
    }

    /**
     * Attempts to stop all actively executing tasks, and cancels all
     * waiting tasks.  Tasks that are in the process of being
     * submitted or executed concurrently during the course of this
     * method may or may not be rejected.
     * @throws SecurityException if a security manager exists and
     *         the caller is not permitted to modify threads
     *         because it does not hold {@link
     *         java.lang.RuntimePermission}<tt>("modifyThread")</tt>,
     */
    public void shutdownNow() {
        checkPermission();
        terminate();
    }

    /**
     * Returns <tt>true</tt> if this pool has been shut down.
     *
     * @return <tt>true</tt> if this pool has been shut down
     */
    public boolean isShutdown() {
        return runState.isAtLeastShutdown();
    }

    /**
     * Returns <tt>true</tt> if all tasks have completed following shut down.
     *
     * @return <tt>true</tt> if all tasks have completed following shut down
     */
    public boolean isTerminated() {
        return runState.isTerminated();
    }

    /**
     * Returns <tt>true</tt> if termination has commenced but has
     * not yet completed.
     *
     * @return <tt>true</tt> if in the process of terminating
     */
    public boolean isTerminating() {
        return runState.isStopping();
    }

    /**
     * Blocks until all tasks have completed execution after a shutdown
     * request, or the timeout occurs, or the current thread is
     * interrupted, whichever happens first.
     *
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @return <tt>true</tt> if this executor terminated and
     *         <tt>false</tt> if the timeout elapsed before termination
     * @throws InterruptedException if interrupted while waiting
     */
    public boolean awaitTermination(long timeout, TimeUnit unit)
        throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.workerLock;
        lock.lock();
        try {
            for (;;) {
                if (runState.isTerminated())
                    return true;
                if (nanos <= 0)
                    return false;
                nanos = termination.awaitNanos(nanos);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Initiate termination.
     */
    private void terminate() {
        if (runState.transitionToStopping()) {
            stopAllWorkers();
            cancelQueuedSubmissions();
            cancelQueuedWorkerTasks();
            interruptUnterminatedWorkers();
        }
    }

    /**
     * Check for termination in shutdown state
     */
    private void tryTerminateOnShutdown() {
        if (runState.isAtLeastShutdown() &&
            runningSubmissions.get() == 0 &&
            !hasQueuedSubmissions() &&
            runningSubmissions.get() == 0) // recheck
            terminate();
    }

    /**
     * Clear out and cancel submissions
     */
    private void cancelQueuedSubmissions() {
        Submission<?> task;
        while (hasQueuedSubmissions() && (task = pollSubmission()) != null)
            task.cancel(false);
    }

    /**
     * Clean out worker queues.
     */
    private void cancelQueuedWorkerTasks() {
        final ReentrantLock lock = this.workerLock;
        lock.lock();
        try {
            ForkJoinWorkerThread[] ws = workers;
            for (int i = 0; i < ws.length; ++i) {
                ForkJoinWorkerThread t = ws[i];
                if (t != null)
                    t.cancelTasks();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Set each worker's status to stopping. Requires lock to avoid
     * conflicts with add/remove
     */
    private void stopAllWorkers() {
        final ReentrantLock lock = this.workerLock;
        lock.lock();
        try {
            ForkJoinWorkerThread[] ws = workers;
            for (int i = 0; i < ws.length; ++i) {
                ForkJoinWorkerThread t = ws[i];
                if (t != null) {
                    RunState rs = t.getRunState();
                    rs.transitionToStopping();
                }
            }
        } finally {
            lock.unlock();
        }
        poolBarrier.signal();
    }

    /**
     * Interrupt all unterminated workers.  This is not required for
     * sake of internal control, but may help unstick user code during
     * shutdown.
     */
    private void interruptUnterminatedWorkers() {
        final ReentrantLock lock = this.workerLock;
        lock.lock();
        try {
            ForkJoinWorkerThread[] ws = workers;
            for (int i = 0; i < ws.length; ++i) {
                ForkJoinWorkerThread t = ws[i];
                if (t != null) {
                    RunState rs = t.getRunState();
                    if (!rs.isTerminated()) {
                        try {
                            t.interrupt();
                        } catch (SecurityException ignore) {
                        }
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    // Status queries

    /**
     * Returns true if all worker threads are currently idle. An idle
     * worker is one that cannot obtain a task to execute because none
     * are available to steal from other threads, and there are no
     * pending submissions to the pool. This method is conservative:
     * It might not return true immediately upon idleness of all
     * threads, but will eventually become true if threads remain
     * inactive.
     * @return true if all threads are currently idle
     */
    public final boolean isQuiescent() {
        return activeCount == 0;
    }

    /**
     * Returns the approximate number of threads that are
     * currently executing tasks. This method may overestimate
     * the number of active threads.
     * @return the number of active threads.
     */
    public final int getActiveThreadCount() {
        return activeCount;
    }

    /**
     * Returns the approximate number of threads that are currently
     * idle waiting for tasks. This method may underestimate the
     * number of idle threads.
     * @return the number of idle threads.
     */
    public final int getIdleThreadCount() {
        return poolSize - activeCount;
    }

    /**
     * Returns the total number of tasks stolen from one thread's work
     * queue by another. This value is only an approximation, obtained
     * by iterating across all threads in the pool, and may
     * mis-estimate the actual total number of steals when the pool is
     * not quiescent. But the value is still useful for monitoring and
     * tuning fork/join programs: In general, steal counts should be
     * high enough to keep threads busy, but low enough to avoid
     * overhead and contention across threads.
     * @return the number of steals.
     */
    public long getStealCount() {
        long sum = 0;
        ForkJoinWorkerThread[] ws = workers;
        for (int i = 0; i < ws.length; ++i) {
            ForkJoinWorkerThread t = ws[i];
            if (t != null)
                sum += t.getWorkerStealCount();
        }
        return sum;
    }

    /**
     * Returns the total number of tasks currently held in queues by
     * worker threads (but not including tasks submitted to the pool
     * that have not begun executing). This value is only an
     * approximation, obtained by iterating across all threads in the
     * pool. This method may be useful for tuning task granularities.
     * @return the number of tasks.
     */
    public long getTotalPerThreadQueueSize() {
        long count = 0;
        ForkJoinWorkerThread[] ws = workers;
        for (int i = 0; i < ws.length; ++i) {
            ForkJoinWorkerThread t = ws[i];
            if (t != null)
                count += t.getQueueSize();
        }
        return count;
    }

    /**
     * Returns the number of tasks that have been submitted (via
     * <tt>submit</tt> or <tt>invoke</tt>) and are currently executing
     * in the pool.
     * @return the number of tasks.
     */
    public int getActiveSubmissionCount() {
        return runningSubmissions.get();
    }

    /**
     * Returns the factory used for constructing new workers
     *
     * @return the factory used for constructing new workers
     */
    public ForkJoinWorkerThreadFactory getFactory() {
        return factory;
    }

    // Callbacks from submissions

    /**
     * Callback on starting execution of externally submitted job.
     */
    final void submissionStarting() {
        runningSubmissions.incrementAndGet();
    }

    /**
     * Completion callback from externally submitted job.
     */
    final void submissionCompleted() {
        if (runningSubmissions.decrementAndGet() == 0 &&
            runState.isAtLeastShutdown())
            tryTerminateOnShutdown();
    }

    /**
     * Wait for a pool event, if necessary. Called only by workers.
     */
    final long barrierSync(long eventCount) {
        return poolBarrier.sync(eventCount);
    }

    /**
     * Embedded submission queue holds submissions not yet started by
     * workers. This is a variant of an Michael/Scott queue that
     * supports a fast check for apparent emptiness.  This class
     * opportunistically subclasses AtromicReference for next-field
     */
    static final class SQNode extends AtomicReference<SQNode> {
        Submission<?> submission;
        SQNode(Submission<?> s) { submission = s; }
    }

    /**
     * Quick check for likely non-emptiness.  Returns true if an
     * add completed but not yet fully taken.
     */
    final boolean mayHaveQueuedSubmissions() {
        return sqHead != sqTail;
    }

    /**
     * Returns true if there are any tasks submitted to this pool
     * that have not yet begun executing.
     * @return <tt>true</tt> if there are any queued submissions.
     */
    public boolean hasQueuedSubmissions() {
        for (;;) {
            SQNode h = sqHead;
            SQNode t = sqTail;
            SQNode f = h.get();
            if (h == sqHead) {
                if (f == null)
                    return false;
                else if (h != t)
                    return true;
                else
                    casSqTail(t, f);
            }
        }
    }

    final void addSubmission(Submission<?> x) {
        SQNode n = new SQNode(x);
        for (;;) {
            SQNode t = sqTail;
            SQNode s = t.get();
            if (t == sqTail) {
                if (s != null)
                    casSqTail(t, s);
                else if (t.compareAndSet(s, n)) {
                    casSqTail(t, n);
                    return;
                }
            }
        }
    }

    final Submission<?> pollSubmission() {
        for (;;) {
            SQNode h = sqHead;
            SQNode t = sqTail;
            SQNode f = h.get();
            if (h == sqHead) {
                if (f == null)
                    return null;
                else if (h == t)
                    casSqTail(t, f);
                else if (casSqHead(h, f)) {
                    Submission<?> x = f.submission;
                    f.submission = null;
                    return x;
                }
            }
        }
    }

    // Temporary Unsafe mechanics for preliminary release

    static final Unsafe _unsafe;
    static final long activeCountOffset;
    static final long sqHeadOffset;
    static final long sqTailOffset;

    static {
        try {
            if (ForkJoinPool.class.getClassLoader() != null) {
                Field f = Unsafe.class.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                _unsafe = (Unsafe)f.get(null);
            }
            else
                _unsafe = Unsafe.getUnsafe();
            activeCountOffset = _unsafe.objectFieldOffset
                (ForkJoinPool.class.getDeclaredField("activeCount"));
            sqHeadOffset = _unsafe.objectFieldOffset
                (ForkJoinPool.class.getDeclaredField("sqHead"));
            sqTailOffset = _unsafe.objectFieldOffset
                (ForkJoinPool.class.getDeclaredField("sqTail"));
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize intrinsics", e);
        }
    }

    final boolean tryIncrementActiveCount() {
        int c = activeCount;
        return _unsafe.compareAndSwapInt(this, activeCountOffset,
                                         c, c+1);
    }

    final boolean tryDecrementActiveCount() {
        int c = activeCount;
        return _unsafe.compareAndSwapInt(this, activeCountOffset,
                                         c, c-1);
    }

    final void incrementActiveCount() {
        while (!tryIncrementActiveCount())
            ;
    }

    final void decrementActiveCount() {
        while (!tryDecrementActiveCount())
            ;
    }

    private boolean casSqTail(SQNode cmp, SQNode val) {
        return _unsafe.compareAndSwapObject(this, sqTailOffset, cmp, val);
    }

    private boolean casSqHead(SQNode cmp, SQNode val) {
        return _unsafe.compareAndSwapObject(this, sqHeadOffset, cmp, val);
    }
}
