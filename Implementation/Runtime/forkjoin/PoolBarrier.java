/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

package jsr166y.forkjoin;
import java.util.concurrent.locks.*;
import java.util.concurrent.atomic.*;

/**
 * A variant of a cyclic barrier that is advanced upon explicit
 * signals representing event occurrences.
 */
final class PoolBarrier {
    /**
     * Wait nodes for Treiber stack representing wait queue.
     */
    static final class QNode {
        QNode next;
        volatile Thread thread; // nulled to cancel wait
        final long count;
        QNode(long c) {
            count = c;
            thread = Thread.currentThread();
        }
        final boolean signal() {
            Thread t = thread;
            if (t == null)
                return false;
            thread = null;
            LockSupport.unpark(t);
            return true;
        }
    }

    /**
     * The event count
     */
    final AtomicLong counter = new AtomicLong();

    /**
     * Head of Treiber stack. Even though this variable is very
     * busy, it is not usually heavily contended because of
     * signal/wait/release policies.
     */
    final AtomicReference<QNode> head = new AtomicReference<QNode>();

    /**
     * Returns the current event count
     */
    final long getCount() {
        return counter.get();
    }

    /**
     * Waits until event count advances from count, or some thread is
     * waiting on a previous count. Help wake up others on release.
     * @param prev previous value returned by sync (or 0)
     * @return current event count
     */
    final long sync(long prev) {
        long count = counter.get();
        if (count != prev || enqAndWait(count))
            releaseAll();
        return count;
    }

    /**
     * Increment event count and release waiting threads.
     */
    final void signal() {
        counter.incrementAndGet();
        releaseAll();
    }

    /**
     * Try to increment event count and release a waiting thread, if
     * one exists (released threads will in turn wake up
     * others). Allows repeated invocation by caller to recheck need
     * for signal on contention.
     * @return true if successful
     */
    final boolean trySignal() {
        QNode q;
        final AtomicReference<QNode> hd = this.head;
        final AtomicLong ctr = this.counter;
        long c = ctr.get();
        boolean inc = ctr.compareAndSet(c, c+1);
        // Even if CAS fails, we know that count is now > c, so help release
        while ((q = hd.get()) != null && q.count <= c) {
            if (hd.compareAndSet(q, q.next) && q.signal())
                break;
        }
        return inc;
    }

    /**
     * Enqueue, block and wait for signal
     * @return true if counter advanced, else false (on spurious wakeup)
     */
    private final boolean enqAndWait(long count) {
        final AtomicReference<QNode> hd = this.head;
        final AtomicLong ctr = this.counter;
        QNode node = null; // delay construction until first check
        QNode h;
        while (((h = hd.get()) == null || h.count == count) &&
               ctr.get() == count) {
            if (node == null)
                node = new QNode(count);
            else if (hd.compareAndSet(node.next = h, node)) {
                while (!Thread.interrupted() && node.thread != null &&
                       ctr.get() == count)
                    LockSupport.park(this);
                node.thread = null;
                if (ctr.get() == count) // premature wake up
                    return false;       // don't release others below
                break;
            }
        }
        return true;
    }

    /**
     * Release all waiting threads. Called on exit from sync, as well
     * as on contention in signal. Regardless of why sync'ing threads
     * exit, other waiting threads must also recheck for tasks or
     * completions before resync. Release by chopping off entire list,
     * and then signalling. This both lessens contention and avoids
     * unbounded enq/deq races.  
     */
    private final void releaseAll() {
        AtomicReference<QNode> hd = this.head;
        QNode q;
        while ( (q = hd.get()) != null) {
            if (hd.compareAndSet(q, null)) {
                do {
                    q.signal();
                } while ((q = q.next) != null);
                break;
            }
        }
    }

}
