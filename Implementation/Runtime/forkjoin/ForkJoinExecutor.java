/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

package jsr166y.forkjoin;
import java.util.concurrent.*;

/**
 * An object that executes {@link ForkJoinTask} computations.  This
 * interface does not expose lifecycle, status, or management methods
 * corresponding to implementations, so may be useful as a basis
 * for classes that must restrict access to such methods.
 *
 */
public interface ForkJoinExecutor {
    /**
     * Arranges for (asynchronous) execution of the given task.
     * @param task the task
     * @throws NullPointerException if task is null
     * @throws RejectedExecutionException if the executor is
     * not in a state that allows execution.
     */
    public <T> void execute(ForkJoinTask<T> task);

    /**
     * Performs the given task; returning its result upon completion
     * @param task the task
     * @return the task's result
     * @throws NullPointerException if task is null
     * @throws RejectedExecutionException if the executor is
     * not in a state that allows execution.
     */
    public <T> T invoke(ForkJoinTask<T> task);

    /**
     * Arranges for (asynchronous) execution of the given task,
     * returning a <tt>Future</tt> that may be used to obtain results
     * upon completion.
     * @param task the task
     * @return a Future that can be used to get the task's results.
     * @throws NullPointerException if task is null
     * @throws RejectedExecutionException if the executor is
     * not in a state that allows execution.
     */
    public <T> Future<T> submit(ForkJoinTask<T> task);

    /**
     * Returns an estimate of how many tasks (including subtasks)
     * may execute at once. This value normally corresponds to the
     * number of threads available for executing tasks by this
     * executor.
     * @return the parallelism level
     */
    public int getParallelismLevel();
}

