package com.sun.tools.javac.code;

import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Pair;

/** A class for representing RPL and effect constraints
 */
public class Constraints {
    public Constraints(List<Pair<RPL,RPL>> disjointRPLs, 
	    List<Pair<Effects,Effects>> noninterferingEffects) {
	this.disjointRPLs = disjointRPLs;
	this.noninterferingEffects = noninterferingEffects;
    }
    public Constraints() {}
    public List<Pair<RPL,RPL>> disjointRPLs = List.nil();
    public List<Pair<Effects,Effects>> noninterferingEffects = List.nil();
    // We can add others here as necessary, like inclusion or subeffects
}