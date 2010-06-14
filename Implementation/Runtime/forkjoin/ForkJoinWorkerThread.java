/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

package jsr166y.forkjoin;
import jsr166y.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;
import sun.misc.Unsafe;
import java.lang.reflect.*;

/**
 * A thread that is internally managed by a ForkJoinPool to execute
 * ForkJoinTasks. This class additionally provides public
 * <tt>static</tt> methods accessing some basic scheduling and
 * execution mechanics for the <em>current</em>
 * ForkJoinWorkerThread. These methods may be invoked only from within
 * other ForkJoinTask computations. Attempts to invoke in other
 * contexts result in exceptions or errors including
 * ClassCastException.  These methods enable construction of
 * special-purpose task classes, as well as specialized idioms
 * occasionally useful in ForkJoinTask processing.
 *
 * <p>The form of supported static methods reflects the fact that
 * worker threads may access and process tasks obtained in any of
 * three ways. In preference order: <em>Local</em> tasks are processed
 * in LIFO (newest first) order. <em>Stolen</em> tasks are obtained
 * from other threads in FIFO (oldest first) order, only if there are
 * no local tasks to run.  <em>Submissions</em> form a FIFO queue
 * common to the entire pool, and are started only if no other
 * work is available.
 *
 * <p> This class also includes utility methods for accessing and
 * manipulating submissions to the pool, in support of extensions that
 * provide more extensive error recovery and/or alternate forms of
 * execution.
 *
 * <p> This class is subclassable solely for the sake of adding
 * functionality -- there are no overridable methods dealing with
 * scheduling or execution. However, you can override initialization
 * and termination cleanup methods surrounding the main task
 * processing loop.  If you do create such a subclass, you will also
 * need to supply a custom ForkJoinWorkerThreadFactory to use it in a
 * ForkJoinPool.
 */
public class ForkJoinWorkerThread extends Thread {

    /*
     * Algorithm overview:
     *
     * 1. Work-Stealing: Work-stealing queues are special forms of
     * Deques that support only three of the four possible
     * end-operations -- push, pop, and deq (aka steal), and only do
     * so under the constraints that push and pop are called only from
     * the owning thread, while deq may be called from other threads.
     * (If you are unfamiliar with them, you probably want to read
     * Herlihy and Shavit's book "The Art of Multiprocessor
     * programming", chapter 16 describing these in more detail before
     * proceeding.)  The main work-stealing queue design is roughly
     * similar to "Dynamic Circular Work-Stealing Deque" by David
     * Chase and Yossi Lev, SPAA 2005
     * (http://research.sun.com/scalable/pubs/index.html).  The main
     * difference ultimately stems from gc requirements that we null
     * out taken slots as soon as we can, to maintain as small a
     * footprint as possible even in programs generating huge numbers
     * of tasks. To accomplish this, we shift the CAS arbitrating pop
     * vs deq (steal) from being on the indices ("base" and "sp") to
     * the slots themselves (mainly via method "casSlotNull()"). So,
     * both a successful pop and deq mainly entail CAS'ing a nonnull
     * slot to null.  Because we rely on CASes of references, we do
     * not need tag bits on base or sp.  They are simple ints as used
     * in any circular array-based queue (see for example ArrayDeque).
     * Updates to the indices must still be ordered in a way that
     * guarantees that (sp - base) > 0 means the queue is empty, but
     * otherwise may err on the side of possibly making the queue
     * appear nonempty when a push, pop, or deq have not fully
     * committed. Note that this means that the deq operation,
     * considered individually, is not wait-free. One thief cannot
     * successfully continue until another in-progress one (or, if
     * previously empty, a push) completes.  However, in the
     * aggregate, we ensure at least probablistic non-blockingness. If
     * an attempted steal fails, a thief always chooses a different
     * random victim target to try next. So, in order for one thief to
     * progress, it suffices for any in-progress deq or new push on
     * any empty queue to complete. One reason this works well here is
     * that apparently-nonempty often means soon-to-be-stealable,
     * which gives threads a chance to activate if necessary before
     * stealing (see below).
     *
     * Efficient implementation of this approach currently relies on
     * an uncomfortable amount of "Unsafe" mechanics. To maintain
     * correct orderings, reads and writes of variable base require
     * volatile ordering.  Variable sp does not require volatile loads
     * (so long as other threads read base first), but require
     * store-ordering on writes.  Because they are protected by
     * volatile base reads, reads of the queue array and its slots do
     * not need volatile load semantics, but writes (in push) require
     * store order and CASes (in pop and deq) require (volatile) CAS
     * semantics. Since these combinations aren't supported using
     * ordinary volatiles, the only way to accomplish these effciently
     * is to use direct Unsafe calls. (Using external AtomicIntegers
     * and AtomicReferenceArrays for the indices and array is
     * significantly slower because of memory locality and indirection
     * effects.) Further, performance on most platforms is very
     * sensitive to placement and sizing of the (resizable) queue
     * array.  Even though these queues don't usually become all that
     * big, the initial size must be large enough to counteract cache
     * contention effects across multiple queues (especially in the
     * presence of GC cardmarking), Also, to improve thread-locality,
     * queues are currently initialized immediately after the thread
     * gets the initial signal to start processing tasks.  However,
     * all queue-related methods except pushTask are written in a way
     * that allows them to instead be lazily allocated and/or disposed
     * of when empty. All together, these low-level implementation
     * choices produce as much as a factor of 4 performance
     * improvement compared to naive implementations, and enable the
     * processing of billions of tasks per second, sometimes at the
     * expense of ugliness.
     *
     * 2. Run control: The primary run control is based on a global
     * counter (activeCount) held by the pool. It uses an algorithm
     * similar to that in Herlihy and Shavit section 17.6 to cause
     * threads to eventually block when all threads declare they are
     * inactive. (See variable "scans".)  For this to work, threads
     * must be declared active when executing tasks, and before
     * stealing a task. They must be inactive before blocking on the
     * PoolBarrier (awaiting a new submission or other Pool event). In
     * between, there is some free play which we take advantage of to
     * avoid contention and rapid flickering of the global
     * activeCount: If inactive, we activate only if a victim queue
     * appears to be nonempty (see above), and even then, back off,
     * looking for another victim if the attempt (CAS) to increase
     * activeCount fails.  Similarly, a thread tries to inactivate
     * only after a full scan of other threads, and if the attempted
     * decrement fails, rescans instead. The net effect is that
     * contention on activeCount is rarely a measurable performance
     * issue. (There are also a few other cases where we scan for work
     * rather than retry/block upon contention.)
     *
     * Unlike in previous incarnations of this framework, we do not
     * ever block worker threads while submissions are executing
     * (i.e., activeCount is nonzero). Doing so can lead to anomalies
     * (like convoying of dependent threads) and overheads that negate
     * benefits. To compensate, we ensure that threads looking for
     * work are extremely well-behaved. Scans (mainly in
     * getStolenTask; also getSubmission and scanWhileJoining) do not
     * modify any variables that might disrupt caches (except, when
     * necessary, activation status) and probe only the base/sp fields
     * of other threads unless they appear non-empty. We also
     * occasionally perform Thread.yields, which may or may not
     * improve good citizenship. It may be possible to replace this
     * with a different advisory blocking scheme that better shields
     * users from the effects of poor ForkJoin task design causing
     * imbalances, in turn causing excessive spins.
     *
     * 3. Selection control. We maintain policy of always choosing to
     * run local tasks rather than stealing, and always trying to
     * steal tasks before trying to run a new submission. This shows
     * up in different ways in different cases though, accounting for
     * the number of different run/get methods. All steals are
     * currently performed in randomly-chosen deq-order. It may be
     * worthwhile to bias these with locality / anti-locality
     * information, but doing this well probably requires more
     * lower-level information from JVMs than currently provided.
     */

    /**
     * Capacity of work-stealing queue array upon initialization.
     * Must be a power of two. Initial size must be at least 2, but is
     * padded to minimize cache effects.
     */
    private static final int INITIAL_QUEUE_CAPACITY = 1 << 13;

    /**
     * Maximum work-stealing queue array size.  Must be less than or
     * equal to 1 << 30 to ensure lack of index wraparound.
     */
    private static final int MAXIMUM_QUEUE_CAPACITY = 1 << 30;

    /**
     * Generator of seeds for per-thread random numbers.
     */
    private static final Random randomSeedGenerator = new Random();

    /**
     * Run state of this worker.
     */
    private final RunState runState;

    /**
     * The pool this thread works in.
     */
    private final ForkJoinPool pool;

    /**
     * The work-stealing queue array. Size must be a power of two.
     */
    private ForkJoinTask<?>[] queue;

    /**
     * Index (mod queue.length) of next queue slot to push to or pop
     * from. It is written only by owner thread, via ordered store.
     * Both sp and base are allowed to wrap around on overflow, but
     * (sp - base) still estimates size. To improve array locality,
     * indices are occasionally renormalized to zero (see
     * tryInactivate).
     */
    private int sp;

    /**
     * Index (mod queue.length) of least valid queue slot, which is
     * always the next position to steal from if nonempty.
     */
    private volatile int base;

    /**
     * Activity status and pause control. When zero, this worker is
     * considered active. Nonzero values indicate number of empty
     * scans (see getStolenTask) to control pausing.  The value must
     * be nonzero upon construction. It must be zero when executing
     * tasks, and BEFORE stealing a task. It must be nonzero before
     * blocking on the PoolBarrier.
     */
    private int scans;

    /**
     * Seed for random number generator for choosing steal victims
     */
    private int randomVictimSeed;

    /**
     * Number of steals, transferred to fullStealCount when idle
     */
    private int stealCount;

    /**
     * Number of steals, just for monitoring purposes,
     */
    private volatile long fullStealCount;

    /**
     * Seed for juRandom methods.
     */
    private long juRandomSeed;

    /**
     * The last event count waited for
     */
    private long eventCount;

    /**
     * Index of this worker in pool array. Set once by pool before running.
     */
    private int poolIndex;

    // Padding to help avoid cacheline sharing across workers
    private int pad0, pad1, pad2, pad3, pad4, pad5, pad6, pad7;
    private int pad8, pad9, pada, padb, padc, padd, pade, padf;

    /**
     * Creates a ForkJoinWorkerThread operating in the given pool.
     * @param pool the pool this thread works in
     * @throws NullPointerException if pool is null;
     */
    protected ForkJoinWorkerThread(ForkJoinPool pool) {
        if (pool == null) throw new NullPointerException();
        this.pool = pool;
        this.runState = new RunState();
        this.scans = 1;
        int rseed = randomSeedGenerator.nextInt();
        this.randomVictimSeed = (rseed == 0)? 1 : rseed; // must be nonzero
        this.juRandomSeed = randomSeedGenerator.nextLong();
    }

    // Initialization and access methods used by Pool

    final void setWorkerPoolIndex(int i) {
        poolIndex = i;
    }

    final int getWorkerPoolIndex() {
        return poolIndex;
    }

    final RunState getRunState() {
        return runState;
    }

    final long getWorkerStealCount() {
        return fullStealCount + stealCount; // can peek at local count too
    }

    // Primitive support for queue operations

    /**
     * Sets sp in store-order.
     */
    private final void setSp(int s) {
        _unsafe.putOrderedInt(this, spOffset, s);
    }

    /**
     * Add in store-order the given task at given slot of q to
     * null. Caller must ensure q is nonnull and index is in range.
     */
    private static final void setSlot(ForkJoinTask<?>[] q, int i,
                                      ForkJoinTask<?> t){
        _unsafe.putOrderedObject(q, (i << qShift) + qBase, t);
    }

    /**
     * CAS given slot of q to null. Caller must ensure q is nonnull
     * and index is in range.
     */
    private static final boolean casSlotNull(ForkJoinTask<?>[] q, int i,
                                             ForkJoinTask<?> t) {
        return _unsafe.compareAndSwapObject(q, (i << qShift) + qBase, t, null);
    }

    // Main queue methods

    /**
     * Returns an estimate of the number of tasks in the queue.
     */
    final int getQueueSize() {
        int n = sp - base;
        return n < 0? 0 : n; // suppress momentarily negative values
    }

    /**
     * Pushes a task. Called only by current thread.
     * @param t the task. Caller must ensure nonnull
     */
    final void pushTask(ForkJoinTask<?> t) {
        ForkJoinTask<?>[] q = queue;
        int mask = q.length - 1;
        int s = sp;
        setSp(s + 1);
        setSlot(q, s & mask, t);
        if (mask <= s + 1 - base)
            growQueue();
   }

    /**
     * Tries to take a task from the base of the queue, failing if
     * either empty or contended.
     * @return a task, or null if none or contended.
     */
    private final ForkJoinTask<?> deqTask() {
        ForkJoinTask<?> t;
        int i;
        int b = base;
        ForkJoinTask<?>[] q = queue;
        if (b - sp < 0 &&
            q != null &&
            (t = q[i = b & (q.length - 1)]) != null &&
            casSlotNull(q, i, t)) {
            base = b + 1;
            return t;
        }
        return null;
    }

    /**
     * Returns a popped task, or null if empty.  Called only by
     * current thread.
     */
    final ForkJoinTask<?> popTask() {
        ForkJoinTask<?> t;
        int i;
        int s;
        ForkJoinTask<?>[] q = queue;
        if (q != null &&
            (s = sp - 1) - base >= 0 &&
            (t = q[i = s & (q.length - 1)]) != null &&
            casSlotNull(q, i, t)) {
            setSp(s);
            return t;
        }
        return null;
    }

    /**
     * Same as popTask, but with implementation biased to expect a
     * task to be available
     */
    private final ForkJoinTask<?> expectedPopTask() {
        int s;
        ForkJoinTask<?>[] q = queue;
        if (q != null) {
            int i = (q.length - 1) & (s = sp - 1);
            ForkJoinTask<?> t = q[i];
            if (casSlotNull(q, i, t) && t != null) {
                setSp(s);
                return t;
            }
        }
        return null;
    }

    /**
     * Specialized version of popTask to pop only if
     * topmost element is the given task.
     * @param t the task to match (null is never matched)
     */
    final boolean popIfNext(ForkJoinTask<?> t) {
        int s;
        ForkJoinTask<?>[] q = queue;
        if (t != null && q != null &&
            casSlotNull(q, (q.length - 1) & (s = sp - 1), t)) {
            setSp(s);
            return true;
        }
        return false;
    }

    /**
     * Returns next task to pop.
     */
    final ForkJoinTask<?> peekTask() {
        ForkJoinTask<?>[] q = queue;
        return q == null? null : q[(sp - 1) & (q.length - 1)];
    }

    /**
     * Doubles queue array size. Transfers elements by emulating
     * steals (deqs) from old array and placing, oldest first, into
     * new array.
     */
    private final void growQueue() {
        ForkJoinTask<?>[] oldQ = queue;
        int oldSize = oldQ.length;
        int newSize = oldSize << 1;
        if (newSize > MAXIMUM_QUEUE_CAPACITY)
            throw new RejectedExecutionException("Queue capacity exceeded");
        ForkJoinTask<?>[] newQ = queue = new ForkJoinTask<?>[newSize];

        int b = base;
        int bf = b + oldSize;
        int oldMask = oldSize - 1;
        int newMask = newSize - 1;
        do {
            int oldIndex = b & oldMask;
            ForkJoinTask<?> t = oldQ[oldIndex];
            if (t != null && !casSlotNull(oldQ, oldIndex, t))
                t = null;
            setSlot(newQ, b & newMask, t);
        } while (++b != bf);
    }

    // Activation control

    /**
     * Unconditionally set status to active and adjust activeCount
     */
    private final void ensureActive() {
        if (scans != 0) {
            scans = 0;
            pool.incrementActiveCount();
        }
    }

    /**
     * Try to activate but fail on contention on active worker counter
     * @return true if now active
     */
    private final boolean tryActivate() {
        if (scans != 0) {
            if (!pool.tryIncrementActiveCount())
                return false;
            scans = 0;
        }
        return true;
    }

    /**
     * Unconditionally inactivate. Does not block even if activeCount
     * now zero. (Use tryInactivate instead.) Needed for helpQuiesce.
     */
    private final void ensureInactive() {
        if (scans == 0) {
            scans = 1;
            pool.decrementActiveCount();
        }
    }

    /**
     * Possibly inactivate and block or pause waiting for work.  If
     * pool is quiescent, before blocking, transfer local steal count
     * to volatile field.  Also renormalizes queue indices to improve
     * future queue array locality. Note that this can cause ongoing
     * steals to momentarily believe queue is nonempty but will still
     * fail to extract a task, which at most may cause them to
     * unnecessarily activate, but even this is minimized by only
     * doing this upon quiesence.
     *
     * Precondition: Local queue is empty, and at least one full scan
     * of other worker queus and submissions failed to find a task.
     *
     * @return true if pool apparently idle on entry to this method
     */
    private final boolean tryInactivate() {
        if (scans == 0 && !pool.tryDecrementActiveCount())
            return false;
        ++scans;
        if (pool.getActiveThreadCount() != 0) {
            if (scans >= SCANS_PER_PAUSE) {
                scans = 1;
                pauseAwaitingWork(null);
            }
            return false;
        }
        if (sp != 0) { // renormalize indices
            setSp(0);
            base = 0;
        }
        int sc = stealCount; // accumulate steals
        if (sc != 0) {
            long fsc = fullStealCount + sc;
            stealCount = 0;
            fullStealCount = fsc;
        }
        eventCount = pool.barrierSync(eventCount);
        return true;
    }

    // Support for pausing when inactive

    /**
     * The number of empty steal attempts before pausing.  Must be a
     * power of two.
     */
    private static final int PROBES_PER_PAUSE = (1 << 10);

    /**
     * The number of empty scans (== probe each worker at least once)
     * before pausing. Based on actual number of processors, not
     * actual poolSize, since this better estimates effects of memory
     * stalls etc on larger machines.
     */
    private static final int SCANS_PER_PAUSE =
        PROBES_PER_PAUSE / Runtime.getRuntime().availableProcessors();

    /**
     * Politely stall when cannot find a task to run. First check run
     * status and cancel task if stopping. Currently, pauses are
     * implemented only as yield, but may someday incorporate advisory
     * blocking.
     * @param joinMe if nonnull, a task to cancel if stopping
     */
    private final void pauseAwaitingWork(ForkJoinTask<?> joinMe) {
        if (runState.isAtLeastStopping()) {
            if (joinMe != null) 
                joinMe.cancel();
        }
        else
            Thread.yield();
    }

    // Lifecycle methods
    
    /**
     * Initializes internal state after construction but before
     * processing any tasks. If you override this method, you must
     * invoke super.onStart() at the beginning of the method.
     * Initialization requires care: Most fields must have legal
     * default values, to ensure that attempted accesses from other
     * threads work correctly even before this thread starts
     * processing tasks.
     */
    protected void onStart() {
        // wait for start signal before allocating queue array
        eventCount = pool.barrierSync(0);
        if (queue == null)
            queue = new ForkJoinTask<?>[INITIAL_QUEUE_CAPACITY];
    }

    /**
     * Perform cleanup associated with termination of this worker
     * thread.  If you override this method, you must invoke
     * super.onTermination at the end of the overridden method.
     *
     * @param exception the exception causing this thread to abort due
     * to an unrecoverable error, or null if completed normally.
     */
    protected void onTermination(Throwable exception) {
        try {
            clearLocalTasks();
            ensureInactive();
            cancelTasks();
            runState.transitionToTerminated();
        } finally {
            pool.workerTerminated(this, exception);
        }
    }

    /**
     * This method is required to be public, but should never be
     * called explicitly. It performs the main run loop to execute
     * ForkJoinTasks.
     */
    public void run() {
        try {
            onStart();
            mainLoop();
            onTermination(null);
        } catch (Throwable ex) {
            onTermination(ex);
        }
    }

    // Methods for running submissions, stolen and/or local tasks

    /**
     * Main run loop. On each step prefer running a submission
     * if previously inactive, else prefer stolen task. If a
     * task was run, also run any other subtasks it pushed;
     * otherwise inactivate.
     */
    private final void mainLoop() {
        boolean preferSubmission = true;
        while (runState.isRunning()) {
            if ((preferSubmission || !runStolenTask()) && !runSubmission())
                preferSubmission = !preferSubmission && tryInactivate();
            else {
                runLocalTasks();
                preferSubmission = false;
            }
        }
    }

    /**
     * Runs all tasks on local queue
     */
    private final void runLocalTasks() {
        ForkJoinTask<?> t;
        while ((t = expectedPopTask()) != null)
            t.exec();
    }

    /**
     * Runs a stolen task if one exists.
     * @return true if ran a task
     */
    private final boolean runStolenTask() {
        ForkJoinTask<?> t = getStolenTask();
        if (t != null) {
            t.exec();
            return true;
        }
        return false;
    }

    /**
     * Runs a submission if one exists.
     * @return true if ran a task
     */
    private final boolean runSubmission() {
        Submission<?> s = getSubmission();
        if (s != null) {
            s.exec();
            return true;
        }
        return false;
    }

    /**
     * Returns a submission, if one exists; activating first if necessary
     */
    private final Submission<?> getSubmission() {
        while (pool.mayHaveQueuedSubmissions()) {
            Submission<?> s;
            if (tryActivate() && (s = pool.pollSubmission()) != null)
                return s;
        }
        return null;
    }

    /**
     * Runs one popped task, if available
     * @return true if ran a task
     */
    private final boolean runLocalTask() {
        ForkJoinTask<?> t = popTask();
        if (t != null) {
            t.exec();
            return true;
        }
        return false;
    }

    /**
     * Pops or steals a task
     * @return task, or null if none available
     */
    private final ForkJoinTask<?> getLocalOrStolenTask() {
        ForkJoinTask<?> t = popTask();
        return t != null? t : getStolenTask();
    }

    /**
     * Runs one popped or stolen task, if available
     * @return true if ran a task
     */
    private final boolean runLocalOrStolenTask() {
        ForkJoinTask<?> t = getLocalOrStolenTask();
        if (t != null) {
            t.exec();
            return true;
        }
        return false;
    }

    /**
     * Runs tasks until activeCount zero
     */
    private final void runUntilQuiescent() {
        for (;;) {
            ForkJoinTask<?> t = getLocalOrStolenTask();
            if (t != null) {
                ensureActive();
                t.exec();
            }
            else {
                ensureInactive();
                if (pool.getActiveThreadCount() == 0) {
                    ensureActive(); // reactivate on exit
                    break;
                }
            }
        }
    }

    // Stealing tasks

    /**
     * Computes next value for random victim probe. Scans don't
     * require a very high quality generator, but also not a crummy
     * one. Marsaglia xor-shift is cheap and works well.
     */
    private static final int xorShift(int r) {
        r ^= r << 1;
        r ^= r >>> 3;
        return r ^ (r << 10);
    }

    /**
     * Tries to steal a task from another worker. Starts at a random
     * index of workers array, and probes workers until finding one
     * with non-empty queue or finding that all are empty.  It
     * randomly selects the first n-1 probes. If these are empty, it
     * resorts to a full circular traversal, which is necessary to
     * accurately set active status by caller.
     *
     * This method must be both fast and quiet -- avoiding as much as
     * possible memory accesses that could disrupt cache sharing etc
     * other than those needed to check for and take tasks. This
     * accounts for, among other things, updating random seed in place
     * without storing it until exit. (Note that we only need to store
     * it if we found a task; otherwise it doesn't matter if we start
     * at the same place next time.)
     *
     * @return a task, or null if none found
     */
    private final ForkJoinTask<?> getStolenTask() {
        final ForkJoinWorkerThread[] ws = pool.workers;
        final int mask = ws.length - 1;  // must be power of 2 minus 1
        int probes = -mask;              // use random index while negative
        int r = randomVictimSeed;        // extract once to keep scan quiet
        int idx = r;
        ForkJoinTask<?> t = null;
        do {
            ForkJoinWorkerThread v = ws[mask & idx];
            r = xorShift(r);                         // update seed
            if (v != null && v.base - v.sp < 0) {    // apparently nonempty
                if (tryActivate() && (t = v.deqTask()) != null) {
                    randomVictimSeed = r;
                    ++stealCount;
                    break;
                }
                probes = -mask;                   // restart on contention
                idx = r;
                continue;
            }
            idx = probes < 0? r : (idx + 1);      // n-1 random then circular
        } while (probes++ <= mask);
        return t;
    }

    /**
     * Tries to steal tasks while waiting for join.  Similar to
     * getStolenTask except intersperses checks for completion and
     * shutdown.
     * @return a task, or null if joinMe is completed
     */
    private final ForkJoinTask<?> scanWhileJoining(ForkJoinTask<?> joinMe) {
        ForkJoinWorkerThread[] ws = pool.workers;
        int mask = ws.length - 1;
        int r = randomVictimSeed;
        int idx = r;
        int probes = 0;
        ForkJoinTask<?> t = null;
        for (;;) {
            ForkJoinWorkerThread v = ws[idx & mask];
            r = xorShift(r);
            if (joinMe.status < 0)
                break;
            if (v != null && (t = v.deqTask()) != null) {
                randomVictimSeed = r;
                ++stealCount;
                break;
            }
            if ((++probes & (PROBES_PER_PAUSE - 1)) == 0)
                pauseAwaitingWork(joinMe);
            idx = probes <= mask? r: (idx + 1); // n-1 random then circular
        }
        return t;
    }

    // Support for core ForkJoinTask methods

    /**
     * Implements ForkJoinTask.quietlyJoin
     */
    final void helpJoinTask(ForkJoinTask<?> joinMe) {
        ForkJoinTask<?> t;
        while (joinMe.status >= 0 &&
               ((t = popTask()) != null ||
                (t = scanWhileJoining(joinMe)) != null))
            t.exec();
    }

    /**
     * Implements RecursiveAction.forkJoin
     */
    final void doForkJoin(RecursiveAction t1, RecursiveAction t2) {
        if (t1.status >= 0 && t2.status >= 0) {
            pushTask(t2);
            if (t1.rawExec()) {
                if (popIfNext(t2)) {
                    if (t2.rawExec())
                        return;
                }
                else {
                    helpJoinTask(t2);
                    if (t2.completedNormally())
                        return;
                }
            }
        }
        Throwable ex;
        if ((ex = t1.getException()) != null)
            t2.cancel();
        else if ((ex = t2.getException()) != null)
            t1.cancel();
        if (ex != null)
            ForkJoinTask.rethrowException(ex);
    }

    /**
     * Timeout version of helpJoin needed for Submission class
     * Returns false if timed out before complated
     */
    final boolean doTimedJoinTask(ForkJoinTask<?> joinMe, long nanos) {
        long startTime = System.nanoTime();
        int spins = 0;
        for (;;) {
            ForkJoinTask<?> t = popTask();
            if (joinMe.isDone())
                return true;
            else if ((t = getLocalOrStolenTask())!= null)
                t.exec();
            else if (runState.isAtLeastStopping())
                return false;
            else if (nanos - (System.nanoTime() - startTime) <= 0)
                return false;
        }
    }

    // Cleanup support

    /**
     * Run or cancel all local tasks on exit from main.
     */
    private final void clearLocalTasks() {
        while (sp - base > 0) {
            ForkJoinTask<?> t = popTask();
            if (t != null) {
                if (runState.isAtLeastStopping())
                    t.setCancelled(); // avoid exceptions due to cancel()
                else
                    t.exec();
            }
        }
    }

    /**
     * Removes and cancels all tasks in queue.  Can be called from any
     * thread.
     */
    final void cancelTasks() {
        while (sp - base > 0) {
            ForkJoinTask<?> t = deqTask();
            if (t != null) // avoid exceptions due to cancel()
                t.setCancelled();
        }
    }


    // Public methods on current thread

    /**
     * Returns the pool hosting the current task execution.
     * @return the pool
     */
    public static ForkJoinPool getPool() {
        return ((ForkJoinWorkerThread)(Thread.currentThread())).pool;
    }

    /**
     * Returns the index number of the current worker thread in its
     * pool.  The return value is in the range
     * <tt>0...getPool().getPoolSize()-1</tt>.  This method may be
     * useful for applications that track status or collect results
     * per-worker rather than per-task.
     * @return the index number.
     */
    public static int getPoolIndex() {
        return ((ForkJoinWorkerThread)(Thread.currentThread())).poolIndex;
    }

    /**
     * Returns an estimate of the number of tasks waiting to be run by
     * the current worker thread. This value may be useful for
     * heuristic decisions about whether to fork other tasks.
     * @return the number of tasks
     */
    public static int getLocalQueueSize() {
        return ((ForkJoinWorkerThread)(Thread.currentThread())).
            getQueueSize();
    }

    /**
     * Returns, but does not remove or execute, the next task locally
     * queued for execution by the current worker thread. There is no
     * guarantee that this task will be the next one actually returned
     * or executed from other polling or execution methods.
     * @return the next task or null if none
     */
    public static ForkJoinTask<?> peekLocalTask() {
        return ((ForkJoinWorkerThread)(Thread.currentThread())).peekTask();
    }

    /**
     * Removes and returns, without executing, the next task queued
     * for execution in the current worker thread's local queue.
     * @return the next task to execute, or null if none
     */
    public static ForkJoinTask<?> pollLocalTask() {
        return ((ForkJoinWorkerThread)(Thread.currentThread())).popTask();
    }

    /**
     * Execute the next task locally queued by the current worker, if
     * one is available.
     * @return true if a task was run; a false return indicates
     * that no task was available.
     */
    public static boolean executeLocalTask() {
        return ((ForkJoinWorkerThread)(Thread.currentThread())).
            runLocalTask();
    }

    /**
     * Removes and returns, without executing, the next task queued
     * for execution in the current worker thread's local queue or if
     * none, a task stolen from another worker, if one is available.
     * A null return does not necessarily imply that all tasks are
     * completed, only that there are currently none available.
     * @return the next task to execute, or null if none
     */
    public static ForkJoinTask<?> pollTask() {
        return ((ForkJoinWorkerThread)(Thread.currentThread())).
            getLocalOrStolenTask();
    }

    /**
     * Helps this program complete by processing a local, stolen or
     * submitted task, if one is available.  This method may be useful
     * when several tasks are forked, and only one of them must be
     * joined, as in:
     * <pre>
     *   while (!t1.isDone() &amp;&amp; !t2.isDone())
     *     ForkJoinWorkerThread.executeTask();
     * </pre>
     *
     * @return true if a task was run; a false return indicates
     * that no task was available.
     */
    public static boolean executeTask() {
        return ((ForkJoinWorkerThread)(Thread.currentThread())).
            runLocalOrStolenTask();
    }

    /**
     * Executes tasks (but not new submissions) until the pool
     * isQuiescent.
     */
    public static void helpQuiesce() {
        ((ForkJoinWorkerThread)(Thread.currentThread())).
            runUntilQuiescent();
    }

    /**
     * Returns an estimate of how many more locally queued tasks there
     * are than idle worker threads that might steal them.  This value
     * may be useful for heuristic decisions about whether to fork
     * other tasks. In many usages of ForkJoinTasks, at steady state,
     * each worker should aim to maintain a small constant number (for
     * example, 3) stealable tasks, plus more if there are idle
     * workers.
     *
     * <p><b>Sample Usage.</b> Here is a variant version of
     * <tt>compute</tt> for the {@link BinaryAsyncAction} Fib example
     * using getEstimatedSurplusTaskCount to dynamically determine
     * sequential threshold:
     *
     * <pre>
     *   protected void compute() {
     *     Fib f = this;
     *     while (f.n &gt; 1 &amp;&amp;
     *       ForkJoinWorkerThread.getEstimatedSurplusTaskCount() &lt;= 3) {
     *        Fib left = new Fib(f.n - 1);
     *        Fib right = new Fib(f.n - 2);
     *        f.linkSubtasks(left, right);
     *        right.fork(); // fork right
     *        f = left;     // loop on left
     *     }
     *     f.result = sequentiallyComputeFibinacci(f.n);
     *     f.finish();
     *   }
     * }
     * </pre>
     *
     * @return the number of tasks, which is negative if there are
     * fewer tasks than idle workers
     */
    public static int getEstimatedSurplusTaskCount() {
        return ((ForkJoinWorkerThread)(Thread.currentThread()))
            .estimatedSurplusTaskCount();
    }

    final int estimatedSurplusTaskCount() {
        return (sp - base) - pool.getIdleThreadCount();
    }

    /**
     * Removes and returns, without executing, the given task from the
     * queue hosting current execution only if it would be the next
     * task executed by the current worker.  Among other usages, this
     * method may be used to bypass task execution during
     * cancellation.
     *
     * <p><b>Sample Usage,</b> This method may help counterbalance
     * effects of dynamic task thresholding. If using a threshold that
     * typically generates too many tasks, then this method may be
     * used to more cheaply execute excess ones. Here is a dynamically
     * tuned version of the {@link RecursiveAction} Applyer example:
     *
     * <pre>
     * class Applyer extends RecursiveAction {
     *   final double[] array;
     *   final int lo, hi, seqSize;
     *   double result;
     *   Applyer next; // keeps track of right-hand-side tasks
     *   Applyer(double[] array, int lo, int hi, int seqSize, Applyer next) {
     *     this.array = array; this.lo = lo; this.hi = hi;
     *     this.seqSize = seqSize; this.next = next;
     *   }
     *
     *   double atLeaf(int l, int r) {
     *     double sum = 0;
     *     for (int i = l; i &lt; h; ++i) // perform leftmost base step
     *       sum += array[i] * array[i];
     *     return sum;
     *   }
     *
     *   protected void compute() {
     *     int l = lo;
     *     int h = hi;
     *     Applyer right = null;
     *     while (h - l &gt; 1 &amp;&amp;
     *        ForkJoinWorkerThread.getEstimatedSurplusTaskCount() &lt;= 3) {
     *        int mid = (l + h) &gt;&gt;&gt; 1;
     *        right = new Applyer(array, mid, h, seqSize, right);
     *        right.fork();
     *        h = mid;
     *     }
     *     double sum = atLeaf(l, h);
     *     while (right != null &amp;&amp; // direct compute unstolen tasks
     *        ForkJoinWorkerThread.removeIfNextLocalTask(right)) {
     *          sum += right.atLeaf(r.lo, r.hi);
     *          right = right.next;
     *      }
     *     while (right != null) {  // join remaining right-hand sides
     *       right.join();
     *       sum += right.result;
     *       right = right.next;
     *     }
     *     result = sum;
     *   }
     * }
     * </pre>
     *
     * @param task the task
     * @return true if removed
     */
    public static boolean removeIfNextLocalTask(ForkJoinTask<?> task) {
        return ((ForkJoinWorkerThread)(Thread.currentThread())).popIfNext(task);
    }

    // Support for alternate handling of submissions

    /**
     * Removes and returns the next unexecuted submission to the given
     * pool, if one is available. To access a submission from the
     * current worker's pool, use <tt>pollSubmission(getPool())</tt>.
     * This method may be useful for draining tasks during exception
     * recovery and for re-assigning work in systems with multiple
     * pools.
     * @param pool the pool
     * @return the next submission, or null if none
     */
    public static Future<?> pollSubmission(ForkJoinPool pool) {
        return pool.pollSubmission();
    }

    /**
     * If the given argument represents a submission to a ForkJoinPool
     * (normally, one returned by <tt>pollSubmission</tt>), returns
     * the actual task submitted to the pool.  This method may be
     * useful for alternate handling of drained submissions..
     * @param submission the submission
     * @return the underlying task
     * @throws IllegalArgumentException if the given future does
     * not represent a submission to a pool
     */
    public static <V> ForkJoinTask<V> getSubmittedTask(Future<V> submission) {
        try {
            return ((Submission)submission).getSubmittedTask();
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * If the argument represents a submission to a ForkJoinPool
     * (normally, one returned by <tt>pollSubmission</tt>), causes it
     * to be ready with the given value returned upon invocation of
     * its <tt>get()</tt> method, regardless of the status of the
     * underlying ForkJoinTask. This method may be useful for
     * alternate handling of drained submissions..
     * @param submission the submission
     * @param value the result to be returned by the submission
     * @throws IllegalArgumentException if the given future does
     * not represent a submission to a pool
     */
    public static <V> void forceCompletion(Future<V> submission, V value) {
        try {
            ((Submission)submission).finishTask(value);
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * If the argument represents a submission to a ForkJoinPool
     * (normally, one returned by <tt>pollSubmission</tt>), causes it
     * to be ready with the given exception thrown on invocation of
     * its <tt>get()</tt> method, regardless of the status of the
     * underlying ForkJoinTask..This method may be useful for
     * alternate handling of drained submissions..
     * @param submission the submission
     * @param exception the exception to be thrown on access
     * @throws IllegalArgumentException if the exception is
     * not a RuntimeException or Error
     * @throws IllegalArgumentException if the given future does
     * not represent a submission to a pool
     */
    public static <V> void forceCompletionExceptionally(Future<V> submission,
                                                        Throwable exception) {
        if (!(exception instanceof RuntimeException) &&
            !(exception instanceof Error))
            throw new IllegalArgumentException();
        try {
            ((Submission)submission).finishTaskExceptionally(exception);
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException();
        }
    }

    // Per-worker exported random numbers
    // Same constants as java.util.Random
    final static long JURandomMultiplier = 0x5DEECE66DL;
    final static long JURandomAddend = 0xBL;
    final static long JURandomMask = (1L << 48) - 1;

    private final int nextJURandom(int bits) {
        long next = (juRandomSeed * JURandomMultiplier + JURandomAddend) &
            JURandomMask;
        juRandomSeed = next;
        return (int)(next >>> (48 - bits));
    }

    private final int nextJURandomInt(int n) {
        if (n <= 0)
            throw new IllegalArgumentException("n must be positive");
        int bits = nextJURandom(31);
        if ((n & -n) == n)
            return (int)((n * (long)bits) >> 31);

        for (;;) {
            int val = bits % n;
            if (bits - val + (n-1) >= 0)
                return val;
            bits = nextJURandom(31);
        }
    }

    private final long nextJURandomLong() {
        return ((long)(nextJURandom(32)) << 32) + nextJURandom(32);
    }

    private final long nextJURandomLong(long n) {
        if (n <= 0)
            throw new IllegalArgumentException("n must be positive");
        long offset = 0;
        while (n >= Integer.MAX_VALUE) { // randomly pick half range
            int bits = nextJURandom(2); // 2nd bit for odd vs even split
            long half = n >>> 1;
            long nextn = ((bits & 2) == 0)? half : n - half;
            if ((bits & 1) == 0)
                offset += n - nextn;
            n = nextn;
        }
        return offset + nextJURandomInt((int)n);
    }

    private final double nextJURandomDouble() {
        return (((long)(nextJURandom(26)) << 27) + nextJURandom(27))
            / (double)(1L << 53);
    }

    /**
     * Returns a random integer using a per-worker random
     * number generator with the same properties as
     * {@link java.util.Random#nextInt}
     * @return the next pseudorandom, uniformly distributed {@code int}
     *         value from this worker's random number generator's sequence
     */
    public static int nextRandomInt() {
        return ((ForkJoinWorkerThread)(Thread.currentThread())).
            nextJURandom(32);
    }

    /**
     * Returns a random integer using a per-worker random
     * number generator with the same properties as
     * {@link java.util.Random#nextInt(int)}
     * @param n the bound on the random number to be returned.  Must be
     *        positive.
     * @return the next pseudorandom, uniformly distributed {@code int}
     *         value between {@code 0} (inclusive) and {@code n} (exclusive)
     *         from this worker's random number generator's sequence
     * @throws IllegalArgumentException if n is not positive
     */
    public static int nextRandomInt(int n) {
        return ((ForkJoinWorkerThread)(Thread.currentThread())).
            nextJURandomInt(n);
    }

    /**
     * Returns a random long using a per-worker random
     * number generator with the same properties as
     * {@link java.util.Random#nextLong}
     * @return the next pseudorandom, uniformly distributed {@code long}
     *         value from this worker's random number generator's sequence
     */
    public static long nextRandomLong() {
        return ((ForkJoinWorkerThread)(Thread.currentThread())).
            nextJURandomLong();
    }

    /**
     * Returns a random integer using a per-worker random
     * number generator with the same properties as
     * {@link java.util.Random#nextInt(int)}
     * @param n the bound on the random number to be returned.  Must be
     *        positive.
     * @return the next pseudorandom, uniformly distributed {@code int}
     *         value between {@code 0} (inclusive) and {@code n} (exclusive)
     *         from this worker's random number generator's sequence
     * @throws IllegalArgumentException if n is not positive
     */
    public static long nextRandomLong(long n) {
        return ((ForkJoinWorkerThread)(Thread.currentThread())).
            nextJURandomLong(n);
    }

    /**
     * Returns a random double using a per-worker random
     * number generator with the same properties as
     * {@link java.util.Random#nextDouble}
     * @return the next pseudorandom, uniformly distributed {@code double}
     *         value between {@code 0.0} and {@code 1.0} from this
     *         worker's random number generator's sequence
     */
    public static double nextRandomDouble() {
        return ((ForkJoinWorkerThread)(Thread.currentThread())).
            nextJURandomDouble();
    }

    // Temporary Unsafe mechanics for preliminary release

    static final Unsafe _unsafe;
    static final long baseOffset;
    static final long spOffset;
    static final long qBase;
    static final int qShift;
    static {
        try {
            if (ForkJoinWorkerThread.class.getClassLoader() != null) {
                Field f = Unsafe.class.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                _unsafe = (Unsafe)f.get(null);
            }
            else
                _unsafe = Unsafe.getUnsafe();
            baseOffset = _unsafe.objectFieldOffset
                (ForkJoinWorkerThread.class.getDeclaredField("base"));
            spOffset = _unsafe.objectFieldOffset
                (ForkJoinWorkerThread.class.getDeclaredField("sp"));
            qBase = _unsafe.arrayBaseOffset(ForkJoinTask[].class);
            int s = _unsafe.arrayIndexScale(ForkJoinTask[].class);
            if ((s & (s-1)) != 0)
                throw new Error("data type scale not a power of two");
            qShift = 31 - Integer.numberOfLeadingZeros(s);
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize intrinsics", e);
        }
    }

}
