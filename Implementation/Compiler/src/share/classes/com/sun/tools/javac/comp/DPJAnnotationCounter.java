package com.sun.tools.javac.comp;

import java.io.PrintWriter;
import java.util.HashSet;

import com.sun.tools.javac.parser.Parser;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.tree.JCTree.DPJEffect;
import com.sun.tools.javac.tree.JCTree.DPJParamInfo;
import com.sun.tools.javac.tree.JCTree.DPJRegionApply;
import com.sun.tools.javac.tree.JCTree.DPJRegionDecl;
import com.sun.tools.javac.tree.JCTree.DPJRegionParameter;
import com.sun.tools.javac.tree.JCTree.DPJRegionPathList;
import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCInstanceOf;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCTypeApply;
import com.sun.tools.javac.tree.JCTree.JCTypeCast;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.Pair;
import com.sun.tools.javac.util.Position;

/** A class for counting DPJ annotations in the code
 *  
 *  @author Rob Bocchino
 */
public class DPJAnnotationCounter extends TreeScanner {

    public enum Context { CLASS, METHOD_DEF, METHOD_INVOKE, IN, TYPE, READ_EFFECT, WRITE_EFFECT };

    public boolean badLineMap = false;

    // Basic Java elements
    
    public int classDefCount;
    public int methodDefCount;
    public int methodInvokeCount;
    public int newClassCount;
    public int variableDefCount;
    public int classTypeParamCount;
    public int methodTypeParamCount;
    public int baseClassCount;
    public int typeTypeArgCount;
    public int methodTypeArgCount;
    public int instanceofCount;
    public int castCount;
        
    // DPJ Annotations
    
    public int annotatedLinesCount;
    public int fieldRegionDeclCount;
    public int localRegionDeclCount;
    public int classRegionParamCount;
    public int typeRegionParamCount;
    public int classEffectParamCount;
    public int classRPLConstraintCount;
    public int classEffectConstraintCount;
    public int methodRegionParamCount;
    public int methodEffectParamCount;
    public int methodRPLConstraintCount;
    public int methodEffectConstraintCount;
    public int inRPLArgCount;
    public int typeRPLArgCount;
    public int typeEffectArgCount;
    public int methodRPLArgCount;
    public int methodEffectArgCount;
    public int methodEffectSummaryCount;
    public int readEffectCount;
    public int readEffectRPLCount;
    public int writeEffectCount;
    public int writeEffectRPLCount;
    
    public void printStatistics(PrintWriter out) {
	if (badLineMap) {
	    System.err.println("WARNING: Linemap unavailable for some classes, SLOC count may be inaccurate");
	}
	System.out.println("*** Basic Java Elements ***");
	System.out.println("Class definitions: " + classDefCount);
	// Use the parser to count methods, because the compiler later adds
	// default constructors that weren't in the source file
	System.out.println("Method definitions: " + Parser.methodCount);
	System.out.println("Methods invoked: " + methodInvokeCount);
	System.out.println("New expressions: " + newClassCount);
	System.out.println("Variable definitions: " + variableDefCount);
	System.out.println("Class type parameters: " + classTypeParamCount);
	System.out.println("Method type parameters: " + methodTypeParamCount);
	System.out.println("Base class types: " + baseClassCount);
	System.out.println("Type arguments to types: " + typeTypeArgCount);
	System.out.println("Type arguments to methods: " + methodTypeArgCount);
	System.out.println("Type casts: " + castCount);
	System.out.println("Instanceof expressions: " + instanceofCount);
	System.out.println("*** DPJ Annotations ***");
	System.out.println("Annotated SLOC: "+ annotatedLinesCount);
	System.out.println("Field region declarations: " + fieldRegionDeclCount);
	System.out.println("Local region declarations: " + localRegionDeclCount);
	System.out.println("Class region parameters: " + classRegionParamCount);
	System.out.println("Class RPL constraints: " + classRPLConstraintCount);
	System.out.println("Type region parameters: " + typeRegionParamCount);
	System.out.println("Class effect parameters: " + classEffectParamCount);
	System.out.println("Class effect constraints: " + classEffectConstraintCount);
	System.out.println("Method region parameters: " + methodRegionParamCount);
	System.out.println("Method RPL constraints: " + methodRPLConstraintCount);
	System.out.println("Method effect parameters: " + methodEffectParamCount);
	System.out.println("Method effect constraints: " + methodEffectConstraintCount);
	System.out.println("Effect arguments to types: " + typeEffectArgCount);
	System.out.println("Effect arguments to methods: " + methodEffectArgCount);
	System.out.println("Method effect summaries: " + methodEffectSummaryCount);
	System.out.println("RPL arguments to 'in': " + inRPLArgCount);
	System.out.println("RPL arguments to types: " + typeRPLArgCount);
	System.out.println("RPL arguments to methods: " + methodRPLArgCount);
    }
    
    /** Visitor argument:  The current context
     */
    public Context context;
    
    /** Visitor argument:  Set for tracking annotated lines
     */
    HashSet<Integer> annotatedLines = null;

    /** Visitor argument:  Map from positions to line numbers
     */
    Position.LineMap lineMap = null;

    /** Add the line for the given pos to the set of annotated lines
     *  for the current class.  javac does something funny with lineMaps,
     *  so we add some code to check whether a lineMap wasn't available.
     * @param pos
     */
    void addLineFor(int pos) {
	if (lineMap == null) {
	    badLineMap = true;
	    return;
	}
	annotatedLines.add(lineMap.getLineNumber(pos));
    }
    
    @Override
    public void visitTopLevel(JCCompilationUnit tree) {
	lineMap = tree.getLineMap();
	super.visitTopLevel(tree);
    }
    
    HashSet<JCClassDecl> visited = new HashSet<JCClassDecl>();
    
    @Override
    public void visitClassDef(JCClassDecl tree) {
	// TODO: Why do some classes get visited twice???
	if (!visited.add(tree)) return; 
	++classDefCount;
	if (tree.extending != null)
	    ++baseClassCount;
	if (tree.implementing != null)
	    baseClassCount += tree.implementing.size();
	Context savedContext = context;
	context = Context.CLASS;
	HashSet<Integer> savedAnnotatedLines = annotatedLines;
	annotatedLines = new HashSet<Integer>();
	super.visitClassDef(tree);
	annotatedLinesCount += annotatedLines.size();
	context = savedContext;
	annotatedLines = savedAnnotatedLines;
    }

    @Override
    public void visitTypeParameter(JCTypeParameter tree) {
	int rplParamsCount = tree.rplparams.size();
	if (rplParamsCount != 0) {
	    addLineFor(tree.pos);
	    typeRegionParamCount += rplParamsCount;
	}
	switch (context) {
	case CLASS:
	    ++classTypeParamCount;
	    break;
	case METHOD_DEF:
	    ++methodTypeParamCount;
	    break;
	default:
	    assert false;
	}
    }
    
    @Override
    public void visitMethodDef(JCMethodDecl tree) {
	++methodDefCount;
	Context savedContext = context;
	context = Context.METHOD_DEF;
	super.visitMethodDef(tree);
	context = savedContext;	
    }
    
    @Override
    public void visitApply(JCMethodInvocation tree) {
	++methodInvokeCount;
	methodTypeArgCount += tree.typeargs.size();
	scan(tree.meth);
	Context savedContext = context;
	context = Context.METHOD_INVOKE;
	scan(tree.regionArgs);
	scan(tree.effectargs);
	context = savedContext;
	scan(tree.typeargs);
	scan(tree.args);
    }
    
    @Override
    public void visitParamInfo(DPJParamInfo tree) {
	scan(tree.rplParams);
	for (Pair<DPJRegionPathList,DPJRegionPathList> pair : tree.rplConstraints) {
	    addLineFor(pair.fst.pos);
	    addLineFor(pair.snd.pos);
	}
	for (JCIdent effectParam : tree.effectParams) {
	    addLineFor(effectParam.pos);
	    switch (context) {
	    case CLASS:
		++classEffectParamCount;
		break;
	    case METHOD_DEF:
		++methodEffectParamCount;
		break;
	    default:
		assert false;  
	    }
	}
	for (Pair<DPJEffect,DPJEffect> pair : tree.effectConstraints) {
	    addLineFor(pair.fst.pos);
	    addLineFor(pair.snd.pos);
	}
	switch (context) {
	case CLASS:
	    classRPLConstraintCount += tree.rplConstraints.size();
	    classEffectConstraintCount += tree.effectConstraints.size();
	    break;
	case METHOD_DEF:
	    methodRPLConstraintCount += tree.rplConstraints.size();
	    methodEffectConstraintCount += tree.effectConstraints.size();
	    break;
	default:
	    assert false;
	}
    }

    @Override
    public void visitRegionParameter(DPJRegionParameter tree) {
	addLineFor(tree.pos);
	switch (context) {
	case CLASS:
	    ++classRegionParamCount;
	    break;
	case METHOD_DEF:
	    ++methodRegionParamCount;
	    break;
	default:
	    assert false;
	}
    }
    
    @Override
    public void visitVarDef(JCVariableDecl tree) {
	++variableDefCount;
	Context savedContext = context;
	context = Context.IN;
	super.visitVarDef(tree);
	context = savedContext;		
    }
    
    @Override
    public void visitRPL(DPJRegionPathList tree) {
	addLineFor(tree.pos);
	switch (context) {
	case IN:
	    ++inRPLArgCount;
	    break;
	case TYPE:
	    ++typeRPLArgCount;
	    break;
	case METHOD_INVOKE:
	    ++methodRPLArgCount;
	    break;
	case READ_EFFECT:
	    ++readEffectRPLCount;
	    break;
	case WRITE_EFFECT:
	    ++writeEffectRPLCount;
	    break;
	default:
	    assert false;
	}
    }
    
    @Override
    public void visitRegionDecl(DPJRegionDecl tree) {	
	addLineFor(tree.pos);
	switch (context) {
	case CLASS:
	    ++fieldRegionDeclCount;
	    break;
	case METHOD_DEF:
	    ++localRegionDeclCount;
	    break;
	default:
	    assert false;
	}
    }

    @Override
    public void visitRegionApply(DPJRegionApply tree) {
	Context savedContext = context;
	context = Context.TYPE;
	super.visitRegionApply(tree);
	context = savedContext;			
    }
    
    @Override
    public void visitTypeApply(JCTypeApply tree) {
	typeTypeArgCount += tree.typeArgs.size();
	Context savedContext = context;
	context = Context.TYPE;
	super.visitTypeApply(tree);
	context = savedContext;
    }
    
    @Override 
    public void visitTypeArray(JCArrayTypeTree tree) {
	Context savedContext = context;
	context = Context.TYPE;
	super.visitTypeArray(tree);
	context = savedContext;
    }
    
    @Override
    public void visitEffect(DPJEffect tree) {
	if (tree.isPure || tree.readEffects.nonEmpty() ||
		tree.writeEffects.nonEmpty())
	    addLineFor(tree.pos);
	else
	    return;
	switch (context) {
	case TYPE:
	    ++typeEffectArgCount;
	    break;
	case METHOD_INVOKE:
	    ++methodEffectArgCount;
	    break;
	case METHOD_DEF:
	    ++methodEffectSummaryCount;
	    break;
	default:
	    assert false;
	}
	Context savedContext = context;	
	if (tree.readEffects != null && tree.readEffects.size() > 0) {
	    ++readEffectCount;
	    context = Context.READ_EFFECT;
	    scan(tree.readEffects);
	}
	if (tree.writeEffects != null && tree.writeEffects.size() > 0) {
	    ++writeEffectCount;
	    context = Context.WRITE_EFFECT;
	    scan(tree.writeEffects);
	}
	context = savedContext;
    }
    
    public void visitTypeCast(JCTypeCast tree) {
	++castCount;
	super.visitTypeCast(tree);
    }

    public void visitTypeTest(JCInstanceOf tree) {
	++instanceofCount;
	super.visitTypeTest(tree);
    }

    public void visitNewClass(JCNewClass tree) {
	++newClassCount;
	super.visitNewClass(tree);
    }
}