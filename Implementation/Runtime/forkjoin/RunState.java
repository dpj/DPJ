/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

package jsr166y.forkjoin;
import java.util.concurrent.atomic.*;

/**
 * Maintains lifecycle control for pool and workers.
 * Opportunistically subclasses AtomicInteger. Don't directly use the
 * AtomicInteger methods though.
 */
final class RunState extends AtomicInteger {
    // Order among values matters
    static final int RUNNING    = 0;
    static final int SHUTDOWN   = 1;
    static final int STOPPING   = 2;
    static final int TERMINATED = 4;

    boolean isRunning()              { return get() == RUNNING; }
    boolean isShutdown()             { return get() == SHUTDOWN; }
    boolean isStopping()             { return get() == STOPPING; }
    boolean isTerminated()           { return get() == TERMINATED; }
    boolean isAtLeastShutdown()      { return get() >= SHUTDOWN; }
    boolean isAtLeastStopping()      { return get() >= STOPPING; }
    boolean transitionToShutdown()   { return transitionTo(SHUTDOWN); }
    boolean transitionToStopping()   { return transitionTo(STOPPING); }
    boolean transitionToTerminated() { return transitionTo(TERMINATED); }

    /**
     * Transition to at least the given state. Return true if not
     * already at least given state.
     */
    private boolean transitionTo(int state) {
        for (;;) {
            int s = get();
            if (s >= state)
                return false;
            if (compareAndSet(s, state))
                return true;
        }
    }
}
