package com.sun.source.tree;

import java.util.List;
import javax.lang.model.element.Name;

/**
 * A tree node for a region parameter.
 *
 * For example:
 * <pre>
 *   <em>name</em> 
 * 
 *   <em>name</em> under <em>bounds</em>
 * </pre>
 *
 * @author Rob Bocchino
 */
public interface RegionParameterTree extends Tree {
    Name getName();
    Tree getBound();
}
