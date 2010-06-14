package com.sun.source.tree;

/**
 * A tree node for a 'nonint' statement.
 *
 * For example:
 * <pre>
 *   nonint <em>statement</em>
 * </pre>
 *
 * @author Rob Bocchino
 */
public interface NonintTree extends StatementTree {
    StatementTree getStatement();
}
