package com.sun.source.tree;

/**
 * A tree node for a spawn expression.
 *
 * For example:
 * <pre>
 *   <em>spawn expression</em>
 * </pre>
 */
public interface SpawnTree extends StatementTree {
    StatementTree getStatement();
}
