package com.sun.source.tree;

import java.util.List;

import com.sun.tools.javac.tree.JCTree.DPJRegionPathList;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.util.Pair;


/**
 * A tree node for region parameter info.
 *
 * @author Rob Bocchino
 */
public interface ParamInfoTree extends Tree {
    List<? extends Tree> getParams();
    List<Pair<DPJRegionPathList,DPJRegionPathList>> getConstraints();
}
