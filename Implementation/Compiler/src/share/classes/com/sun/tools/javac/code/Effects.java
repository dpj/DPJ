package com.sun.tools.javac.code;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.sun.tools.javac.code.Effect.VariableEffect;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Substitute.AsMemberOf;
import com.sun.tools.javac.code.Substitute.Subst;
import com.sun.tools.javac.code.Substitute.SubstRPLs;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.Resolve;
import com.sun.tools.javac.tree.JCTree;
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
public class Effects implements 
	Iterable<Effect>,
	SubstRPLs<Effects>,
	AsMemberOf<Effects>
{
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

    public void addAllEffects(Iterable<? extends JCTreeWithEffects> trees) {
	for (JCTreeWithEffects tree : trees)
	    this.addAll(tree.effects);
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
	    result.addAll(e.substAllParams(t));
	}
	return result;
    }
    
    /** @return a new Effects set where the RPL parameters 'from' 
     * have been replaced with the RPLs 'to' */
    public Effects substRPLParams(Iterable<RPL> from, Iterable<RPL> to) {
	Effects result = new Effects();
	for (Effect e : effects) {
	    result.add(e.substRPLParams(from, to));
	}
	return result;
    }
    
    public Effects substTRParams(Iterable<Type> from, Iterable<Type> to) {
	Effects result = new Effects();
	for (Effect e : effects) {
	    result.add(e.substTRParams(from, to));
	}
	return result;	
    }
    
    public Effects substEffectParams(Iterable<Effects> from, 
	    Iterable<Effects> to) {
	Effects result = new Effects();
	for (Effect e : effects) {
	    result.addAll(e.substEffectParams(from, to));
	}
	return result;
    }
    
     public Effects substRPLForVar(VarSymbol from, RPL to) {
	Effects result = new Effects();
	for (Effect e : effects) {
	    result.add(e.substRPLForVar(from, to));
	}
	return result;	
    }
    
    public Effects substVars(Iterable<VarSymbol> from, 
	    Iterable<VarSymbol> to) {
	Effects result = new Effects();
	for (Effect e : effects) {
	    result.add(e.substVars(from, to));
	}
	return result;
    }
    
    public Effects substExpsForVars(Iterable<VarSymbol> from, 
	    Iterable<JCExpression> to) {
	Effects result = new Effects();
	for (Effect e : effects) {
	    result.add(e.substExpsForVars(from, to));
	}
	return result;		
    }
    
    public Effects substIndices(Iterable<VarSymbol> from, 
	    	Iterable<JCExpression> to) {
	Effects result = new Effects();
	for (Effect e: effects) {
	    result.add(e.substIndices(from, to));
	}
	return result;
    }

    public Iterator<Effect> iterator() {
	return effects.iterator();
    }
    
    @Override public String toString() {	
	return trim().effects.toString();
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
    public Effects asMemberOf(Type t, Types types) {
	Effects memberEffects = new Effects();
	for (Effect e : effects) {
	    memberEffects.addAll(e.asMemberOf(t, types));
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
	JCExpression selectedExp = attr.rs.selectedExp(tree.meth,env);
	
	Effects result = this;
	if (sym != null) {
	    // Translate to subclass and substitute for class 
	    // region and effect params
	    if (selectedExp.type instanceof ClassType) {
		ClassType ct = (ClassType) selectedExp.type;
		result = 
			result.asMemberOf(ct.tsym.type, types);
		if (ct.getRPLArguments().size() == 
			ct.tsym.type.getRPLArguments().size()) {
		    result = 
			    result.substRPLParams(ct.tsym.type.getRPLArguments(),
				    ct.getRPLArguments());
		}
		result = 
			result.substEffectParams(ct.tsym.type.getEffectArguments(),
				ct.getEffectArguments());
	    }
	    // Substitute for actual arg expressions
	    RPL rpl = attr.exprToRPL(selectedExp);
	    if (rpl != null) {
		result = result.substRPLForVar(tree.getThisSymbol(), rpl);
	    }
	    result = result.substExpsForVars(sym.params, tree.args);

            MethodSymbol methSym = tree.getMethodSymbol();
            if (tree.mtype != null) {
        	// Substitute for method region params
        	if (sym.rgnParams != null) {
        	    result = result.substRPLParams(sym.rgnParams, 
        		    tree.mtype.regionActuals);
        	}
        	// Substitute for type region params
        	if (sym.typarams != null) {
        		result = result.substTRParams(sym.typarams,
        			tree.mtype.typeactuals);
        	}
        	if (methSym != null) {
        	    List<Type> paramtypes = methSym.type.getParameterTypes();
        	    ListBuffer<Type> argtypes = ListBuffer.lb();
        	    for (JCExpression arg : tree.getArguments())
        		argtypes.append(arg.type);
        	    result = result.substTRParams(paramtypes, argtypes.toList());
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
        	result = result.substEffectParams(sym.effectparams,
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
     * Capture all effects
     */
    public Effects capture() {
	Effects capturedEffects = new Effects();
	boolean changed = false;
	for (Effect e : effects) {
	    Effect capturedEffect = e.capture();
	    capturedEffects.add(capturedEffect);
	    if (capturedEffect != e) changed = true;
	}
	return changed ? capturedEffects : this;
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
    public static boolean nonintConstraintsAreSatisfied
    	(Iterable<Pair<Effects,Effects>> constraints,
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

    public static boolean nonintConstraintsAreSatisfied
    	(Iterable<Pair<Effects,Effects>> constraints,
    		JCMethodInvocation tree, 
	    List<RPL> rplFormals, List<RPL> rplActuals,
	    List<Effects> effectFormals, List<Effects> effectActuals, 
	    Types types, Attr attr, Env<AttrContext> env) {
	Constraints envConstraints = env.info.constraints;
	for (Pair<Effects,Effects> constraint : constraints) {
	    Effects first = constraint.fst.translateMethodEffects(tree, types, attr, env);
	    first = first.substRPLParams(rplFormals, rplActuals);
	    first = first.substEffectParams(effectFormals, effectActuals);
	    Effects second = constraint.snd.translateMethodEffects(tree, types, attr, env);
	    second = second.substRPLParams(rplFormals, rplActuals);
	    second = second.substEffectParams(effectFormals, effectActuals);
	    if (!noninterferingEffects(first, second, envConstraints, false)) {
		System.out.println(first + " interferes with " +second);
		return false;
	    }
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
    
    /**
     * Substitution classes
     */
    public static final Subst substRPLParams = new Subst<Effects,RPL,RPL>() {
	public Effects basic(Effects effects, RPL from, RPL to) {
	    return effects.substRPLParams(List.of(from), List.of(to));
	}
	public Effects substIterable(Effects effects, Iterable<RPL> from,
		Iterable<RPL> to) {
	    return effects.substRPLParams(from, to);
	}
    };
    public static final Subst substEffectParams = new Subst<Effects,Effects,Effects>() {
	public Effects basic(Effects effects, Effects from, Effects to) {
	    return effects.substEffectParams(List.of(from), List.of(to));
	}
	public Effects substIterable(Effects effects, Iterable<Effects> from,
		Iterable<Effects> to) {
	    return effects.substEffectParams(from, to);
	}
    };
    public static final Subst substRPLsForVars = new Subst<Effects,VarSymbol,RPL>() {
	public Effects basic(Effects effects, VarSymbol from, RPL to) {
	    return effects.substRPLForVar(from, to);
	}
    };
    public static final Subst substIndices =
	    new Subst<Effects,VarSymbol,JCExpression>() {
	public Effects basic(Effects effects, VarSymbol from, JCExpression to) {
	    return effects.substIndices(List.of(from), List.of(to));
	}
	public Effects substIterable(Effects effects, Iterable<VarSymbol> from,
		Iterable<JCExpression> to) {
	    return effects.substIndices(from, to);
	}
    };


}
