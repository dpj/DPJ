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
 * Resultless ForkJoinTasks with explicit completions.  Unlike
 * RecursiveActions, AsyncActions do not intrinisically complete upon
 * exit from their <tt>compute</tt> methods, but instead require
 * explicit invocation of their <tt>finish</tt> methods. This class is
 * not directly subclassable outside of this package. Instead, you can
 * subclass one of the supplied abstract classes that support various
 * styles of asynchronous processing.
 */
public abstract class AsyncAction extends ForkJoinTask<Void> {
    /**
     * Disallow direct construction outside this package.
     */
    AsyncAction() {
    }

    /**
     * The asynchronous part of the computation performed by this
     * task.  While you must define this method, you should not call
     * it directly unless this task will never be forked or joined.
     * If this method throws an exception,
     * <tt>finishExceptionally</tt> is immediately invoked.
     */
    protected abstract void compute();

    /**
     * Completes this task, and if not already aborted or cancelled,
     * returning a <tt>null</tt> result upon <tt>join</tt> and related
     * operations. This method may be invoked only from within other
     * ForkJoinTask computations. This method may be used to provide
     * results, mainly for asynchronous tasks. Upon invocation, the
     * task itself, if running, must exit (return) in a small finite
     * number of steps.
     */
    public abstract void finish();

    /**
     * Completes this task abnormally, and if not already aborted or
     * cancelled, causes it to throw the given exception upon
     * <tt>join</tt> and related operations. Upon invocation, the task
     * itself, if running, must exit (return) in a small finite number
     * of steps.
     * @param ex the unchecked exception (RuntimeException or Error)
     * to throw.
     * @throws IllegalArgumentException if the given exception is not
     * a RuntimeException or Error.
     */
    public abstract void finishExceptionally(Throwable ex);

    /**
     * Always returns null.
     * @return null
     */
    public final Void rawResult() {
        return null;
    }
    
}
