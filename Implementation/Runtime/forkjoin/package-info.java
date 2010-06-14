/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */


/**
 * A fine-grained parallel computation framework. ForkJoinTasks and
 * their related support classes provide a very efficient basis for
 * obtaining platform-independent parallel speed-ups of
 * computation-intensive operations.  They are not a full substitute
 * for the kinds of arbitrary processing supported by Executors or
 * Threads. However, when applicable, they typically provide
 * significantly greater performance on multiprocessor platforms.
 *
 * <p> Candidates for fork/join processing mainly include those that
 * can be expressed using parallel divide-and-conquer techniques: To
 * solve a problem, break it in two (or more) parts, and then solve
 * those parts in parallel, continuing on in this way until the
 * problem is too small to be broken up, so is solved directly.  The
 * underlying <em>work-stealing</em> framework makes subtasks
 * available to other threads (normally one per CPU), that help
 * complete the tasks.  In general, the most efficient ForkJoinTasks
 * are those that directly implement this algorithmic design pattern.
 *
 * <p>While direct implementation of parallel divide-and-conquer
 * algorithms is often straightforward, it can also be tedious and
 * code-intensive. For this reason, a number of solution "templates"
 * are available for common kinds of operations on lists and arrays:
 * applying some operation to all elements, combining elements
 * according to some function, and so on.  In this preliminary
 * release, these are presented via some interfaces describing the
 * associated code bodies in TaskTypes, along with an evolving set of
 * implementations for lists and arrays of objects and scalars.
 */
package jsr166y.forkjoin;
