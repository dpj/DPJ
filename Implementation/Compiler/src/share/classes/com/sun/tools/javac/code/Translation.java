package com.sun.tools.javac.code;

import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

/**
 * Interfaces for translation via type, region, and ref group 
 * substitution.
 * 
 * @author Rob Bocchino
 */
public class Translation {

    /** Interface for representing RPL substitutions */
    public interface SubstRPLs<T extends SubstRPLs<T>> {
	
	/** 'this' after substituting 'to' RPLs for 'from' RPLs */
	public T substRPLs(List<RPL> from, List<RPL> to);
	
    }

    /** Apply RPL substitution to a list of things */
    public static <T extends SubstRPLs<T>> List<T>substRPLs(List<T> list,
	    List<RPL> from, List<RPL> to) {
	ListBuffer<T> lb = ListBuffer.lb();
	for (T elt : list) lb.append(elt.substRPLs(from, to));
	return lb.toList();
    }

}