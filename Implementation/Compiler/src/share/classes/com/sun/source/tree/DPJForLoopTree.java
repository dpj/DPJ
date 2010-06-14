package com.sun.source.tree;

/**
 * A tree node for a 'dpjfor' statement.
 *
 * For example:
 * <pre>
 *   finish <em>statement</em>
 * </pre>
 *
 * @author Rob Bocchino
 */
public interface DPJForLoopTree extends StatementTree {
    VariableTree getVariable();
    ExpressionTree getStart();
    ExpressionTree getLength();
    ExpressionTree getStride();
    StatementTree getStatement();
}
