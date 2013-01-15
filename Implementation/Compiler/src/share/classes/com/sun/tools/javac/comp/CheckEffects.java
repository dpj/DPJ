package com.sun.tools.javac.comp;

import static com.sun.tools.javac.code.Flags.STATIC;
import static com.sun.tools.javac.code.Kinds.MTH;
import static com.sun.tools.javac.code.Kinds.TYP;
import static com.sun.tools.javac.code.Kinds.VAR;
import static com.sun.tools.javac.code.TypeTags.CLASS;
import static com.sun.tools.javac.code.TypeTags.TYPEVAR;

import java.util.LinkedList;

import com.sun.tools.javac.code.Effect;
import com.sun.tools.javac.code.Effect.InvocationEffect;
import com.sun.tools.javac.code.Effects;
import com.sun.tools.javac.code.Lint;
import com.sun.tools.javac.code.RPL;
import com.sun.tools.javac.code.RPLs;
import com.sun.tools.javac.code.Substitute;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.OperatorSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ArrayType;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.DPJAtomic;
import com.sun.tools.javac.tree.JCTree.DPJCobegin;
import com.sun.tools.javac.tree.JCTree.DPJFinish;
import com.sun.tools.javac.tree.JCTree.DPJForLoop;
import com.sun.tools.javac.tree.JCTree.DPJNegationExpression;
import com.sun.tools.javac.tree.JCTree.DPJNonint;
import com.sun.tools.javac.tree.JCTree.DPJRegionApply;
import com.sun.tools.javac.tree.JCTree.DPJSpawn;
import com.sun.tools.javac.tree.JCTree.JCArrayAccess;
import com.sun.tools.javac.tree.JCTree.JCAssert;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCAssignOp;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCCase;
import com.sun.tools.javac.tree.JCTree.JCCatch;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCConditional;
import com.sun.tools.javac.tree.JCTree.JCDoWhileLoop;
import com.sun.tools.javac.tree.JCTree.JCEnhancedForLoop;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCExpressionWithRPL;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCForLoop;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCIf;
import com.sun.tools.javac.tree.JCTree.JCInstanceOf;
import com.sun.tools.javac.tree.JCTree.JCLabeledStatement;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCNewArray;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCParens;
import com.sun.tools.javac.tree.JCTree.JCReturn;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCSwitch;
import com.sun.tools.javac.tree.JCTree.JCSynchronized;
import com.sun.tools.javac.tree.JCTree.JCThrow;
import com.sun.tools.javac.tree.JCTree.JCTreeWithEffects;
import com.sun.tools.javac.tree.JCTree.JCTry;
import com.sun.tools.javac.tree.JCTree.JCTypeApply;
import com.sun.tools.javac.tree.JCTree.JCTypeCast;
import com.sun.tools.javac.tree.JCTree.JCUnary;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.JCTree.JCWhileLoop;
import com.sun.tools.javac.tree.JCTree.LetExpr;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Pair;

/**
 * Tree scanner that walks the AST, infers effects, and performs the following 
 * checks:
 * 
 * 1. Inferred method body effects are a subeffect of their declared effects 
 *    (violation = error)
 * 
 * 2. Inferred effects of each statement in a cobegin block are pairwise
 *    noninterfering (violation = warning)
 *    
 * 3. Inferred effects of foreach body are noninterfering with themselves, after
 *    replacing the index variable with its negation (violation = warning)
 * 
 * @author Rob Bocchino
 * @author Jeff Overbey
 * @author Mohsen Vakilian
 */
public class CheckEffects extends EnvScanner { // DPJ
    
    protected static final Context.Key<CheckEffects> effectsKey =
	new Context.Key<CheckEffects>();

    private final Name.Table names;
    private final Log log;
    private final Types types;
    private final Resolve rs;
    private final RPLs rpls;
    private final Attr attr;
    private       Lint lint;

    public static CheckEffects instance(Context context) {
	CheckEffects instance = context.get(effectsKey);
	if (instance == null)
	    instance = new CheckEffects(context);
	return instance;
    }

    protected CheckEffects(Context context) {
	super(context);
	context.put(effectsKey, this);

	names = Name.Table.instance(context);
	log = Log.instance(context);
        types = Types.instance(context);
	lint = Lint.instance(context);
	rs = Resolve.instance(context);
	rpls = RPLs.instance(context);
	attr = Attr.instance(context);
    }

    /** Are we in an atomic statement? */
    private boolean inAtomic;
    
    /** Are we in a nonint statement? */
    private boolean inNonint;
    
    /**
     * Compute interference between sets of statements
     * @param stats
     * @return
     */
    private boolean statementsInterfere(List<JCStatement> stats,
	    boolean atomicOK) {
	if (stats.size() > 1)
	    return statementsInterfere(stats.head, stats.tail, atomicOK);
	return false;
    }
    
    private boolean statementsInterfere(JCStatement stat, List<JCStatement> stats,
	    boolean atomicOK) {
	for (JCStatement stat2 : stats) {
	    Effects effects1 = stat.effects.inEnvironment(rs, childEnvs.head, false);
	    Effects effects2 = stat2.effects.inEnvironment(rs, childEnvs.head, false);
	    if (!Effects.noninterferingEffects(effects1, effects2,
			childEnvs.head.info.constraints, atomicOK))
		return true;
	}
	if (statementsInterfere(stats, atomicOK))
	    return true;
	return false;
    }

    /**
     * Utility class to compute the accessed RPL of an expression
     * that accesses a location.  The expression itself doesn't tell us whether
     * the access is a read or write; we need the surrounding context (parent
     * in the AST) to tell us that.
     */
    protected RPL accessedRPL(JCExpression tree, boolean inConstructor) {
	RPL result = (new RPLAccessVisitor()).accessed(tree, inConstructor);
	if (tree instanceof JCExpressionWithRPL && inConstructor == false) {
	    ((JCExpressionWithRPL)tree).rpl = result;
	}
	return result;
    }
    private class RPLAccessVisitor extends JCTree.Visitor {
	public RPL result = null;
	public boolean inConstructor = false;
	public RPL accessed(JCExpression tree, boolean inConstructor) {
	    this.inConstructor = inConstructor;
	    tree.accept(this);
	    return result;
	}
	/**
	 * Determine whether a variable symbol is an instance field of the
	 * enclosing class or a superclass.
	 * @param v
	 * @return
	 */
	private boolean isInstanceField(VarSymbol v) {
	    Type site = parentEnv.enclClass.sym.type;
            if (v.owner.kind == TYP &&
        	    (v.flags() & STATIC) == 0 &&
        	    (site.tag == CLASS || site.tag == TYPEVAR)) {
        	if (types.asOuterSuper(site, v.owner) != null) {
        	    return true;
        	}
            }
            return false;
	}

	public void visitSelect(JCFieldAccess tree) {
	    if (tree.selected.type instanceof ClassType &&
		    tree.sym instanceof VarSymbol) {
		ClassType ct = (ClassType) tree.selected.type;
		VarSymbol vsym = (VarSymbol) tree.sym;
		if (inConstructor && isInstanceField(vsym)) return;
		if (vsym.rpl == null) return;
		result = Substitute.allRPLParams(rpls.memberRPL(types, ct, 
			vsym), ct);
		RPL rpl = attr.exprToRPL(tree.selected);
		if (rpl != null) {
		    result = result.substRPLForVar(tree.sym.enclThis(), rpl);
		}
	    }
        }
	
	public void visitIdent(JCIdent tree) {
	    VarSymbol v = attr.getVarSymbolFor(tree, parentEnv);
	    if (v != null) {
		if (!inConstructor || !isInstanceField(v)) {
		    result = attr.getRPLFor(tree, parentEnv);
		    result = attr.asMemberOf(result, tree, parentEnv);
		}
	    }
        }

	public void visitIndexed(JCArrayAccess tree) {
	    result = attr.getRPLFor(tree,parentEnv);
	    result = attr.asMemberOf(result, tree, parentEnv);
	    result = attr.substIndex(result, tree, parentEnv);
            result = attr.substRPLForVar(result, tree, parentEnv);
        }
        @Override
        public void visitTree(JCTree tree) {
            // Default: Do nothing (result = null)
        }
    };
    
    /**
     * Add the effects of from into the effects of to
     * @param from
     * @param to
     */
    private void addAll(JCTreeWithEffects from, JCTreeWithEffects to) {
	to.effects.addAll(from.effects);
	if (attr.inConstructor(parentEnv))
	    to.getConstructorEffects().addAll(from.getConstructorEffects());	
    }
    
    private void addAll(List<? extends JCTreeWithEffects> from, JCTreeWithEffects to) {
	for (List<? extends JCTreeWithEffects> l = from; l.tail != null; l = l.tail) {
	    to.effects.addAll(l.head.effects);
	    if (attr.inConstructor(parentEnv))
		to.getConstructorEffects().addAll(l.head.getConstructorEffects());
	}
    }
    
    /**
     * Add the effects of from into the effects of to
     * If the tree accessed an RPL, add a read effect for it
     * @param from
     * @param to
     */
    private void addAllWithRead(JCExpression from, JCTreeWithEffects to) {
	addAll(from, to);
	addReadEffect(from, to);
    }

    /**
     * If from accessed an RPL, add a read effect for it to the
     * effects of to.
     * @param from
     * @param to
     */    
    private void addReadEffect(JCExpression from, JCTreeWithEffects to) {
	RPL access = accessedRPL(from, false);
	if (access != null)
	    to.effects.add(new Effect.ReadEffect(rpls, access, 
		    inAtomic, inNonint));
	access = accessedRPL(from, true);
	if (access != null && attr.inConstructor(parentEnv))
	    to.getConstructorEffects().add(new Effect.ReadEffect(rpls, access, 
		    inAtomic, inNonint));
    }
    
    /**
     * Add the effects of from into the effects of to.
     * If the tree accessed an RPL, add a write effect for it.
     * @param from
     * @param to
     */    
    private void addAllWithWrite(JCExpression from, JCTreeWithEffects to) {
	to.effects.addAll(from.effects);
	if (attr.inConstructor(parentEnv))
	    to.getConstructorEffects().addAll(from.getConstructorEffects());
	addWriteEffect(from, to);
    }
    
    /**
     * If from accessed an RPL, add a write effect for it to the
     * effects of to.
     * @param from
     * @param to
     */
    private void addWriteEffect(JCExpression from, JCTreeWithEffects to) {
	RPL access = accessedRPL(from, false);
	if (access != null)
	    to.effects.add(new Effect.WriteEffect(rpls, access, 
		    inAtomic, inNonint));
	access = accessedRPL(from, true);
	if (access != null)
	    to.getConstructorEffects().add(new Effect.WriteEffect(rpls, access, 
		    inAtomic, inNonint));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Visitor Methods
    ///////////////////////////////////////////////////////////////////////////

    private Effects initEffects;
    private LinkedList<Pair<Effects, DiagnosticPosition>> ctorEffects;

    @Override
    public void visitClassDef(JCClassDecl tree) {
	Effects savedInitEffects = initEffects;
        LinkedList<Pair<Effects, DiagnosticPosition>> 
        	savedCtorEffects = ctorEffects;
        initEffects = new Effects();
        ctorEffects = new LinkedList<Pair<Effects, DiagnosticPosition>>();
	super.visitClassDef(tree);
        // Check declared constructor effects against initializers                               
        Env<AttrContext> env = childEnvs.head;
        if (env != null) {
            initEffects = initEffects.inEnvironment(rs, env, true);
            for (Pair<Effects, DiagnosticPosition> pair : ctorEffects) {
                Effects declaredEffects = pair.fst.inEnvironment(rs, env, true);
                if (!initEffects.areSubeffectsOf(declaredEffects)) {
                    log.error(pair.snd, "bad.effect.summary");
                    System.err.println("Missing " +
                            initEffects.missingFrom(declaredEffects).trim());
                }
            }
        }
        initEffects = savedInitEffects;
        ctorEffects = savedCtorEffects;
    }
    
    @Override
    public void visitMethodDef(JCMethodDecl tree) {
	super.visitMethodDef(tree);
	MethodSymbol m = tree.sym;
	Effects actualEffects = Effects.UNKNOWN;
	Effects declaredEffects = m.effects;
	DiagnosticPosition pos = (tree.effects == null) ?
		tree.pos() : tree.effects.pos();
	if (tree.body != null) {
	    if (!attr.inConstructor(childEnvs.head)) {
		actualEffects = 
		    tree.body.effects.inEnvironment(rs, childEnvs.head, true);
	    } else {
		actualEffects =
		    tree.body.getConstructorEffects().inEnvironment(rs, childEnvs.head, true);
		// Add in constructor effects for later checking against initializers            
                ctorEffects.add(new Pair<Effects, DiagnosticPosition>(declaredEffects,pos));
	    }
	}
	if (!actualEffects.areSubeffectsOf(declaredEffects)) {
	    System.err.println("Effect summary does not cover " + 
		    actualEffects.missingFrom(declaredEffects));
	    log.error(pos, "bad.effect.summary");
	}
    }
	
    @Override
    public void visitAssert(JCAssert tree) {
	super.visitAssert(tree);
	addAll(tree.cond, tree);
	if (tree.detail != null) 
	    addAll(tree.detail, tree);
	
    }

    @Override
    public void visitDoLoop(JCDoWhileLoop tree) {
	super.visitDoLoop(tree);
	addAllWithRead(tree.cond, tree);
	addAll(tree.body, tree);
    }

    @Override
    public void visitDPJForLoop(DPJForLoop tree) {
	super.visitDPJForLoop(tree);
	if (tree.var.init != null) addAllWithRead(tree.var.init, tree);
	if (tree.start != null) addAllWithRead(tree.start, tree);
	if (tree.length != null) addAllWithRead(tree.length, tree);
	if (tree.stride != null) addAllWithRead(tree.stride, tree);
	addAll(tree.body, tree);
	Env<AttrContext> env = parentEnv.dup(tree, parentEnv.info.dup());
	env.info.scope.enter(tree.var.sym);
	Effects effects = tree.body.effects.inEnvironment(rs, env, false);
	env.info.scope.leave();
	Effects negatedEffects = 
	    effects.substIndices(List.of(tree.var.sym), 
		    List.<JCExpression>of(new DPJNegationExpression(tree.var.sym)));
	if (!Effects.noninterferingEffects(effects, negatedEffects,
		env.info.constraints, tree.isNondet)) {
	    log.warning(tree.pos(), "interference.foreach");
	}
    }

    @Override
    public void visitFinish(DPJFinish tree) {
	super.visitFinish(tree);
	addAll(tree.body, tree);
    }

    @Override
    public void visitAtomic(DPJAtomic tree) {
	boolean savedInAtomic = inAtomic;
	inAtomic = true;
	super.visitAtomic(tree);
	addAll(tree.body, tree);
	inAtomic = savedInAtomic;
    }
    
    @Override
    public void visitNonint(DPJNonint tree) {
	boolean savedInNonint = inNonint;
	inNonint = true;
	super.visitNonint(tree);
	addAll(tree.body, tree);
	inNonint = savedInNonint;	
    }
    
    @Override
    public void visitForeachLoop(JCEnhancedForLoop tree) {
	super.visitForeachLoop(tree);
	if (tree.var.init != null) addAllWithRead(tree.var.init, tree);
	addAllWithRead(tree.expr, tree);
	addAll(tree.body, tree);
    }

    @Override
    public void visitForLoop(JCForLoop tree) {
	super.visitForLoop(tree);
	addAll(tree.init, tree);
	if (tree.cond != null) addAllWithRead(tree.cond, tree);
	if (tree.step != null) addAll(tree.step, tree);
	addAll(tree.body, tree);
    }

    @Override
    public void visitIf(JCIf tree) {
	super.visitIf(tree);
	addAllWithRead(tree.cond, tree);
	addAll(tree.thenpart, tree);
	if (tree.elsepart != null) 
	    addAll(tree.elsepart, tree);
    }

    @Override
    public void visitIndexed(JCArrayAccess tree) {
	super.visitIndexed(tree);
	addAllWithRead(tree.indexed, tree);
	addAllWithRead(tree.index, tree);
    }

    @Override
    public void visitLabelled(JCLabeledStatement tree) {
	super.visitLabelled(tree);
	addAll(tree.body, tree);
    }

    @Override
    public void visitNewArray(JCNewArray tree) {
	// TODO Constructor effects
	super.visitNewArray(tree);
    }

    @Override
    public void visitSpawn(DPJSpawn tree) {
	super.visitSpawn(tree);
	addAll(tree.body, tree);
    }

    @Override
    public void visitSwitch(JCSwitch tree) {
	super.visitSwitch(tree);
	addAllWithRead(tree.selector, tree);
	addAll(tree.cases, tree);
    }

    @Override
    public void visitCase(JCCase tree) {
	super.visitCase(tree);
	if (tree.pat != null)
	    addAllWithRead(tree.pat, tree);
	addAll(tree.stats, tree);
    }

    @Override
    public void visitSynchronized(JCSynchronized tree) {
	super.visitSynchronized(tree);
	addAllWithRead(tree.lock, tree);
	addAll(tree.body, tree);
    }

    @Override
    public void visitThrow(JCThrow tree) {
	super.visitThrow(tree);
	addAllWithRead(tree.expr, tree);
    }

    @Override
    public void visitTry(JCTry tree) {
	super.visitTry(tree);
	addAll(tree.body, tree);
	addAll(tree.catchers, tree);
	if (tree.finalizer != null) 
	    addAll(tree.finalizer, tree);
    }

    @Override
    public void visitCatch(JCCatch tree) {
	super.visitCatch(tree);
	addAll(tree.body, tree);
    }

    @Override
    public void visitTypeCast(JCTypeCast tree) {
	super.visitTypeCast(tree);
	addAllWithRead(tree.expr, tree);
    }

    @Override
    public void visitWhileLoop(JCWhileLoop tree) {
	super.visitWhileLoop(tree);
	addAllWithRead(tree.cond, tree);
	addAll(tree.body, tree);
    }

    @Override
    public void visitLetExpr(LetExpr tree) {
	super.visitLetExpr(tree);
	addAll(tree.defs, tree);
	if (tree.expr instanceof JCTreeWithEffects)
	    addAll((JCTreeWithEffects) tree.expr, tree);
    }

    @Override public void visitSelect(JCFieldAccess tree) {
	super.visitSelect(tree);
	addAllWithRead(tree.selected, tree);
    }

    @Override public void visitAssign(JCAssign tree) {
        super.visitAssign(tree);
        accumulateAssignEffects(tree.lhs, tree.rhs, tree);
    }

    private void accumulateAssignEffects(JCExpression lhs,
	    JCExpression rhs, JCTreeWithEffects to) {
        addAllWithRead(rhs, to);
        addAllWithWrite(lhs, to);
    }

    @Override public void visitExec(JCExpressionStatement tree) {
	super.visitExec(tree);
	addAllWithRead(tree.expr, tree);
    }
    
    @Override public void visitAssignop(JCAssignOp tree) {
        super.visitAssignop(tree);
        accumulateAssignEffects(tree.lhs, tree.rhs, tree);
    }
    
    @Override public void visitVarDef(JCVariableDecl tree) {	
	super.visitVarDef(tree);

	if (tree.init != null) {
	    addAllWithRead(tree.init, tree);
            if (attr.inClassDef(parentEnv) && !tree.mods.areStatic()) {
                // Record field initializer effects for checking against                         
                // constructors                                                                  
                initEffects.addAll(tree.init.effects);
            }
	}
    }

    @Override public void visitParens(JCParens tree) {
	super.visitParens(tree);
	addAllWithRead(tree.expr, tree);
    }
    
    @Override public void visitTypeTest(JCInstanceOf tree) {
	super.visitTypeTest(tree);
	addAllWithRead(tree.expr, tree);
    }

    @Override public void visitUnary(JCUnary tree) {
	super.visitUnary(tree);
	switch (((OperatorSymbol)tree.operator).opcode) {
	case JCTree.PREINC: case JCTree.PREDEC:
	case JCTree.POSTINC: case JCTree.POSTDEC:
	    addAllWithWrite(tree.arg, tree);
	    break;
	default:
	    addAllWithRead(tree.arg, tree);
	    break;	
	}
    }

    @Override public void visitBinary(JCBinary tree) {
	super.visitBinary(tree);
	addAllWithRead(tree.lhs, tree);
	addAllWithRead(tree.rhs, tree);
    }
    
    @Override public void visitConditional(JCConditional tree) {
	super.visitConditional(tree);
	addAllWithRead(tree.cond, tree);
	addAllWithRead(tree.truepart, tree);
	addAllWithRead(tree.falsepart, tree);
    }
    
    @Override
    public void visitApply(JCMethodInvocation tree) {

	super.visitApply(tree);
	
	// We are accumulating effects for e.m(e1, ..., en)
	// Accumulate the effects from evaluating e
	addAll(tree.meth, tree);
	
	// Accumulate any effects from evaluating e1, ..., en
	for (JCExpression arg : tree.args) {
	    addAllWithRead(arg, tree);
	}

	// Accumulate the effect of invoking m
	MethodSymbol sym = tree.getMethodSymbol();
	
	if (sym != null) {
	    Effects effects = 
		sym.effects.translateMethodEffects(tree, types, attr, parentEnv);
            
            InvocationEffect ie = new InvocationEffect(rpls, sym, effects);
            if (inNonint) {
        	ie = (InvocationEffect) ie.inNonint();
            } else if (inAtomic) {
        	ie = (InvocationEffect) ie.inAtomic();
            }
            
	    tree.effects.add(ie);
	    if (attr.inConstructor(parentEnv))
		tree.getConstructorEffects().add(ie);
	}
    }
    
    @Override
    public void visitReturn(JCReturn tree) {
	super.visitReturn(tree);

	// Effects for 'return' or 'return e'
	if (tree.expr != null) {
	    // 'return e'
	    // Accumulate effects of evaluating e
	    addAllWithRead(tree.expr, tree);
	}
    }

    @Override public void visitNewClass(JCNewClass tree) {
	super.visitNewClass(tree);
	// Effects for new T(e1, ... en)
	// Only effects are those of evaluating e1, ..., en
	for (JCExpression arg : tree.args) {
	    // TODO:  Handle RPL arguments to constructor
	    addAllWithRead(arg, tree);
	}
    }
    
    @Override public void visitRegionApply(DPJRegionApply tree) {
	super.visitRegionApply(tree);
    }
    
    @Override public void visitTypeApply(JCTypeApply tree) {
	super.visitTypeApply(tree);
    }
    
    @Override public void visitBlock(JCBlock tree) {
	super.visitBlock(tree);
	for (JCTree.JCStatement stat : tree.stats) {
	    addAll(stat, tree);
	}
        if (attr.inClassDef(parentEnv) && !tree.isStatic()) {
            // Record instance initializer effects for checking against                         
            // constructors                                                                  
            initEffects.addAll(tree.effects);
        }
    }
    
    @Override public void visitCobegin(DPJCobegin tree) {
	super.visitCobegin(tree);
	tree.effects = tree.body.effects;
	if (attr.inConstructor(parentEnv))
	    tree.setConstructorEffects(tree.body.getConstructorEffects());
	boolean interfere = false;
	if (tree.body instanceof JCBlock) {
	    interfere = statementsInterfere(((JCBlock) tree.body).stats,
		    tree.isNondet);
	}
	if (interfere) {
	    log.warning(tree.pos(), "interference.cobegin");
	}
    }


}