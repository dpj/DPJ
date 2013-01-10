package com.sun.tools.javac.code;

import com.sun.tools.javac.code.RPLElement.VarRPLElement;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.tree.JCTree.JCExpression;
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
	
	/** Substitute 'to' RPLs for 'from' RPLs */
	public T substRPLParams(List<RPL> from, List<RPL> to);
	/** Do TR param substitutions implied by type bindings */
	public T substTRParams(List<Type> from, List<Type> to);
	/** Substitute an RPL for a variable */
	public T substRPLForVar(VarSymbol from, RPL to);
    }

    public static <T extends SubstRPLs<T>> List<T>substRPLsForVars
    	(List<T> in, List<VarSymbol> from, List<RPL> to)
    {
	ListBuffer<T> lb = ListBuffer.lb();
	for (T elt : in) {
	    for (VarSymbol var : from) {
		lb.append(elt.substRPLForVar(var, to.head));
		to = to.tail;
	    }
	}	
	return lb.toList();
    }
    
    /** Apply RPL substitution to a list */
    public static <T extends SubstRPLs<T>> List<T>substRPLs(List<T> list,
	    List<RPL> from, List<RPL> to) {
	ListBuffer<T> lb = ListBuffer.lb();
	for (T elt : list) lb.append(elt.substRPLParams(from, to));
	return lb.toList();
    }
    
    /** Apply TR param substitution to a list */
    public static <T extends SubstRPLs<T>> List<T>substTRParams
    	(List<T> list, List<Type> from, List<Type> to)
    {
	ListBuffer<T> lb = ListBuffer.lb();
	for (T elt : list) lb.append (elt.substTRParams(from, to));
	return lb.toList();		
    }	

    /** Apply RPL and TR param substitution to an element */
    public static <T extends SubstRPLs<T>> T substAllRPLParams
    	(T elt, Type t)
    {
	T result = elt.substRPLParams(t.tsym.type.getRPLArguments(),
		t.getRPLArguments());
	result = result.substTRParams(t.tsym.type.getTypeArguments(),
		t.getTypeArguments());
	return result;	
    }
    
    /** Apply RPL and TR param substitution to a list */
    public static <T extends SubstRPLs<T>> List<T> substAllRPLParamsList
    	(List<T> list, Type t)
    {
	ListBuffer<T> lb = ListBuffer.lb();
	for (T elt : list) lb.append (substAllRPLParams(elt,t));
	return lb.toList();
    }
    
    /** Interface for representing 'as member of' translation */
    public interface AsMemberOf<T extends AsMemberOf<T>> {
	
	/** 'this' as a member of type t */
	public T asMemberOf(Type t, Types types);
	
    }

    /** Apply 'as member of' to a list of things */
    public static <T extends AsMemberOf<T>> List<T>asMemberOf(List<T> list,
	    Types types, Type t) {
	ListBuffer<T> lb = ListBuffer.lb();
	for (T elt : list) lb.append(elt.asMemberOf(t, types));
	return lb.toList();
    }
    
    /** Interface for representing variable substitutions */
    public interface SubstVars<T extends SubstVars<T>> {
	
	/** 'this' after substituting 'to' expressions for 'from' vars */
	public T substVars(List<VarSymbol> from, List<JCExpression> to);
	
    }
    
    /** Apply var-expr substitution to a list of things */
    public static <T extends SubstVars<T>> List<T>substVars 
    	(List<T> list, List<VarSymbol> from, List<JCExpression> to) 
    {
	ListBuffer<T> lb = ListBuffer.lb();
	for (T elt : list) lb.append(elt.substVars(from, to));
	return lb.toList();
    }
    
    
}