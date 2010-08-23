package com.sun.tools.javac.code;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.sun.tools.javac.code.Effect.VariableEffect;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.RegionParameterSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.Resolve;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCTreeWithEffects;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Pair;

/**
 * A collection class representing a set of effects.
 */
public class Effects implements Iterable<Effect> {
    private Set<Effect> effects = new HashSet<Effect>();
    
    public static final Effects UNKNOWN = new Effects();
    
    public Effects() {}
    
    public Effects(Effect effect) {
	add(effect);
    }
    
    /** If this effect collection represents a single effect variable, extract the
     *  variable; otherwise return null.
     */
    public VariableEffect asVariableEffect() {
	if (effects.size() != 1) return null;
	Effect effect = effects.iterator().next();
	return (effect instanceof VariableEffect) ?
		(VariableEffect) effect : null;
    }
    
    public void add(Effect effect) {
	effects.add(effect);
    }

    public void addAll(Effects otherEffects) {
	for (Effect e : otherEffects)
	    this.add(e);
    }

    public void addAllEffects(List<? extends JCTreeWithEffects> list) {
	for (List<? extends JCTreeWithEffects> l = list; l.tail != null; l = l.tail)
	    this.addAll(l.head.effects);
    }

    public boolean isEmpty() {
	return effects.isEmpty();
    }
    
    /** @return an arbitrary effect from this set, or <code>null</code> if it is empty */
    public Effect first() {
	Iterator<Effect> it = effects.iterator();
	if (it.hasNext())
	    return it.next();
	else
	    return null;
    }
    
    /** @return a new Effects set that is a duplicate of this one without the given Effect */
    public Effects without(Effect e) {
	Effects result = new Effects();
	for (Effect effect : effects)
	    if (!effect.equals(e))
		result.add(effect);
	return result;
    }

    
    /**
     * Do all the RPL and effect parameter substitutions implied by the bindings of t
     */
    public Effects substForAllParams(Type t) {
	Effects result = new Effects();
	for (Effect e : effects) {
	    result.addAll(e.substForAllParams(t));
	}
	return result;
    }
    
    /** @return a new Effects set where instances of the given RegionParameterSymbols
     * have been replaced respectively with the given RPLs */
    public Effects substForRegionParams(List<RegionParameterSymbol> from, List<RPL> to) {
	Effects result = new Effects();
	for (Effect e : effects) {
	    result.add(e.substForParams(from, to));
	}
	return result;
    }
    
    public Effects substForTRParams(List<Type> from, List<Type> to) {
	Effects result = new Effects();
	for (Effect e : effects) {
	    result.add(e.substForTRParams(from, to));
	}
	return result;	
    }
    
    public static List<Effects> substForParams(List<Effects> list, 
		List<RegionParameterSymbol> from, 
		List<RPL> to) {
	ListBuffer<Effects> buf = ListBuffer.lb();
	for (Effects effects : list) {
	    buf.append(effects.substForRegionParams(from, to));
	}
	return buf.toList();
    }

    public Effects substForEffectVars(List<Effects> from, List<Effects> to) {
	Effects result = new Effects();
	for (Effect e : effects) {
	    result.addAll(e.substForEffectVars(from, to));
	}
	return result;
    }
    
    public static List<Effects> substForEffectVars(List<Effects> effects, 
		List<Effects> from, List<Effects> to) {
	ListBuffer<Effects> buf = new ListBuffer<Effects>();
	while (effects.nonEmpty()) {
	    buf.append(effects.head.substForEffectVars(from, to));
	    effects = effects.tail;
	}
	return buf.toList();
    }

    
    public Effects substForThis(RPL rpl) {
	Effects result = new Effects();
	for (Effect e : effects) {
	    result.add(e.substForThis(rpl));
	}
	return result;
    }
    
    public static List<Effects> substForThis(List<Effects> list, RPL rpl) {
	ListBuffer<Effects> buf = ListBuffer.lb();
	for (Effects effects : list) {
	    buf.append(effects.substForThis(rpl));
	}	
	return buf.toList();

    }

    public Effects substForVars(List<VarSymbol> from, List<VarSymbol> to) {
	Effects result = new Effects();
	for (Effect e : effects) {
	    result.add(e.substForVars(from, to));
	}
	return result;
    }
    
    public Effects substExpsForVars(List<VarSymbol> from, List<JCExpression> to) {
	Effects result = new Effects();
	for (Effect e : effects) {
	    result.add(e.substExpsForVars(from, to));
	}
	return result;		
    }
    
    public Effects substIndices(List<VarSymbol> from, 
	    	List<JCExpression> to) {
	Effects result = new Effects();
	for (Effect e: effects) {
	    result.add(e.substIndices(from, to));
	}
	return result;
    }

    public static List<Effects> substIndices(List<Effects> list,
	    List<VarSymbol> from, List<JCExpression> to) {
	ListBuffer<Effects> buf = ListBuffer.lb();
	for (Effects effects : list) {
	    buf.append(effects.substIndices(from, to));
	}	
	return buf.toList();
    }
    
    public Iterator<Effect> iterator() {
	return effects.iterator();
    }
    
    @Override public String toString() {
	return effects.toString();
    }
    
    @Override
    public int hashCode() {
	return this.effects.hashCode();
    }

    @Override
    public boolean equals(Object o) {
	if (!(o instanceof Effects))
	    return false;
	return effects.equals(((Effects)o).effects);
    }

    /** @return true iff every Effect in this set is a subeffect of at least one 
     * Effect in the given set 
     */
    public boolean areSubeffectsOf(Effects otherEffects) {
        if (effects.isEmpty()) return true;
        if (effects == UNKNOWN || otherEffects == UNKNOWN) return true;
        
        Effect e = this.first();
        if (!e.isSubeffectOf(otherEffects)) {
            return false;
        } else {
            return this.without(e).areSubeffectsOf(otherEffects);    
        }
    }

    /** @return a set of Effects in this set that are <b>not</b> subeffects 
     * of at least one Effect in the given set 
     */
    public Effects missingFrom(Effects otherEffects) {
	Effects result = new Effects();
	for (Effect e : effects)
	    if (!e.isSubeffectOf(otherEffects))
	        result.add(e);
	return result;
    }
    
    /**
     * The effects as a member of t
     */
    public Effects asMemberOf(Types types, Type t, Symbol owner) {
	Effects memberEffects = new Effects();
	for (Effect e : effects) {
	    memberEffects.addAll(e.asMemberOf(types, t, owner));
	}
	return memberEffects;
    }

    /**
     * Translate effects from method signature context to method use context.  This
     * is used both for declared effects and for method effect constraints.
     * @return Translated effects
     */    
    public Effects translateMethodEffects(JCMethodInvocation tree, 
	    Types types, Attr attr, Env<AttrContext> env) {
	
	MethodSymbol sym = tree.getMethodSymbol();
	
	Effects result = this;
	if (sym != null) {
	    if (tree.meth instanceof JCFieldAccess) {
        	// Translate to subclass and substitute for class 
        	// region and effect params
        	JCFieldAccess fa = (JCFieldAccess) tree.meth;
        	if (fa.selected.type instanceof ClassType) {
        	    ClassType ct = (ClassType) fa.selected.type;
        	    result = 
        		result.asMemberOf(types, ct.tsym.type, sym.owner);
        	    if (ct.getRegionActuals().size() == 
        		ct.tsym.type.getRegionParams().size()) {
        		result = 
        		    result.substForRegionParams(ct.tsym.type.getRegionParams(),
        			ct.getRegionActuals());
        	    }
        	    result = 
        		result.substForEffectVars(ct.tsym.type.getEffectArguments(),
        			ct.getEffectArguments());
        	}
                // Substitute for this
        	RPL rpl = attr.exprToRPL(fa.selected);
        	if (rpl != null) {
        	    result = result.substForThis(rpl);
        	}
        	// Substitute for actual arg expressions
        	result = result.substExpsForVars(sym.params, tree.args);
            } else if (tree.meth instanceof JCIdent) {
        	// Translate to subclass
        	result = result.asMemberOf(types, env.enclClass.sym.type,
        		sym.owner);
            }

            MethodSymbol methSym = tree.getMethodSymbol();
            if (tree.mtype != null) {
        	// Substitute for method region params
        	if (sym.rgnParams != null) {
        	    result = result.substForRegionParams(sym.rgnParams, 
        		    tree.mtype.regionActuals);
        	}
        	// Substitute for type region params
        	if (sym.typarams != null) {
        		result = result.substForTRParams(sym.typarams,
        			tree.mtype.typeactuals);
        	}
        	if (methSym != null) {
        	    List<Type> paramtypes = methSym.type.getParameterTypes();
        	    ListBuffer<Type> argtypes = ListBuffer.lb();
        	    for (JCExpression arg : tree.getArguments())
        		argtypes.append(arg.type);
        	    result = result.substForTRParams(paramtypes, argtypes.toList());
        	}
            }
            
            // Substitute for index exprs
            if (methSym != null && methSym.params != null &&
        	    !effects.isEmpty() && !tree.getArguments().isEmpty()) {
        	result = result.substIndices(methSym.params, 
        		tree.getArguments());
            }

            // Substitute for method effect params
            if (sym.effectparams != null && tree.mtype != null) {
        	result = result.substForEffectVars(sym.effectparams,
        	    tree.mtype.effectactuals);
            }
	}
	return result;
    }
    
    /**
     * The effect set as it appears in the environment env:
     * - RPLs that refer to out-of-scope variables get converted to 
     *   more general RPLs
     * - RPLs that refer to out-of-scope local region names get
     *   deleted
     * - Stack regions corresponding to out-of-scope local variables
     *   get deleted
     */
    public Effects inEnvironment(Resolve rs, Env<AttrContext> env,
	    boolean pruneLocalEffects) {
	Effects newEffects = new Effects();
	boolean changed = false;
	for (Effect e : effects) {
	    Effect newEffect = e.inEnvironment(rs, env, pruneLocalEffects);
	    if (newEffect == null) {
		changed = true;
	    } else {
		newEffects.add(newEffect);
		if (newEffect != e) changed = true;
	    }
	}
	return changed ? newEffects : this;
    }
    
    /**
     * Return an effect set given by the argument effect set, occurring
     * in an atomic statement.
     */
    public Effects inAtomic() {
	Effects newEffects = new Effects();
	boolean changed = false;
	for (Effect e : effects) {
	    Effect newEffect = e.inAtomic();
	    newEffects.add(newEffect);
	    if (newEffect != e) changed = true;
	}
	return changed ? newEffects : this;	
    }
    
    /**
     * Return an effect set given by the argument effect set, occurring
     * in a nonint statement.
     */
    public Effects inNonint() {
	Effects newEffects = new Effects();
	boolean changed = false;
	for (Effect e : effects) {
	    Effect newEffect = e.inNonint();
	    newEffects.add(newEffect);
	    if (newEffect != e) changed = true;
	}
	return changed ? newEffects : this;
    }

    /**
     * Check whether two effect sets are noninterfering
     */
    public static boolean noninterferingEffects(Effects effects1, Effects effects2, 
	    Constraints constraints, boolean atomicOK) {
        if (effects1.isEmpty()) return true;
        Effect e = effects1.first();
        boolean result = e.isNoninterferingWith(effects2, constraints, atomicOK);
        if (result) {
           effects1 = effects1.without(e);
           result = noninterferingEffects(effects1, effects2,
        	   constraints, atomicOK);
        }
        return result;
    }
    
    /**
     * Check whether noninterference constraints are satisfied
     */
    public static boolean nonintConstraintsAreSatisfied(List<Pair<Effects,Effects>> constraints,
	    Type t, Constraints envConstraints) {
	for (Pair<Effects,Effects> constraint : constraints) {
	    Effects first = constraint.fst.substForAllParams(t);
	    Effects second = constraint.snd.substForAllParams(t);
	    if (!noninterferingEffects(first, second, envConstraints, false)) {
		return false;
	    }
	}
	return true;
    }

    public static boolean nonintConstraintsAreSatisfied(List<Pair<Effects,Effects>> constraints,
	    JCMethodInvocation tree, 
	    List<RegionParameterSymbol> rplFormals, List<RPL> rplActuals,
	    List<Effects> effectFormals, List<Effects> effectActuals, 
	    Types types, Attr attr, Env<AttrContext> env) {
	Constraints envConstraints = env.info.constraints;
	for (Pair<Effects,Effects> constraint : constraints) {
	    Effects first = constraint.fst.translateMethodEffects(tree, types, attr, env);
	    first = first.substForRegionParams(rplFormals, rplActuals);
	    first = first.substForEffectVars(effectFormals, effectActuals);
	    Effects second = constraint.snd.translateMethodEffects(tree, types, attr, env);
	    second = second.substForRegionParams(rplFormals, rplActuals);
	    second = second.substForEffectVars(effectFormals, effectActuals);
	    System.out.println("first="+first);
	    System.out.println("second="+second);
	    if (!noninterferingEffects(first, second, envConstraints, false))
		return false;
	}

	return true;
    }
    
    /** Trim effects to minimal set
     */
    public Effects trim() {
	Effects newEffects = new Effects();
	newEffects.effects.addAll(this.effects);
	boolean changed = false;
	for (Effect e : effects) {
	    newEffects.effects.remove(e);
	    if (e.isSubeffectOf(newEffects)) {
		changed = true;
	    } else {
		newEffects.effects.add(e);
	    }
	}
	return changed ? newEffects : this;
    }
    
}
