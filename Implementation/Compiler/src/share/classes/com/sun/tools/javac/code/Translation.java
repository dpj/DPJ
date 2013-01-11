package com.sun.tools.javac.code;

import java.util.Iterator;

import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

/**
 * Interfaces for translation via type, region, and effect 
 * substitution.
 * 
 * @author Rob Bocchino
 */
public class Translation {

    public static abstract class Subst<Elt,From,To> {
	public abstract Elt subst(Elt elt, From from, To to);
	public Elt substIterable(Elt elt, Iterable<From> from, Iterable<To> to) {
	    Iterator<From> fromIterator = from.iterator();
	    Iterator<To> toIterator = to.iterator();
	    while (fromIterator.hasNext() && toIterator.hasNext()) {
		elt = subst (elt, fromIterator.next(), toIterator.next());
	    }
	    return elt;
	}
    }

    public static <Elt,From,To> Elt 
    	subst(Subst<Elt,From,To> s, Elt elt, From from, To to) 
    {
	return s.subst(elt,from,to);
    }
    
    public static <Elt,From,To> Elt 
	substIterable(Subst<Elt,From,To> s, Elt elt, 
		Iterable<From> from, Iterable<To> to) 
    {
	return s.substIterable(elt,from,to);
    }
    
    public static <Elt,From,To> List<Elt> substIntoList
    	(Subst<Elt,From,To> s, List<Elt> elts, From from, To to) 
    {
	ListBuffer<Elt> lb = ListBuffer.lb();
	for (Elt elt : elts) {
	    lb.append(s.subst(elt, from, to));
	}
	return lb.toList();
    }

    public static <Elt,From,To> List<Elt> substListIntoList
    	(Subst<Elt,From,To> s, List<Elt> elts, List<From> from, List<To> to)
    {
	ListBuffer<Elt> lb = ListBuffer.lb();
	for (Elt elt : elts) {
	    lb.append(s.substIterable(elt, from, to));
	}
	return lb.toList();
    }

    /** Interface for representing RPL substitutions */
    public interface SubstRPLs<T extends SubstRPLs<T>> {
	
	/** Substitute 'to' RPLs for 'from' RPLs */
	public T substRPLParams(Iterable<RPL> from, Iterable<RPL> to);
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