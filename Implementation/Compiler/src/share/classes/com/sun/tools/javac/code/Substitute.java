package com.sun.tools.javac.code;

import static com.sun.tools.javac.code.Kinds.VAR;

import java.util.Iterator;

import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.Resolve;
import com.sun.tools.javac.tree.JCTree.JCArrayAccess;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

/**
 * Interfaces for performing type, region, and effect substitutions.
 * 
 * @author Rob Bocchino
 */
public class Substitute {

    /**
     * Class representing a substitution
     * @author Rob Bocchino
     *
     * @param <Elt>  Type of element to substitute into
     * @param <From> Type of parameter
     * @param <To>   Type of argument
     */
    public static abstract class Subst<Elt,From,To> {
	/**
	 * A basic substitution
	 * @param elt  Element on which to perform substitution
	 * @param from Parameter of substitution
	 * @param to   Argument of substitution
	 * @return     elt after substituting arg for param
	 */
	public abstract Elt basic(Elt elt, From from, To to);
	/**
	 * Iterated substitution.  The default implemementation is to apply
	 * 'basic' for each pair of elements from 'from' and 'to'.
	 * @param elt  Element on which to perform substitution
	 * @param from Parameters of substitution
	 * @param to   Arguments of substitution
	 * @return     elt after substituting args for params
	 */
	public Elt iterable(Elt elt, Iterable<From> from, Iterable<To> to) {
	    Iterator<From> fromIterator = from.iterator();
	    Iterator<To> toIterator = to.iterator();
	    while (fromIterator.hasNext() && toIterator.hasNext()) {
		elt = basic (elt, fromIterator.next(), toIterator.next());
	    }
	    return elt;
	}
    }

    /**
     * Perform substitution using iterable collections of params and args.
     * @param subst Substitution operation
     * @param elt   Element on which to perform substitution
     * @param from  Params of substitution
     * @param to    Args of substitution
     * @return      Result of substitution
     */
    public static <Elt,From,To> Elt 
	iterable(Subst<Elt,From,To> subst, Elt elt, 
		Iterable<From> from, Iterable<To> to) 
    {
	return subst.iterable(elt,from,to);
    }

    /**
     * Substitute single param/arg pair into a list
     */
    public static <Elt,From,To> List<Elt> intoList
    	(Subst<Elt,From,To> subst, List<Elt> elts, From from, To to) 
    {
	ListBuffer<Elt> lb = ListBuffer.lb();
	for (Elt elt : elts) {
	    lb.append(subst.basic(elt, from, to));
	}
	return lb.toList();
    }

    /**
     * Substitute iterable collections of params and args into list
     */
    public static <Elt,From,To> List<Elt> listIntoList
    	(Subst<Elt,From,To> subst, List<Elt> elts, List<From> from, List<To> to)
    {
	ListBuffer<Elt> lb = ListBuffer.lb();
	for (Elt elt : elts) {
	    lb.append(subst.iterable(elt, from, to));
	}
	return lb.toList();
    }

    /** Interface representing RPL substitutions */
    public interface SubstRPLs<T extends SubstRPLs<T>> {	
	/** Substitute 'to' RPLs for 'from' RPLs */
	public T substRPLParams(Iterable<RPL> from, Iterable<RPL> to);
	/** Do TR param substitutions implied by type bindings */
	public T substTRParams(Iterable<Type> from, Iterable<Type> to);
	/** Substitute an RPL for a variable */
	public T substRPLForVar(VarSymbol from, RPL to);
    }

    /** Apply RPL and TR param substitution to an element */
    public static <T extends SubstRPLs<T>> T allRPLParams
    	(T elt, Type t)
    {
	if (elt == null || t == null) return elt;
	T result = elt.substRPLParams(t.tsym.type.getRPLArguments(),
		t.getRPLArguments());
	result = result.substTRParams(t.tsym.type.getTypeArguments(),
		t.getTypeArguments());
	return result;	
    }
    
    /** Interface representing 'as member of' translation */
    public interface AsMemberOf<T extends AsMemberOf<T>> {
	
	/** 'this' as a member of type t */
	public T asMemberOf(Type t, Types types);
	
    }

    /** Interface representing index substitution */
    public interface SubstIndex<T extends SubstIndex<T>> {
	/** 'this' after substituting index */
	public T substIndex(VarSymbol from, JCExpression to); 
    }
    
    /** Interface representing substitution of RPL for var */
    public interface SubstRPLForVar<T extends SubstRPLForVar<T>> {
	/** 'this' after substituting RPL for var */
	public T substRPLForVar(VarSymbol from, RPL to);
    }

    
}