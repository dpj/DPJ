/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 *
 */

package jsr166y.forkjoin;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * A minimal skeleton for AsyncActions.  Instances of BasicAsyncAction
 * maintain a single atomic integer task state that may be used to
 * control activities. Task state is initially zero, or negative if
 * this task has been cancelled. Once negative, the value cannot be
 * changed.
 */
public abstract class BasicAsyncAction extends AsyncAction {
    /**
     * Gets the task state, which is initially zero, or negative if
     * this task has been cancelled. Once negative, the value cannot
     * be changed.
     * @return task state
     */
    protected final int getTaskState() {
        return status;
    }

    /**
     * Atomically sets the task state to the given nonnegative updated
     * value if the current value is non-negative and equal to the
     * expected value.
     * @param expect the expected value
     * @param update the new value
     * @return true if successful
     */
    protected final boolean compareAndSetTaskState(int expect, int update) {
        return update >= 0 && casStatus(expect, update);
    }

    /**
     * Sets the task state to the given nonnegative value, assuming
     * that no other thread is concurrently accessing this task.  The
     * effects under any other usage are undefined. This method is
     * typically only useful for initializing state values.
     * @param value the new value
     * @throws IllegalArgumentException if value negative
     */
    protected final void setTaskState(int value) {
        if (value < 0) throw new IllegalArgumentException();
        setStatus(value);
    }

    /**
     * Sets task state to indicate complation
     */
    public void finish() {
        setDone();
    }

    /**
     * Sets task state to indicate complation with the given exception
     */
    public void finishExceptionally(Throwable ex) {
        checkedSetDoneExceptionally(ex);
    }

    final boolean exec() {
        if (status >= 0) {
            try {
                compute();
            } catch(Throwable rex) {
                setDoneExceptionally(rex);
            }
        }
        return false;
    }

}
