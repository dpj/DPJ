/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 *
 */

package jsr166y.forkjoin;

/**
 * AsyncActions that are always linked in binary parent-child
 * relationships. Compared to Recursive tasks, BinaryAsyncActions may
 * have smaller stack space footprints and faster completion mechanics
 * but higher per-task footprints. Compared to LinkedAsyncActions,
 * BinaryAsyncActions are simpler to use and have less overhead in
 * typical uasges but are restricted to binary computation trees.
 *
 * <p> Upon construction, an BinaryAsyncAction does not bear any
 * linkages. For non-root tasks, links must be established using
 * method <tt>linkSubtasks</tt> before use.
 *
 * <p> <b>Sample Usage.</b>  A version of Fibonacci:
 * <pre>
 * class Fib extends BinaryAsyncAction {
 *   final int n;
 *   int result;
 *   Fib(int n) { this.n = n; }
 *   protected void compute() {
 *     if (n &gt; 1) {
 *        linkAndForkSubtasks(new Fib(n-1), new Fib(n-2));
 *     else {
 *        result = n; // fib(0)==0; fib(1)==1
 *        finish();
 *     }
 *   }
 *   protected void onFinish(BinaryAsyncAction x, BinaryAsyncAction y) {
 *      result = ((Fib)x).result + ((Fib)y).result;
 *   }
 * }
 * </pre>
 * An alternative, and usually faster strategy is to instead use a
 * loop to fork subtasks:
 * <pre>
 *   protected void compute() {
 *     Fib f = this;
 *     while (f.n &gt; 1) {
 *        Fib left = new Fib(f.n - 1);
 *        Fib right = new Fib(f.n - 2);
 *        f.linkSubtasks(left, right);
 *        right.fork(); // fork right
 *        f = left;     // loop on left
 *     }
 *     f.result = f.n;
 *     f.finish();
 *   }
 * }
 * </pre>
 */
public abstract class BinaryAsyncAction extends AsyncAction {
    /**
     * Parent to propagate completion; nulled after completion to
     * avoid retaining entire tree as garbage
     */
    private BinaryAsyncAction parent;

    /**
     * Sibling to access on subtask joins, also nulled after completion.
     */
    private BinaryAsyncAction sibling;

    /*
     * Note: we also piggyback one bit join count on
     * ForkJoinTask.status field. The first arriving thread CAS's
     * status from 0 to 1. The second ultimately sets to negative
     * value to signify completion.
     */

    /**
     * Creates a new action. Unless this is a root task, you will need
     * to link it using method <tt>linkSubtasks</tt> before forking as
     * a subtask.
     */
    protected BinaryAsyncAction() {
    }

    /**
     * Establishes links for the given tasks to have the current task
     * as parent, and each other as siblings.
     * @param x one subtask
     * @param y the other subtask
     * @throws NullPointerException if either argument is null.
     */
    public final void linkSubtasks(BinaryAsyncAction x, BinaryAsyncAction y) {
        x.parent = y.parent = this;
        x.sibling = y;
        y.sibling = x;
    }

    /**
     * Overridable callback action triggered upon <tt>finish</tt> of
     * subtasks.  Upon invocation, both subtasks have completed.
     * After return, this task <tt>isDone</tt> and is joinable by
     * other tasks. The default version of this method does
     * nothing. But it may may be overridden in subclasses to perform
     * some action (for example a reduction) when this task is
     * completes.
     * @param x one subtask
     * @param y the other subtask
     */
    protected void onFinish(BinaryAsyncAction x, BinaryAsyncAction y) {
    }

    /**
     * Overridable callback action triggered by
     * <tt>finishExceptionally</tt>.  Upon invocation, this task has
     * aborted due to an exception (accessible via
     * <tt>getException</tt>). If this method returns <tt>true</tt>,
     * the exception propagates to the current task's
     * parent. Otherwise, normal completion is propagated.  The
     * default version of this method does nothing and returns
     * <tt>true</tt>.
     * @return true if this task's exception should be propagated to
     * this tasks parent.
     */
    protected boolean onException() {
        return true;
    }

    /**
     * Equivalent in effect to invoking <tt>linkSubtasks</tt> and then
     * forking both tasks.
     * @param x one subtask
     * @param y the other subtask
     */
    public void linkAndForkSubtasks(BinaryAsyncAction x, BinaryAsyncAction y) {
        linkSubtasks(x, y);
        y.fork();
        x.fork();
    }

    /**
     * Completes this task, and if this task has a sibling that is
     * also complete, invokes <tt>onFinish</tt> of parent task, and so
     * on. If an exception is encountered, tasks instead
     * <tt>finishExceptionally</tt>.
     */
    public final void finish() {
        // todo: Use removeIfNextLocalTask(s) without possibly blowing stack
        BinaryAsyncAction a = this;
        for (;;) {
            BinaryAsyncAction s = a.sibling;
            BinaryAsyncAction p = a.parent;
            a.sibling = null;
            a.parent = null;
            a.setDone();
            if (p == null || p.casStatus(0, 1))
                break;
            try {
                p.onFinish(a, s);
            } catch(Throwable rex) {
                p.doFinishExceptionally(rex);
                return;
            }
            a = p;
        }
    }

    /**
     * Completes this task abnormally. Unless this task already
     * cancelled or aborted, upon invocation, this method invokes
     * <tt>onException</tt>, and then, depending on its return value,
     * finishes parent (if one exists) exceptionally or normally.  To
     * avoid unbounded exception loops, this method aborts if an
     * exception is encountered in any <tt>onException</tt>
     * invocation.
     * @param ex the exception to throw when joining this task
     * @throws NullPointerException if ex is null
     * @throws Throwable if any invocation of
     * <tt>onException</tt> does so.
     */
    public final void finishExceptionally(Throwable ex) {
        if (!(ex instanceof RuntimeException) && !(ex instanceof Error))
            throw new IllegalArgumentException(ex);
        doFinishExceptionally(ex);
    }

    /**
     * Internal version without argument screening
     */
    private void doFinishExceptionally(Throwable ex) {
        BinaryAsyncAction a = this;
        while (a.status != ForkJoinTask.HAS_EXCEPTION) {
            a.setDoneExceptionally(ex);
            BinaryAsyncAction s = a.sibling;
            if (s != null)
                s.cancel();
            if (!a.onException() || (a = a.parent) == null)
                break;
        }
    }

    /**
     * Returns this task's parent, or null if none or this task
     * is already finished.
     * @return this task's parent, or null if none.
     */
    public final BinaryAsyncAction getParent() {
        return parent;
    }

    /**
     * Returns this task's sibling, or null if none or this task is
     * already finished.
     * @return this task's sibling, or null if none.
     */
    public BinaryAsyncAction getSibling() {
        return sibling;
    }

    /**
     * Resets the internal bookkeeping state of this task, erasing
     * parent and child linkages.
     */
    public void reinitialize() {
        parent = sibling = null;
        super.reinitialize();
    }

    final boolean exec() {
        if (status >= 0) {
            try {
                compute();
            } catch(Throwable rex) {
                doFinishExceptionally(rex);
            }
        }
        return false;
    }
}
