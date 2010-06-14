package com.sun.source.tree;

/**
 * A tree node for a 'finish' statement.
 *
 * For example:
 * <pre>
 *   finish <em>statement</em>
 * </pre>
 *
 * @author Rob Bocchino
 */
public interface FinishTree extends StatementTree {
    StatementTree getStatement();
}
