package com.sun.tools.javac.code;

import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Pair;

/** A class for representing RPL and effect constraints
 */
public class Constraints {
    public List<Pair<RPL,RPL>> disjointRPLs = List.nil();
    public List<Pair<Effect,Effect>> noninterferingEffects = List.nil();
    // We can add others here as necessary, like inclusion or subeffects
}