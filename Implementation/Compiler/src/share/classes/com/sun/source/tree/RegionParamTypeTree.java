package com.sun.source.tree;

import java.util.List;

/**
 * A tree node for a type expression involving region parameters.
 *
 * For example:
 * <pre>
 *   <em>type</em> &lt&lt; <em>Region Path Lists</em> &gt&gt;
 * </pre>
 *
 * @author Robert Bocchino
 */
public interface RegionParamTypeTree extends Tree {
    Tree getType();
    List<? extends Tree> getRegionArguments();
}
