package com.sun.tools.javac.comp;

import static com.sun.tools.javac.code.Flags.BLOCK;
import static com.sun.tools.javac.code.Flags.FINAL;
import static com.sun.tools.javac.code.Flags.STATIC;
import static com.sun.tools.javac.code.Kinds.MTH;
import static com.sun.tools.javac.code.Kinds.TYP;

import com.sun.tools.javac.code.RPL;
import com.sun.tools.javac.code.RPLElement;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.RegionParameterSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.tree.JCTree.DPJForLoop;
import com.sun.tools.javac.tree.JCTree.DPJRegionParameter;
import com.sun.tools.javac.tree.JCTree.DPJRegionPathList;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCCase;
import com.sun.tools.javac.tree.JCTree.JCCatch;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCEnhancedForLoop;
import com.sun.tools.javac.tree.JCTree.JCForLoop;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCLabeledStatement;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.DPJRegionDecl;
import com.sun.tools.javac.tree.JCTree.JCSwitch;
import com.sun.tools.javac.tree.JCTree.JCTry;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Pair;

/** This is a specialization of TreeScanner that reconstructs the
 *  attribution environment of each node as it scans the tree.  It is
 *  useful for passes such as effect checking and the porting tool
 *  that need environment information but don't want to depend on
 *  Attr.
 *  
 *  This class factors the environment-building logic out of Attr.  In
 *  theory, Attr could be built from this class too (leading to a tighter
 *  design and less code duplication) but that would require some more
 *  engineering.
 *  
 *  This pass assumes that any nodes you want to process have already been
 *  attributed.  Non-attributed symbols (i.e., null symbols in the tree)
 *  should be ignored, though I haven't thoroughly tested that assertion.
 *  If any null pointer errors crop up, just add some more code saying
 *  "if this symbol is null, don't add it to the environment."
 *  
 *  @author Rob Bocchino
 *  @author Mohsen Vakilian
 */
public abstract class EnvScanner extends TreeScanner {

    final Name.Table names;
    final MemberEnter memberEnter;
    protected Enter enter;

    protected EnvScanner(Context context) {
	names = Name.Table.instance(context);
	memberEnter = MemberEnter.instance(context);
	enter = Enter.instance(context);
    }
    
    public void setEnter(Enter enter) {
	this.enter = enter;
    }

    /* ************************************************************************
     * Visitor methods
     *************************************************************************/

    /**
     * Visitor argument: the environment that came in from our parent.
     */
    protected Env<AttrContext> parentEnv;
    
    /**
     * Visitor return value: the environment(s) used to process our children.
     */
    protected List<Env<AttrContext>> childEnvs;
    
    @Override
    public void visitClassDef(JCClassDecl tree) {
	Env<AttrContext> savedEnv = parentEnv;
	parentEnv = enter.typeEnvs.get(tree.sym);
	if (parentEnv != null) super.visitClassDef(tree);
	childEnvs = List.of(parentEnv);
	parentEnv = savedEnv;
    }

    @Override
    public void visitApply(JCMethodInvocation tree) {
	Env<AttrContext> savedEnv = parentEnv;
	parentEnv = parentEnv.dup(tree, parentEnv.info.dup());
        super.visitApply(tree);
        childEnvs = List.of(parentEnv);
        parentEnv = savedEnv;
    }
    
    @Override
    public void visitBlock(JCBlock tree) {
	Env<AttrContext> savedEnv = parentEnv;
        if (parentEnv.info.scope.owner.kind == TYP) {
            // Block is a static or instance initializer;
            // let the owner of the environment be a freshly
            // created BLOCK-method.
            parentEnv = parentEnv.dup(tree, parentEnv.info.dup(parentEnv.info.scope.dupUnshared()));
            parentEnv.info.scope.owner =
                new MethodSymbol(tree.flags | BLOCK, names.empty, null,
                                 parentEnv.info.scope.owner);
            if ((tree.flags & STATIC) != 0) parentEnv.info.staticLevel++;
            super.visitBlock(tree);
        } else {
            // Create a new local environment with a local scope.
            parentEnv = parentEnv.dup(tree, parentEnv.info.dup(parentEnv.info.scope.dup()));
            super.visitBlock(tree);
            parentEnv.info.scope.leave();
        }	
	childEnvs = List.of(parentEnv);
        parentEnv = savedEnv;
    }
    
    @Override
    public void visitDPJForLoop(DPJForLoop tree) {
	Env<AttrContext> savedEnv = parentEnv;
	parentEnv = parentEnv.dup(parentEnv.tree, parentEnv.info.dup(parentEnv.info.scope.dup()));
	super.scan(tree.var);
	super.scan(tree.start);
	if (tree.length != null) super.scan(tree.length);
	if (tree.stride != null) super.scan(tree.stride);
	parentEnv.tree = tree; // before, we were not in loop!
	super.scan(tree.body);
	parentEnv.info.scope.leave();
	childEnvs = List.of(parentEnv);
	parentEnv = savedEnv;
    }
    
    @Override
    public void visitForeachLoop(JCEnhancedForLoop tree) {
	Env<AttrContext> savedEnv = parentEnv;
	parentEnv =  parentEnv.dup(parentEnv.tree, parentEnv.info.dup(parentEnv.info.scope.dup()));
	super.scan(tree.var);
        parentEnv.tree = tree; // before, we were not in loop!
        scan(tree.body);
        parentEnv.info.scope.leave();
	childEnvs = List.of(parentEnv);
        parentEnv = savedEnv;
    }

    @Override
    public void visitForLoop(JCForLoop tree) {
	Env<AttrContext> savedEnv = parentEnv;
        parentEnv = parentEnv.dup(parentEnv.tree, parentEnv.info.dup(parentEnv.info.scope.dup()));
        super.scan(tree.init);
        if (tree.cond != null) super.scan(tree.cond);
        parentEnv.tree = tree; // before, we were not in loop!
        super.scan(tree.step);
        super.scan(tree.body);
        parentEnv.info.scope.leave();
	childEnvs = List.of(parentEnv);
        parentEnv = savedEnv;
    }

    @Override
    public void visitLabelled(JCLabeledStatement tree) {
	Env<AttrContext> savedEnv = parentEnv;
	parentEnv = parentEnv.dup(tree);
	super.scan(tree.body);
	childEnvs = List.of(parentEnv);
	parentEnv = savedEnv;
    }

    @Override
    public void visitMethodDef(JCMethodDecl tree) {
	Env<AttrContext> savedEnv = parentEnv;
	MethodSymbol m = tree.sym;

	// Create a new environment with local scope
	// for attributing the method.
	parentEnv = memberEnter.methodEnv(tree, parentEnv);

	if (tree.paramInfo != null) {

	    // Enter all region parameters into the local method scope.
	    for (List<DPJRegionParameter> l = tree.paramInfo.rplParams; l.nonEmpty();
	    	l = l.tail) {
		RegionParameterSymbol sym = l.head.sym;
		parentEnv.info.scope.enterIfAbsent(sym);
	    }

	    // Enter region constraints
	    ListBuffer<Pair<RPL,RPL>> buf = ListBuffer.lb();
	    for (Pair<DPJRegionPathList,DPJRegionPathList> treeConstraint : 
		tree.paramInfo.rplConstraints) {
		Pair<RPL,RPL> constraint = 
			new Pair<RPL,RPL>(treeConstraint.fst.rpl,
				  treeConstraint.snd.rpl);
		buf.append(constraint);
	    }
	    List<Pair<RPL,RPL>> constraints = buf.toList();
	    parentEnv.info.constraints.disjointRPLs =
		parentEnv.info.constraints.disjointRPLs.appendList(constraints);
	    //parentEnv.info.constraintsOld = parentEnv.info.constraints.disjointRPLs;
	}

	// Enter all type parameters into the local method scope.
	for (List<JCTypeParameter> l = tree.typarams; l.nonEmpty(); l = l.tail)
	    parentEnv.info.scope.enterIfAbsent(l.head.type.tsym);

	// Visit value parameters.
	for (List<JCVariableDecl> l = tree.params; l.nonEmpty(); l = l.tail) {
	    super.scan(l.head);
        }

	if (tree.body != null) {
	    // Visit method body.
	    super.scan(tree.body);
	}
	
	parentEnv.info.scope.leave();

	childEnvs = List.of(parentEnv);
	parentEnv = savedEnv;

    }

    @Override
    public void visitNewClass(JCNewClass tree) {
	Env<AttrContext> savedEnv = parentEnv;
	// The local environment of a class creation is
        // a new environment nested in the current one.
	parentEnv = parentEnv.dup(tree, parentEnv.info.dup());
        super.visitNewClass(tree);
	childEnvs = List.of(parentEnv);
        parentEnv = savedEnv;
    }
    
    @Override
    public void visitSwitch(JCSwitch tree) {
	super.scan(tree.selector);
	Env<AttrContext> savedEnv = parentEnv;
        Env<AttrContext> switchEnv =
            parentEnv.dup(tree, parentEnv.info.dup(parentEnv.info.scope.dup()));
        ListBuffer<Env<AttrContext>> buffer = ListBuffer.lb();
        for (List<JCCase> l = tree.cases; l.nonEmpty(); l = l.tail) {
            parentEnv = switchEnv.dup(tree, switchEnv.info.dup(switchEnv.info.scope.dup()));
            super.scan(l.head);
            parentEnv.info.scope.leave();
            buffer.append(parentEnv);
        }        
        switchEnv.info.scope.leave();
	childEnvs = buffer.toList();
	parentEnv = savedEnv;
    }
    
    @Override
    public void visitTry(JCTry tree) {
	// Visit body
	super.scan(tree.body);

	Env<AttrContext> savedEnv = parentEnv;
	// Attribute catch clauses
        for (List<JCCatch> l = tree.catchers; l.nonEmpty(); l = l.tail) {
            JCCatch c = l.head;
            parentEnv = savedEnv.dup(c, savedEnv.info.dup(savedEnv.info.scope.dup()));
            super.scan(c.param);
            super.scan(c.body);
            parentEnv.info.scope.leave();
        }

        // Attribute finalizer
        if (tree.finalizer != null) super.scan(tree.finalizer);
	childEnvs = List.of(parentEnv);
        parentEnv = savedEnv;
    }

    @Override
    public void visitVarDef(JCVariableDecl tree) {
	// Local variables have not been entered yet, so we need to do it now:
        if (parentEnv.info.scope.owner.kind == MTH) {
            if (tree.sym != null)
        	parentEnv.info.scope.enter(tree.sym);
        }

        Env<AttrContext> savedEnv = parentEnv;

        VarSymbol v = tree.sym;
        if (v != null && tree.init != null) {
            if ((v.flags_field & FINAL) != 0 && tree.init.getTag() != JCTree.NEWCLASS) {
        	// Nothing to do
            } else {
        	// Attribute initializer in a new environment
        	// with the declared variable as owner.
        	// Check that initializer conforms to variable's declared type.
        	parentEnv = memberEnter.initEnv(tree, parentEnv);
        	super.scan(tree.init);
            }
        }
        super.visitVarDef(tree);
        childEnvs = List.of(parentEnv);
        parentEnv = savedEnv;
    }
    
    @Override
    public void visitRegionDecl(DPJRegionDecl tree) {
        if (parentEnv.info.scope.owner.kind == MTH) {
            if (tree.sym != null) {
                parentEnv.info.scope.enter(tree.sym);
            }
        }
    }

}
