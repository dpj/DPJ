package com.sun.source.tree;

/**
 * A tree node for an 'atomic' statement.
 *
 * For example:
 * <pre>
 *   atomic <em>statement</em>
 * </pre>
 *
 * @author Rob Bocchino
 */
public interface AtomicTree extends StatementTree {
    StatementTree getStatement();
}
