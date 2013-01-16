/*
 * Copyright 1999-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package com.sun.tools.javac.tree;

import static com.sun.tools.javac.code.Flags.ANNOTATION;
import static com.sun.tools.javac.code.Flags.ARRAYCONSTR;
import static com.sun.tools.javac.code.Flags.DPJStandardFlags;
import static com.sun.tools.javac.code.Flags.ENUM;
import static com.sun.tools.javac.code.Flags.INTERFACE;
import static com.sun.tools.javac.code.Flags.SYNTHETIC;
import static com.sun.tools.javac.code.Flags.VARARGS;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.sun.tools.javac.code.BoundKind;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.TypeTags;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree.DPJAtomic;
import com.sun.tools.javac.tree.JCTree.DPJCobegin;
import com.sun.tools.javac.tree.JCTree.DPJFinish;
import com.sun.tools.javac.tree.JCTree.DPJForLoop;
import com.sun.tools.javac.tree.JCTree.DPJNegationExpression;
import com.sun.tools.javac.tree.JCTree.DPJNonint;
import com.sun.tools.javac.tree.JCTree.DPJParamInfo;
import com.sun.tools.javac.tree.JCTree.DPJRegionDecl;
import com.sun.tools.javac.tree.JCTree.DPJRegionParameter;
import com.sun.tools.javac.tree.JCTree.DPJRegionPathList;
import com.sun.tools.javac.tree.JCTree.DPJRegionPathListElt;
import com.sun.tools.javac.tree.JCTree.DPJSpawn;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCArrayAccess;
import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import com.sun.tools.javac.tree.JCTree.JCAssert;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCAssignOp;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCBreak;
import com.sun.tools.javac.tree.JCTree.JCCase;
import com.sun.tools.javac.tree.JCTree.JCCatch;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCConditional;
import com.sun.tools.javac.tree.JCTree.JCContinue;
import com.sun.tools.javac.tree.JCTree.JCDoWhileLoop;
import com.sun.tools.javac.tree.JCTree.JCEnhancedForLoop;
import com.sun.tools.javac.tree.JCTree.JCErroneous;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCForLoop;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCIf;
import com.sun.tools.javac.tree.JCTree.JCImport;
import com.sun.tools.javac.tree.JCTree.JCInstanceOf;
import com.sun.tools.javac.tree.JCTree.JCLabeledStatement;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCNewArray;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCParens;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
import com.sun.tools.javac.tree.JCTree.JCReturn;
import com.sun.tools.javac.tree.JCTree.JCSkip;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCSwitch;
import com.sun.tools.javac.tree.JCTree.JCSynchronized;
import com.sun.tools.javac.tree.JCTree.JCThrow;
import com.sun.tools.javac.tree.JCTree.JCTry;
import com.sun.tools.javac.tree.JCTree.JCTypeApply;
import com.sun.tools.javac.tree.JCTree.JCTypeCast;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCUnary;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.JCTree.JCWhileLoop;
import com.sun.tools.javac.tree.JCTree.JCWildcard;
import com.sun.tools.javac.tree.JCTree.LetExpr;
import com.sun.tools.javac.tree.JCTree.TypeBoundKind;
import com.sun.tools.javac.util.Convert;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Pair;

/** Prints out a tree as an indented Java source program.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
public class Pretty extends JCTree.Visitor {

    /**
     * No code gen, just pretty print the AST
     */
    public static final int NONE = 0;

    /**
     * Sequential code gen
     */
    public static final int SEQ = 1;
    
    /**
     * Sequential code gen with instrumentation
     */
    public static final int SEQ_INST = 2;
    
    /**
     * Parallel code gen
     */
    public static final int PAR = 3;
    
    /**
     * Code generation mode.  We are using the Pretty printer as a code generator, 
     * so we have to keep track of what kind of code generation we are doing.
     */
    public int codeGenMode = NONE;
    
    /**
     * Set if we need to compile to sequential code.
     */
    public boolean sequential = false;
    
    private Log log;
    
    public Pretty(Writer out, boolean sourceOutput,
	          int codeGenMode) {
        this.out = out;
        this.sourceOutput = sourceOutput;
	this.codeGenMode = codeGenMode;
	switch (codeGenMode) {
	case SEQ_INST:
	case SEQ:
	    this.sequential = true;
	default:
	    break;	
	}
    }
    
    // Note:  The following two methods are a HACK to work around
    // the fact that the javac implementation of enums is broken! Without
    // these flags, we get strange output that is not legal
    // Java code.
    //
    /** Set if we are in an enum block
     */
    public boolean inEnumBlock = false;

    /** Set if we are in an enum variable declaration
     */
    public boolean inEnumVarDecl = false;
    
    /**
     * Set if we need to DPJplicate this
     */
    private boolean thisIsBogus = false;
    
    /** Set when we are producing source output.  If we're not
     *  producing source output, we can sometimes give more detail in
     *  the output even though that detail would not be valid java
     *  source.
     */
    private final boolean sourceOutput;

    /** The output stream on which trees are printed.
     */
    Writer out;

    /** Indentation width (can be reassigned from outside).
     */
    public int width = 4;

    /** The current left margin.
     */
    int lmargin = 0;

    /** The enclosing class name.
     */
    Name enclClassName;

    /** A hashtable mapping trees to their documentation comments
     *  (can be null)
     */
    Map<JCTree, String> docComments = null;

    /** Align code to be indented to left margin.
     */
    void align() throws IOException {
        for (int i = 0; i < lmargin; i++) out.write(" ");
    }

    /** Increase left margin by indentation width.
     */
    void indent() {
        lmargin = lmargin + width;
    }

    /** Decrease left margin by indentation width.
     */
    void undent() {
        lmargin = lmargin - width;
    }

    /** Enter a new precedence level. Emit a `(' if new precedence level
     *  is less than precedence level so far.
     *  @param contextPrec    The precedence level in force so far.
     *  @param ownPrec        The new precedence level.
     */
    void open(int contextPrec, int ownPrec) throws IOException {
        if (ownPrec < contextPrec) out.write("(");
    }

    /** Leave precedence level. Emit a `(' if inner precedence level
     *  is less than precedence level we revert to.
     *  @param contextPrec    The precedence level we revert to.
     *  @param ownPrec        The inner precedence level.
     */
    void close(int contextPrec, int ownPrec) throws IOException {
        if (ownPrec < contextPrec) out.write(")");
    }

    /** Print string, replacing all non-ascii character with unicode escapes.
     */
    public void print(Object s) throws IOException {
        out.write(Convert.escapeUnicode(s.toString()));
    }
    public void printAligned(Object s) throws IOException {
	align();
	print(s);
    }

    /** Print new line.
     */
    public void println() throws IOException {
        out.write(lineSep);
    }

    String lineSep = System.getProperty("line.separator");

    /**************************************************************************
     * Traversal methods
     *************************************************************************/

    /** Exception to propogate IOException through visitXXX methods */
    protected static class UncheckedIOException extends Error {
	static final long serialVersionUID = -4032692679158424751L;
        UncheckedIOException(IOException e) {
            super(e.getMessage(), e);
        }
    }

    /** Visitor argument: the current precedence level.
     */
    int prec;

    /** Visitor method: print expression tree.
     *  @param prec  The current precedence level.
     */
    public void printExpr(JCTree tree, int prec) throws IOException {
        int prevPrec = this.prec;
        try {
            this.prec = prec;
            if (tree == null) print("/*missing*/");
            else {
                tree.accept(this);
            }
        } catch (UncheckedIOException ex) {
            IOException e = new IOException(ex.getMessage());
            e.initCause(ex);
            throw e;
        } finally {
            this.prec = prevPrec;
        }
    }

    /** Derived visitor method: print expression tree at minimum precedence level
     *  for expression.
     */
    public void printExpr(JCTree tree) throws IOException {
        printExpr(tree, TreeInfo.noPrec);
    }

    /** Derived visitor method: print statement tree.
     */
    public void printStat(JCTree tree) throws IOException {
        printExpr(tree, TreeInfo.notExpression);
    }

    /** Derived visitor method: print list of expression trees, separated by given string.
     *  @param sep the separator string
     */
    public <T extends JCTree> void printExprs(List<T> trees, String sep) throws IOException {
        if (trees.nonEmpty()) {
            printExpr(trees.head);
            for (List<T> l = trees.tail; l.nonEmpty(); l = l.tail) {
                print(sep);
                printExpr(l.head);
            }
        }
    }

    /** Derived visitor method: print list of expression trees, separated by commas.
     */
    public <T extends JCTree> void printExprs(List<T> trees) throws IOException {
        printExprs(trees, ", ");
    }

    /** Derived visitor method: print list of statements, each on a separate line.
     */
    public void printStats(List<? extends JCTree> trees) throws IOException {
	for (List<? extends JCTree> l = trees; l.nonEmpty(); l = l.tail) {
            if ((codeGenMode == NONE) || !(l.head instanceof DPJRegionDecl)) {
        	align();
        	printStat(l.head);
        	println();
            }
        }
    }

    public void printCobeginStats(List<? extends JCTree> trees) throws IOException {
	int count = 0;
	for (List<? extends JCTree> l = trees; l.nonEmpty(); l = l.tail) {
            if ((codeGenMode == NONE) || !(l.head instanceof DPJRegionDecl)) {
        	align();
        	printStat(l.head);
        	println();
        	if (++count < l.size()) {
        	    align();
        	    if (codeGenMode == SEQ_INST) print("DPJRuntime.Instrument.cobeginSeparator();");
        	    println();
        	}
            }
        }
    }

    /** Print a set of modifiers.
     */
    public void printFlags(long flags) throws IOException {
	if ((flags & SYNTHETIC) != 0) print("/*synthetic*/ ");
        print(TreeInfo.flagNames(flags));
        if ((flags & DPJStandardFlags) != 0) print(" ");
        if ((flags & ANNOTATION) != 0) print("@");
    }

    public void printAnnotations(List<JCAnnotation> trees) throws IOException {
        for (List<JCAnnotation> l = trees; l.nonEmpty(); l = l.tail) {
            printStat(l.head);
            println();
            align();
        }
    }

    /** Print documentation comment, if it exists
     *  @param tree    The tree for which a documentation comment should be printed.
     */
    public void printDocComment(JCTree tree) throws IOException {
        if (docComments != null) {
            String dc = docComments.get(tree);
            if (dc != null) {
                print("/**"); println();
                int pos = 0;
                int endpos = lineEndPos(dc, pos);
                while (pos < dc.length()) {
                    align();
                    print(" *");
                    if (pos < dc.length() && dc.charAt(pos) > ' ') print(" ");
                    print(dc.substring(pos, endpos)); println();
                    pos = endpos + 1;
                    endpos = lineEndPos(dc, pos);
                }
                align(); print(" */"); println();
                align();
            }
        }
    }
//where
    static int lineEndPos(String s, int start) {
        int pos = s.indexOf('\n', start);
        if (pos < 0) pos = s.length();
        return pos;
    }

    /** Print region parameter constraints
     */
    public void printConstraints(List<Pair<DPJRegionPathList,DPJRegionPathList>> constraints) 
    throws IOException {
        if (constraints.nonEmpty()) {
            print(" | ");
            int count = 0;
            for (Pair<DPJRegionPathList,DPJRegionPathList> pair : constraints) {
        	if (count++ > 0) print(", ");
        	print(pair.fst);
                print(" # ");
                print(pair.snd);        	
            }
        }
    }
    
    /** If parameter list is non-empty, print it enclosed in "<...>" brackets.
     */
    public void printTypeRegionEffectParams(List<JCTypeParameter> typeParams,
    	DPJParamInfo paramInfo) throws IOException {
        if (typeParams.nonEmpty()) {
            print("<");
            printExprs(typeParams);
            print(">");
        }
    }

    /** If there are type or region parameters, print both
     */
    public void printParams(List<JCTypeParameter> typarams, DPJParamInfo rgnparaminfo)
    	throws IOException {
	boolean printRegionParams = ((codeGenMode == NONE) && rgnparaminfo != null && 
		rgnparaminfo.rplParams.nonEmpty()); 
	if (typarams.nonEmpty() || printRegionParams) {
	    print("<");
	    printExprs(typarams);
	    if (printRegionParams) {
		if (typarams.nonEmpty()) print(", ");
		print("region ");
		printExprs(rgnparaminfo.rplParams);
		printConstraints(rgnparaminfo.rplConstraints);
	    }
	    print(">");
	}
    }
    /** Print a block.
     */
    public void printBlock(List<? extends JCTree> stats) throws IOException {
        print("{");
        println();
        indent();
        printStats(stats);
        undent();
        align();
        print("}");
    }

    public void printCobeginBlock(List<? extends JCTree> stats) throws IOException {
        print("{");
        println();
        indent();
        printCobeginStats(stats);
        undent();
        align();
        print("}");
    }

    /** Print a block.
     */
    public void printEnumBody(List<JCTree> stats) throws IOException {
        print("{");
        println();
        indent();
        boolean first = true;
        for (List<JCTree> l = stats; l.nonEmpty(); l = l.tail) {
            if (isEnumerator(l.head)) {
                if (!first) {
                    print(",");
                    println();
                }
                align();
                printStat(l.head);
                first = false;
            }
        }
        print(";");
        println();
        inEnumBlock = true;
        for (List<JCTree> l = stats; l.nonEmpty(); l = l.tail) {
            if (!isEnumerator(l.head)) {
        	align();
                printStat(l.head);
                println();
            }
        }
        inEnumBlock = false;
        undent();
        align();
        print("}");
    }

    /** Is the given tree an enumerator definition? */
    boolean isEnumerator(JCTree t) {
        return t.getTag() == JCTree.VARDEF && (((JCVariableDecl) t).mods.flags & ENUM) != 0;
    }

    /** Print unit consisting of package clause and import statements in toplevel,
     *  followed by class definition. if class definition == null,
     *  print all definitions in toplevel.
     *  @param tree     The toplevel tree
     *  @param cdef     The class definition, which is assumed to be part of the
     *                  toplevel tree.
     */
    public void printUnit(JCCompilationUnit tree, JCClassDecl cdef) throws IOException {
        docComments = tree.docComments;
        printDocComment(tree);
        if (tree.pid != null) {
            print("package ");
            printExpr(tree.pid);
            print(";");
            println();
        }
        boolean firstImport = true;
        for (List<JCTree> l = tree.defs;
        l.nonEmpty() && (cdef == null || l.head.getTag() == JCTree.IMPORT);
        l = l.tail) {
            if (l.head.getTag() == JCTree.IMPORT) {
                JCImport imp = (JCImport)l.head;
                Name name = TreeInfo.name(imp.qualid);
                if (name == name.table.asterisk ||
                        cdef == null ||
                        isUsed(TreeInfo.symbol(imp.qualid), cdef)) {
                    if (firstImport) {
                        firstImport = false;
                        println();
                    }
                    printStat(imp);
                }
            } else {
                printStat(l.head);
            }
        }
        if (cdef != null) {
            printStat(cdef);
            println();
        }
    }
    // where
    boolean isUsed(final Symbol t, JCTree cdef) {
        class UsedVisitor extends TreeScanner {
            public void scan(JCTree tree) {
                if (tree!=null && !result) tree.accept(this);
            }
            boolean result = false;
            public void visitIdent(JCIdent tree) {
                if (tree.sym == t) result = true;
            }
        }
        UsedVisitor v = new UsedVisitor();
        v.scan(cdef);
        return v.result;
    }

    /**************************************************************************
     * Visitor methods
     *************************************************************************/

    public void visitTopLevel(JCCompilationUnit tree) {
        try {
            printUnit(tree, null);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    boolean inImport = false;
    public void visitImport(JCImport tree) {
        try {
            inImport = true;
            print("import ");
            if (tree.staticImport) print("static ");
            printExpr(tree.qualid);
            print(";");
            println();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        finally {
            inImport = false;
        }
    }

    public void visitClassDef(JCClassDecl tree) {
        try {
            println(); align();
            printDocComment(tree);
            printAnnotations(tree.mods.annotations);
            printFlags(tree.mods.flags & ~INTERFACE);
            Name enclClassNamePrev = enclClassName;
            enclClassName = tree.name;
            if ((tree.mods.flags & INTERFACE) != 0) {
                print("interface " + tree.name);
                printParams(tree.typarams, tree.paramInfo);
                if (tree.implementing.nonEmpty()) {
                    print(" extends ");
                    printExprs(tree.implementing);
                }
            } else {
                if ((tree.mods.flags & ENUM) != 0)
                    print("enum " + tree.name);
                else
                    print("class " + tree.name);
                printParams(tree.typarams, tree.paramInfo);
                if (tree.extending != null) {
                    print(" extends ");
                    printExpr(tree.extending);
                }
                if (tree.implementing.nonEmpty()) {
                    print(" implements ");
                    printExprs(tree.implementing);
                }
            }
            print(" ");
            if ((tree.mods.flags & ENUM) != 0) {
                printEnumBody(tree.defs);
            } else {
                printBlock(tree.defs);
            }
            enclClassName = enclClassNamePrev;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitMethodDef(JCMethodDecl tree) {
        try {
            // when producing source output, omit anonymous constructors
            if (tree.name == tree.name.table.init &&
                    (enclClassName == null ||
                	    enclClassName.toString().equals("")) &&
                    sourceOutput) return;
            println(); align();
            printDocComment(tree);
            printExpr(tree.mods);
            printParams(tree.typarams, tree.paramInfo);
            if (tree.name == tree.name.table.init) {
        	print(enclClassName != null ? enclClassName : tree.name);
            } else {
                printExpr(tree.restype);
                print(" " + tree.name);
            }
            print("(");
            printExprs(tree.params);
            print(")");
            if (tree.effects != null && (codeGenMode == NONE)) {
        	if (tree.effects.isPure) {
        	    print(" pure");          
        	} else {        	    
        	    if (tree.effects.readEffects.nonEmpty()) {
        		print(" reads ");
        		printExprs(tree.effects.readEffects);
        	    }
        	    if (tree.effects.writeEffects.nonEmpty()) {
        		print(" writes ");
        		printExprs(tree.effects.writeEffects);
        	    }
        	}
            }
            if (tree.thrown.nonEmpty()) {
                print(" throws ");
                printExprs(tree.thrown);
            }
            if (tree.body != null) {
                print(" ");
                printStat(tree.body);
            } else {
                print(";");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitVarDef(JCVariableDecl tree) {
        try {
            if (docComments != null && docComments.get(tree) != null) {
                println(); align();
            }
            printDocComment(tree);
            if ((tree.mods.flags & ENUM) != 0) {
        	//print("/*public static final*/ ");
                print(tree.name);
                if (tree.init != null) {
                    //print(" /* = ");
                    inEnumVarDecl = true;
                    printExpr(tree.init);
                    inEnumVarDecl = false;
                    //print(" */");
                }
            } else {
                printExpr(tree.mods);
                if ((tree.mods.flags & VARARGS) != 0) {
                    printExpr(((JCArrayTypeTree) tree.vartype).elemtype);
                    print("... " + tree.name);
                } else {
                    printExpr(tree.vartype);
                    print(" " + tree.name);
                }
                // DPJ BEGIN
                if ((codeGenMode == NONE) && tree.rpl != null && !tree.rpl.elts.isEmpty()) {
                    print(" in ");
                    print(tree.rpl);
                }
                // DPJ END
                if (tree.init != null) {
                    print(" = ");
                    printExpr(tree.init);
                }
                if (prec == TreeInfo.notExpression) print(";");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitRegionDecl(DPJRegionDecl tree) { // DPJ -- based on visitVarDef
        try {
            if (docComments != null && docComments.get(tree) != null) {
                println(); align();
            }
            printDocComment(tree);
            printExpr(tree.mods);
            print("region ");
            if (tree.isAtomic)
        	print("atomic ");
            print(tree.name);
            print(";");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitRPLElt(DPJRegionPathListElt tree) { // DPJ
	try {
	    switch (tree.type) {
	    case DPJRegionPathListElt.STAR:
		print("*");
		break;
	    case DPJRegionPathListElt.NAME:
	   // case DPJRegionPathListElt.FIELD_REGION:
		print(tree.exp);
		break;
	    case DPJRegionPathListElt.ARRAY_INDEX:
		print("[");
		print(tree.exp);
		print("]");
		break;
	    case DPJRegionPathListElt.ARRAY_UNKNOWN:
		print("[?]");
		break;
	    default:
		print("Unknown RPL element");
	    	break;
	    }
	} catch (IOException e) {	    
	    throw new UncheckedIOException(e);
	}
    }	

    public void visitRPL(DPJRegionPathList tree) { // DPJ
	try {
	    int size = tree.elts.size();
	    int idx = 0;
	    for (Iterator<DPJRegionPathListElt> I = tree.elts.iterator();
        	I.hasNext(); )  {
		visitRPLElt(I.next());
		if (++idx < size) print(":");
	    }
	} catch (IOException e) {	    
	    throw new UncheckedIOException(e);
	}
    }	

    public void visitSkip(JCSkip tree) {
        try {
            print(";");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitBlock(JCBlock tree) {
        try {
            printFlags(tree.flags);
            printBlock(tree.stats);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitDoLoop(JCDoWhileLoop tree) {
        try {
            print("do ");
            printStat(tree.body);
            align();
            print(" while ");
            if (tree.cond.getTag() == JCTree.PARENS) {
                printExpr(tree.cond);
            } else {
                print("(");
                printExpr(tree.cond);
                print(")");
            }
            print(";");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitWhileLoop(JCWhileLoop tree) {
        try {
            print("while ");
            if (tree.cond.getTag() == JCTree.PARENS) {
                printExpr(tree.cond);
            } else {
                print("(");
                printExpr(tree.cond);
                print(")");
            }
            print(" ");
            printStat(tree.body);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitForLoop(JCForLoop tree) {
        try {
            print("for (");
            if (tree.init.nonEmpty()) {
                if (tree.init.head.getTag() == JCTree.VARDEF) {
                    printExpr(tree.init.head);
                    for (List<JCStatement> l = tree.init.tail; l.nonEmpty(); l = l.tail) {
                        JCVariableDecl vdef = (JCVariableDecl)l.head;
                        print(", " + vdef.name + " = ");
                        printExpr(vdef.init);
                    }
                } else {
                    printExprs(tree.init);
                }
            }
            print("; ");
            if (tree.cond != null) printExpr(tree.cond);
            print("; ");
            printExprs(tree.step);
            print(") ");
            printStat(tree.body);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitForeachLoop(JCEnhancedForLoop tree) {
        try {
            print("for (");
            printExpr(tree.var);
            print(" : ");
            printExpr(tree.expr);
            print(") ");
            printStat(tree.body);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void seqDPJForLoop(DPJForLoop tree) {
        try {
            if (codeGenMode == SEQ_INST) {
        	print("DPJRuntime.Instrument.enterForeach(");
        	if (tree.length != null)
        	    print(tree.length);
        	else
        	    print(tree.start + ".size()");
        	print(");");
        	println();
        	align();
            }
            int depth = lmargin / width;
            if (tree.length == null) {
        	// Iterator form of foreach
        	print("{\n");
        	indent();
        	align();
        	printType(tree.start.type);
        	print(" ");
                print("i_"); print(depth); print(" = ");
        	printExpr(tree.start);
        	print(";\n");
        	align();
        	print("DPJIterator.Status<"+tree.var.type+
        		"> status_"+depth+"=null;\n");
        	align();
        	print ("while ((status_"+depth+"=i_"+depth+
        		".next()).hasElement()) {\n");
            } else {
        	// Indexed form of foreach
        	print("for (");
        	long flags = tree.var.mods.flags;
        	tree.var.mods.flags &= ~Flags.FINAL;
        	printExpr(tree.var);
        	tree.var.mods.flags = flags;
        	print(" = ");
        	printExpr(tree.start);
        	print(", ");
                print("i_"); print(depth); print(" = ");
        	print("0");
                print("; i_"); print(depth);
		print(" < ");
		printExpr(tree.length);
		print("; ");
		print(tree.var.name);
		if (tree.stride != null) {
		    print(" += ");
		    printExpr(tree.stride);
		} else {
		    print("++");
		}
		print(", ++i_"); print(depth);
		print(") {\n");
            }
            indent();
            align();
	    if (codeGenMode == SEQ_INST) {
		print("DPJRuntime.Instrument.enterForeachIter();\n");
		align();
	    }
	    if (tree.length == null) {
		printExpr(tree.var);
		print(" = ");
		print("status_"); print(depth);
		print(".getElement();\n");
		align();
	    }
	    printStat(tree.body);
	    println();
	    if(codeGenMode == SEQ_INST) {
		align();
		print("DPJRuntime.Instrument.exitForeachIter();\n");
	    }
	    undent();
	    align();
	    print("}\n");
	    if (tree.length == null) {
		undent();
		align();
		print("}\n");
	    }
	    align();
	    if(codeGenMode == SEQ_INST) print("DPJRuntime.Instrument.exitForeach();");
	} catch (IOException e) {
	    throw new UncheckedIOException(e);
	} finally {
	    Types.printDPJ = true;
	}
    }

    private void parDPJForLoop(DPJForLoop tree) {
	try {
	    Types.printDPJ = false;
	    List<String> toCoInvoke = List.<String>nil();
	    List<String> copyOutAssign = List.<String>nil();
	    println();
	    String stName = "__dpj_S"+dpj_tname++;
	    printAligned("class " + stName + " extends RecursiveAction {\n");
	    indent();
	    printAligned("int __dpj_begin;\n");
	    printAligned("int __dpj_length;\n");
	    printAligned("int __dpj_stride;\n");
	    align();
	    Set<VarSymbol> copyIn = new HashSet(tree.usedVars);
	    copyIn.removeAll(tree.declaredVars);
	    Set<VarSymbol> copyOut = new HashSet(tree.definedVars);
	    copyOut.removeAll(tree.declaredVars);
	    if(copyOut.size()>0 && !tree.isNondet)
		// In real life this should have been caught by the type checker.
		print("Error: Assignment inside foreach to local variable declared prior to foreach\n");
	    
	    // Don't copy field values in/out, which would interfere with the STM system.
	    // TODO Is this the best way to handle this issue?
	    if (tree.isNondet) {
		Set<VarSymbol> dontInclude = new HashSet<VarSymbol>();
		for (VarSymbol var : copyIn)
		    if (var.owner.kind != Kinds.MTH && !var.toString().equals("this"))
			dontInclude.add(var);
		for (VarSymbol var : copyOut)
		    if (var.owner.kind != Kinds.MTH && !var.toString().equals("this"))
			dontInclude.add(var);
		copyIn.removeAll(dontInclude);
		copyOut.removeAll(dontInclude);
	    }
	    
	    Set<VarSymbol> copyAll = new HashSet(copyIn);
	    copyAll.addAll(copyOut);
	    
	    // Declare local vars necessary for copyin/copyout
	    for(VarSymbol var : copyAll) {
		printType(var.type);
		print(" ");
		print(varString(var)+";\n");
		align();
	    }
	    
	    //Generate constructor for class
	    print(stName+"(int __dpj_begin, int __dpj_length, int __dpj_stride");
	    for(VarSymbol var : copyIn) {
		print(", ");
		printType(var.type);
		print(" "+varString(var));
	    }
	    
	    print(") {\n");
	    indent(); align();
	    print("this.__dpj_begin = __dpj_begin;\n"); align();
	    print("this.__dpj_length = __dpj_length;\n"); align();
	    print("this.__dpj_stride = __dpj_stride;\n");
	    for(VarSymbol var : copyIn) {
		align();
		print("this."+varString(var)+"="+varString(var)+";\n");
	    }
	    undent();
	    align();
	    print("}\n");
	    
	    //Generate run method
	    printAligned("protected void compute() {\n");
	    indent();
	    printAligned("if((__dpj_length / __dpj_stride) > DPJRuntime.RuntimeState.dpjForeachCutoff) {\n");
	    indent();
	    printAligned("RecursiveAction[] __dpj_splits = new RecursiveAction[DPJRuntime.RuntimeState.dpjForeachSplit];\n");
	    printAligned("for(int i=0; i<DPJRuntime.RuntimeState.dpjForeachSplit; i++)\n");
	    indent();
	    printAligned("__dpj_splits[i] = new "+stName+"(__dpj_begin + (__dpj_length/DPJRuntime.RuntimeState.dpjForeachSplit)*i, (i+1==DPJRuntime.RuntimeState.dpjForeachSplit) ? (__dpj_length -  __dpj_length/DPJRuntime.RuntimeState.dpjForeachSplit*i) : (__dpj_length/DPJRuntime.RuntimeState.dpjForeachSplit), __dpj_stride");
	    for(VarSymbol var : copyIn)
		print(", "+varString(var));
	    print(");\n");
	    undent();
	    printAligned("RecursiveAction.forkJoin(__dpj_splits);\n");
	    undent();
	    printAligned("}\n");
	    printAligned("else {\n");
	    indent();
	    long flags = tree.var.mods.flags;
	    tree.var.mods.flags &= ~Flags.FINAL;
	    printAligned("for("+tree.var.toString()+" = __dpj_begin; "+tree.var.sym.toString()+" < __dpj_begin + __dpj_length * __dpj_stride; "+tree.var.sym.toString()+"+=__dpj_stride)\n");
	    tree.var.mods.flags = flags;
	    indent();
	    align();
	    boolean wasBogus = thisIsBogus;
	    thisIsBogus=true;
	    printStat(tree.body);
	    thisIsBogus=wasBogus;
	    undent();
	    printAligned("}\n"); //end else
	    undent();
	    printAligned("}\n"); //end run
	    undent();
	    printAligned("};\n"); //close class block
	    
	    //Okay, now generate the actual invocation
	    align();
	    print("if(!DPJRuntime.RuntimeState.insideParallelTask) {\n");
	    indent();
	    printAligned("DPJRuntime.RuntimeState.insideParallelTask = true;\n");
	    printAligned("DPJRuntime.RuntimeState.pool.invoke(new "+stName+"("+tree.start.toString()+", "+(tree.length==null ? tree.start.toString()+".size()" : tree.length.toString())+", "+(tree.stride==null ? "1" : tree.stride.toString()));
	    for(VarSymbol var : copyIn)
		{
		    align();
		    if(var.toString().equals("this") && !thisIsBogus)
			print(", this");
		    else
			print(", "+varString(var));
		}
	    print("));\n");
	    align();
	    print("DPJRuntime.RuntimeState.insideParallelTask = false;\n");
	    undent();
	    align();
	    print("}\n");
	    align();
	    print("else\n");
	    indent();
	    printAligned("(new "+stName+"("+tree.start.toString()+", "+
		    (tree.length==null ?
			    tree.start.toString()+".size()" : 
				tree.length.toString()) + 
				", "+(tree.stride==null ? "1" : tree.stride.toString()));
	    for(VarSymbol var : copyIn)
		{
		    align();
		    if(var.toString().equals("this") && !thisIsBogus)
			print(", this");
		    else
			print(", "+varString(var));
		}
	    print(")).forkJoin();\n");
	    undent();
	}
	catch(IOException e) {
	    throw new UncheckedIOException(e);
	}
	finally {
	    Types.printDPJ = true;
	}
    }

    private String varString(VarSymbol maybeThis) {
	if(maybeThis.toString().equals("this"))
	    return "__dpj_this";
	else
	    return maybeThis.toString();
    }
    
    public void visitDPJForLoop(DPJForLoop tree) {
	if (codeGenMode == NONE) {
	    try {
		if (tree.isNondet) {
		    print("foreach_nd (");
		} else {
		    print("foreach (");
		}
		long flags = tree.var.mods.flags;
		tree.var.mods.flags &= ~Flags.FINAL;
		printExpr(tree.var);
		tree.var.mods.flags = flags;
		print(" in ");
		printExpr(tree.start);
		if (tree.length != null) {
		    print(", ");
		    printExpr(tree.length);
		}
		if (tree.stride != null) {
		    print(", ");
		    printExpr(tree.stride);
		}
		print(") ");
		printStat(tree.body);
	    }
	    catch(IOException e) {
		throw new UncheckedIOException(e);
	    }
	} else if(sequential || tree.length==null) {
	    seqDPJForLoop(tree);
	} else {
	    parDPJForLoop(tree);
	}
    }
    
    public void visitLabelled(JCLabeledStatement tree) {
        try {
            print(tree.label + ": ");
            printStat(tree.body);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitSwitch(JCSwitch tree) {
        try {
            print("switch ");
            if (tree.selector.getTag() == JCTree.PARENS) {
                printExpr(tree.selector);
            } else {
                print("(");
                printExpr(tree.selector);
                print(")");
            }
            print(" {");
            println();
            printStats(tree.cases);
            align();
            print("}");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitCase(JCCase tree) {
        try {
            if (tree.pat == null) {
                print("default");
            } else {
                print("case ");
                printExpr(tree.pat);
            }
            print(": ");
            println();
            indent();
            printStats(tree.stats);
            undent();
            align();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitSynchronized(JCSynchronized tree) {
        try {
            print("synchronized ");
            if (tree.lock.getTag() == JCTree.PARENS) {
                printExpr(tree.lock);
            } else {
                print("(");
                printExpr(tree.lock);
                print(")");
            }
            print(" ");
            printStat(tree.body);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitTry(JCTry tree) {
        try {
            print("try ");
            printStat(tree.body);
            for (List<JCCatch> l = tree.catchers; l.nonEmpty(); l = l.tail) {
                printStat(l.head);
            }
            if (tree.finalizer != null) {
                print(" finally ");
                printStat(tree.finalizer);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitCatch(JCCatch tree) {
        try {
            print(" catch (");
            printExpr(tree.param);
            print(") ");
            printStat(tree.body);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitConditional(JCConditional tree) {
        try {
            open(prec, TreeInfo.condPrec);
            printExpr(tree.cond, TreeInfo.condPrec);
            print(" ? ");
            printExpr(tree.truepart, TreeInfo.condPrec);
            print(" : ");
            printExpr(tree.falsepart, TreeInfo.condPrec);
            close(prec, TreeInfo.condPrec);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitIf(JCIf tree) {
        try {
            print("if ");
            if (tree.cond.getTag() == JCTree.PARENS) {
                printExpr(tree.cond);
            } else {
                print("(");
                printExpr(tree.cond);
                print(")");
            }
            print(" ");
            printStat(tree.thenpart);
            if (tree.elsepart != null) {
                print(" else ");
                printStat(tree.elsepart);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitExec(JCExpressionStatement tree) {
	// Get rid of bogus super() invocation in enum constructor
	if (inEnumBlock && tree.expr instanceof JCMethodInvocation) {
	    JCMethodInvocation mi = (JCMethodInvocation) tree.expr;
	    if (mi.meth instanceof JCIdent) {
		JCIdent id = (JCIdent) mi.meth;
		if (id.name.toString().equals("super"))
		    return;
	    }
	}		
	try {
            printExpr(tree.expr);
            if (prec == TreeInfo.notExpression) print(";");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitBreak(JCBreak tree) {
        try {
            print("break");
            if (tree.label != null) print(" " + tree.label);
            print(";");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitNegationExpression(DPJNegationExpression tree) {
	try {
	    print("~ ");
	    print(tree.negatedExpr);
	} catch (IOException e) {
	    throw new UncheckedIOException(e);
	}
    }
    
    public void visitContinue(JCContinue tree) {
        try {
            print("continue");
            if (tree.label != null) print(" " + tree.label);
            print(";");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitReturn(JCReturn tree) {
        try {
            print("return");
            if (tree.expr != null) {
                print(" ");
                printExpr(tree.expr);
            }
            print(";");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitThrow(JCThrow tree) {
        try {
            print("throw ");
            printExpr(tree.expr);
            print(";");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitAssert(JCAssert tree) {
        try {
            print("assert ");
            printExpr(tree.cond);
            if (tree.detail != null) {
                print(" : ");
                printExpr(tree.detail);
            }
            print(";");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void printArgs(List<JCExpression> typeargs, List<DPJRegionPathList> rplargs,
	    boolean printKeyword) throws IOException {
        boolean rplsToPrint = ((codeGenMode == NONE) && rplargs.nonEmpty());
        if (!typeargs.isEmpty() || rplsToPrint) {
            print("<");
            printExprs(typeargs);
            if (typeargs.nonEmpty() && rplsToPrint)
        	print(", ");
            if (rplsToPrint && printKeyword) print("region ");
            printExprs(rplargs);
            print(">");
        }
    }
    
    public void visitApply(JCMethodInvocation tree) {
	try {
            if (!tree.typeargs.isEmpty() ||
        	    !tree.regionArgs.isEmpty()) {
                if (tree.meth.getTag() == JCTree.SELECT) {
                    JCFieldAccess left = (JCFieldAccess)tree.meth;
                    printExpr(left.selected);
                    print(".");
                    printArgs(tree.typeargs, tree.regionArgs, true);
                    print(left.name);
                } else {
                    printArgs(tree.typeargs, tree.regionArgs, true);
                    printExpr(tree.meth);
                }
            } else {
                printExpr(tree.meth);
            }
            print("(");
            printExprs(tree.args);
            print(")");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void printArrayConstructor(Type cellType, List<JCExpression> args)
            throws IOException {
        if (cellType instanceof ClassType) {
            ClassType ct = (ClassType) cellType;
            if (ct.cellType != null) {
                printArrayConstructor(ct.cellType, args);
                print("[]");
                return;
            }
        }
        print("new ");
        printType(cellType);
        print("[");
        printExprs(args);
        print("]");
    }

    public void visitNewClass(JCNewClass tree) {
        try {
            if (tree.encl != null) {
                printExpr(tree.encl);
                print(".");
            }
            if ((tree.constructor.flags() & ARRAYCONSTR) != 0) {
                // Convert array constructor call to regular Java array         
		ClassType ct = (ClassType) tree.clazz.type;
                printArrayConstructor(ct.cellType, tree.args);
                return;
            }
            if (!inEnumVarDecl) {
        	print("new ");
        	boolean rplsToPrint = ((codeGenMode == NONE) &&
        		tree.regionArgs.nonEmpty());
        	if (rplsToPrint || !tree.typeargs.isEmpty()) {
        	    print("<");
        	    printExprs(tree.typeargs);
        	    if (rplsToPrint) {
        		if (tree.typeargs.nonEmpty()) print(", ");
        		printExprs(tree.regionArgs);
        	    }
        	    print(">");
        	}
        	printExpr(tree.clazz);
            }
            print("(");
            printExprs(tree.args);
            print(")");
            if (tree.def != null) {
                Name enclClassNamePrev = enclClassName;
                enclClassName =
                        tree.def.name != null ? tree.def.name :
                            tree.type != null && tree.type.tsym.name != tree.type.tsym.name.table.empty ? tree.type.tsym.name :
                                null;
                //if ((tree.def.mods.flags & Flags.ENUM) != 0) print("/*enum*/");
                printBlock(tree.def.defs);
                enclClassName = enclClassNamePrev;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitNewArray(JCNewArray tree) {
        try {
            if (tree.elemtype != null) {
                print("new ");
                JCTree elem = tree.elemtype;
                if (elem instanceof JCArrayTypeTree)
                    printBaseElementType((JCArrayTypeTree) elem);
                else
                    printExpr(elem);
                List<DPJRegionPathList> rl = tree.rpls;
                List<JCIdent> indexVars = tree.indexVars;
                for (List<JCExpression> l = tree.dims; l.nonEmpty(); l = l.tail) {
                    print("[");
                    printExpr(l.head);
                    print("]");
                    if ((codeGenMode == NONE) && rl.head != null) {
                	print("<");
                	printExpr(rl.head);
                	print(">");
                    }
                    if ((codeGenMode == NONE) && indexVars.head != null) {
                	print("#");
                    	printExpr(indexVars.head);
                    }
                    rl = rl.tail;
                    indexVars = indexVars.tail;
                }
                if (elem instanceof JCArrayTypeTree)
                    printBrackets((JCArrayTypeTree) elem);
            }
            if (tree.elems != null) {
                if (tree.elemtype != null) print("[]");
                print("{");
                printExprs(tree.elems);
                print("}");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitParens(JCParens tree) {
        try {
            print("(");
            printExpr(tree.expr);
            print(")");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    public void visitSpawn(DPJSpawn tree) {
	try {
	    if (codeGenMode == NONE) {
		print("spawn ");
	    } else {
		print("{\n");
		indent();
		align();
		if(codeGenMode == SEQ_INST) print("DPJRuntime.Instrument.enterSpawn();");
		println();
		align();
	    }
	    printStat(tree.body);
	    if (codeGenMode != NONE) {
		println();
		align();
		if(codeGenMode == SEQ_INST) print("DPJRuntime.Instrument.exitSpawn();\n");
		undent();
		align();
		print("}");
	    }
	} catch (IOException e) {
	    throw new UncheckedIOException(e);
	}
    }

    public void visitFinish(DPJFinish tree) {
	try {
	    if (codeGenMode == NONE) {
		print("dpjfinish ");
	    } else {
		if(codeGenMode == SEQ_INST) print("DPJRuntime.Instrument.enterFinish();\n");
		align();
	    }
	    printStat(tree.body);
	    if (codeGenMode != NONE) {
		println();
		align();
		if(codeGenMode == SEQ_INST) print("DPJRuntime.Instrument.exitFinish();");
	    }
	} catch (IOException e) {
	    throw new UncheckedIOException(e);
	}
    }
    
    private int dpj_tname = 0;    
    public void visitCobegin(DPJCobegin tree) {
	if (codeGenMode == NONE) {
	    try {
		if (tree.isNondet) {
		    print("cobegin_nd ");
		} else {
		    print("cobegin ");
		}
		printStat(tree.body);
	    } catch (IOException e) {
		throw new UncheckedIOException(e);
	    }
	} else if(sequential || !(tree.body instanceof JCBlock)) {
	    seqCobegin(tree);
	} else {
	    parCobegin(tree);
	}	
    }
    
    public void visitAtomic(DPJAtomic tree) {
	try {
	    if (codeGenMode == NONE) {
		print("atomic ");
	    } else if (codeGenMode == PAR) {
		//TODO Use javac's normal error logging
		throw new Error("Nondeterministic constructs are not supported");
	    }
	    printStat(tree.body);
	} catch(IOException e) {
	    throw new UncheckedIOException(e);
	}
    }
    
    public void visitNonint(DPJNonint tree) {
	try {
	    if (codeGenMode == NONE) {
		print("nonint ");
	    }
	    printStat(tree.body);
	} catch(IOException e) {
	    throw new UncheckedIOException(e);
	}
    }

    public void seqCobegin(DPJCobegin tree) {
	try {
	    if(codeGenMode == SEQ_INST) print("DPJRuntime.Instrument.enterCobegin();\n");
	    align();
	    if (tree.body instanceof JCBlock) {
		JCBlock block = (JCBlock) tree.body;
		try {
	            printFlags(block.flags);
	            printCobeginBlock(block.stats);
	        } catch (IOException e) {
	            throw new UncheckedIOException(e);
	        }
	    } else {
		printStat(tree.body);
	    }
	    println();
	    align();
	    if(codeGenMode == SEQ_INST) print("DPJRuntime.Instrument.exitCobegin();");
	} catch (IOException e) {
	    throw new UncheckedIOException(e);
	}
    }

    public void parCobegin(DPJCobegin tree) {
	boolean savedPrintDPJ = Types.printDPJ;
	Types.printDPJ = false;
	try {
	    JCBlock body = (JCBlock)(tree.body);
	    List<String> toCoInvoke = List.<String>nil();
	    List<String> copyOutAssign = List.<String>nil();
	    String arr = "__dpj_s"+dpj_tname++;
	    int orig_dpj_tname = dpj_tname;
	    println();
	    int i=0;
	    for(JCStatement statement : body.stats)
	    {
		align();
		String stName = "__dpj_S"+dpj_tname++;
		print("class " + stName + " extends RecursiveAction {\n");
		indent();
		Set<VarSymbol> copyIn = new HashSet(tree.usedVars[i]);
		copyIn.removeAll(tree.declaredVars[i]);
		Set<VarSymbol> copyOut = new HashSet(tree.definedVars[i]);
		copyOut.removeAll(tree.declaredVars[i]);
		// TODO:  Don't put 'this' in in the first place!
		for (VarSymbol vs : copyIn) {
		     if (vs.name.toString().equals("this")) {
			 copyIn.remove(vs);
			 copyOut.remove(vs);
			 break;
		     }
		}
		Set<VarSymbol> copyAll = new HashSet(copyIn);
		copyAll.addAll(copyOut);

		
		//Declare local vars necessary for copyin/copyout
		for(VarSymbol var : copyAll)
		{
		    align();
		    printType(var.type);
		    print(" ");
		    print(var.toString()+";\n");
		}
		
		//Generate constructor for class
		align();
		print(stName+"(");
		boolean needsComma = false;
		for(VarSymbol var : copyIn) {
		    if (needsComma)
			print(",");
		    else
			needsComma=true;
		    printType(var.type);
		    print(" ");
		    print(var.toString());
		}
		print(") {\n");
		indent();
		for(VarSymbol var : copyIn)
		{
		    align();
		    print("this."+var.toString()+"="+var.toString()+";\n");
		}
		undent();
		align();
		print("}\n");
		
		//Generate run method
		align();
		print("protected void compute() {\n");
		indent();
		align();
		printOwner = true;
		printStat(statement);
		printOwner = false;
		println();
		undent();
		align();
		print("}\n");
		
		//close class block
		undent();
		align();
		print("};\n");
		
		//generate code for FJTask object, for use later
		String varList="";
		needsComma=false;
		for(VarSymbol var : copyIn)
		{
		    if (needsComma)
			varList+=",";
		    else
			needsComma=true;
		    varList+=var.toString();
		}
		toCoInvoke = toCoInvoke.append("new "+stName+"("+varList+")");
		
		//generate code for copy out assignment, for use later
		for(VarSymbol var : copyOut)
		{
		    String stmt=var.toString()+" = (("+stName+")("+arr+
		    	"["+(dpj_tname - 1 - orig_dpj_tname)+"]))."+
		    	var.toString()+";\n";
		    copyOutAssign = copyOutAssign.append(stmt);
		}
		
		i++;
	    }
	    
	    //Okay, now generate the actual array and coInvoke call
	    align();
	    print("RecursiveAction[] "+arr+" = {");
	    boolean needsComma=false;
	    for(String toPrint : toCoInvoke)
	    {
		if (needsComma)
		    print(",");
		else
		    needsComma=true;
		print(toPrint);
	    }
	    print("};\n");
	    align();
	    int cobegin_wrapper = dpj_tname++;
	    print("class __dpj_S"+cobegin_wrapper+" extends RecursiveAction {\n");
	    indent();
	    align();
	    print("RecursiveAction[] __dpj_toforkjoin;\n");
	    align();
	    print("__dpj_S"+cobegin_wrapper+"(RecursiveAction[] __dpj_toforkjoin_) {\n");
	    indent();
	    align();
	    print("__dpj_toforkjoin = __dpj_toforkjoin_;\n");
	    undent();
	    align();
	    print("}\n");
	    align();
	    print("protected void compute() {\n");
	    indent();
	    align();
	    print("RecursiveAction.forkJoin(__dpj_toforkjoin);\n");
	    undent();
	    align();
	    print("}\n"); //end compute
	    undent();
	    align();
	    print("};\n"); //end cobegin_wrapper class
	    align();
	    print("if(!DPJRuntime.RuntimeState.insideParallelTask) {\n");
	    indent();
	    align();
            print("DPJRuntime.RuntimeState.insideParallelTask = true;\n");
	    align();
	    print("DPJRuntime.RuntimeState.pool.invoke(new __dpj_S"+cobegin_wrapper+"("+arr+"));\n");
	    undent();
	    align();
	    print("DPJRuntime.RuntimeState.insideParallelTask = false;\n");
	    align();
            print("}\n");
	    align();
	    print("else\n");
	    indent();
	    align();
	    print("RecursiveAction.forkJoin("+arr+");\n");
	    undent();
	    
	    //Generate copy out assignments
	    for(String assign : copyOutAssign)
	    {
		align();
		print(assign);
	    }
	}
	catch(IOException e) {
	    throw new UncheckedIOException(e);
	}
	finally {
	    Types.printDPJ = savedPrintDPJ;
	}
    }
	
    public void visitAssign(JCAssign tree) {
        try {
            open(prec, TreeInfo.assignPrec);
            printExpr(tree.lhs, TreeInfo.assignPrec + 1);
            print(" = ");
            printExpr(tree.rhs, TreeInfo.assignPrec);
            close(prec, TreeInfo.assignPrec);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String operatorName(int tag) {
        switch(tag) {
            case JCTree.POS:     return "+";
            case JCTree.NEG:     return "-";
            case JCTree.NOT:     return "!";
            case JCTree.COMPL:   return "~";
            case JCTree.PREINC:  return "++";
            case JCTree.PREDEC:  return "--";
            case JCTree.POSTINC: return "++";
            case JCTree.POSTDEC: return "--";
            case JCTree.NULLCHK: return "<*nullchk*>";
            case JCTree.OR:      return "||";
            case JCTree.AND:     return "&&";
            case JCTree.EQ:      return "==";
            case JCTree.NE:      return "!=";
            case JCTree.LT:      return "<";
            case JCTree.GT:      return ">";
            case JCTree.LE:      return "<=";
            case JCTree.GE:      return ">=";
            case JCTree.BITOR:   return "|";
            case JCTree.BITXOR:  return "^";
            case JCTree.BITAND:  return "&";
            case JCTree.SL:      return "<<";
            case JCTree.SR:      return ">>";
            case JCTree.USR:     return ">>>";
            case JCTree.PLUS:    return "+";
            case JCTree.MINUS:   return "-";
            case JCTree.MUL:     return "*";
            case JCTree.DIV:     return "/";
            case JCTree.MOD:     return "%";
            default: throw new Error();
        }
    }

    public void visitAssignop(JCAssignOp tree) {
        try {
            open(prec, TreeInfo.assignopPrec);
            printExpr(tree.lhs, TreeInfo.assignopPrec + 1);
            print(" " + operatorName(tree.getTag() - JCTree.ASGOffset) + "= ");
            printExpr(tree.rhs, TreeInfo.assignopPrec);
            close(prec, TreeInfo.assignopPrec);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitUnary(JCUnary tree) {
        try {
            int ownprec = TreeInfo.opPrec(tree.getTag());
            String opname = operatorName(tree.getTag());
            open(prec, ownprec);
            if (tree.getTag() <= JCTree.PREDEC) {
                print(opname);
                printExpr(tree.arg, ownprec);
            } else {
                printExpr(tree.arg, ownprec);
                print(opname);
            }
            close(prec, ownprec);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitBinary(JCBinary tree) {
        try {
            int ownprec = TreeInfo.opPrec(tree.getTag());
            String opname = operatorName(tree.getTag());
            open(prec, ownprec);
            printExpr(tree.lhs, ownprec);
            print(" " + opname + " ");
            printExpr(tree.rhs, ownprec + 1);
            close(prec, ownprec);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitTypeCast(JCTypeCast tree) {
        try {
            open(prec, TreeInfo.prefixPrec);
            print("(");
            printExpr(tree.clazz);
            print(")");
            printExpr(tree.expr, TreeInfo.prefixPrec);
            close(prec, TreeInfo.prefixPrec);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitTypeTest(JCInstanceOf tree) {
        try {
            open(prec, TreeInfo.ordPrec);
            printExpr(tree.expr, TreeInfo.ordPrec);
            print(" instanceof ");
            printExpr(tree.clazz, TreeInfo.ordPrec + 1);
            close(prec, TreeInfo.ordPrec);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitIndexed(JCArrayAccess tree) {
        try {
            printExpr(tree.indexed, TreeInfo.postfixPrec);
            print("[");
            printExpr(tree.index);
            print("]");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitSelect(JCFieldAccess tree) {
        try {
            if (tree.selected instanceof JCIdent && 
        	    ((JCIdent)(tree.selected)).name.length() == 0) {
                // Hack to work around generation of types starting with "." during barrier insertion
                // TODO Fix the root cause of this (in TreeMaker, I think).
        	print(tree.name);
            } else {
                if (tree.sym instanceof ClassSymbol &&
                	isArrayClass(tree.sym.type)) {
                    printType(tree.sym.type);
                }
                else {
                    printExpr(tree.selected, TreeInfo.postfixPrec);
                    print(".");
                    print(tree.name);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public boolean printOwner = false;
    
    /**                                                                                    
     * Recursively print a type.  If the type is an array class type,                      
     * then print it out as a regular Java array.  Otherwise, print out                    
     * the regular Java type.                                                              
     */
    private void printType(Type type) throws IOException {
        // Don't print out DPJ type annotations if we are generating code                  
        boolean oldPrintDPJ = Types.printDPJ;
        Types.printDPJ = (codeGenMode == NONE);

        boolean printed = false;
        if (type instanceof ClassType) {
            ClassType ct = (ClassType) type;
            if (ct.cellType != null  && !inImport) {
                printType(ct.cellType);
                print("[]");
                printed = true;
            }
        }
        if (!printed) {
            print(type);
        }
        Types.printDPJ = oldPrintDPJ;
    }

    private boolean isArrayClass(Type type) {
        if (type instanceof ClassType) {
            ClassType ct = (ClassType) type;
            return ct.cellType != null;
        }
        return false;
    }
    
    public void visitIdent(JCIdent tree) {
        try {
            if (thisIsBogus && tree.toString().equals("this"))
        	print("__dpj_this");
            else if (printOwner && tree.toString().equals("this")) {
        	print (tree.sym.owner.type + "." + "this");
            } 
            else if (tree.sym instanceof ClassSymbol) {
                if (isArrayClass(tree.sym.type)) {
                    printType(tree.sym.type);
                }
                else print(tree.name);
            }
            else {
        	print(tree.name);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitLiteral(JCLiteral tree) {
        try {
            switch (tree.typetag) {
                case TypeTags.INT:
                    print(tree.value.toString());
                    break;
                case TypeTags.LONG:
                    print(tree.value + "L");
                    break;
                case TypeTags.FLOAT:
                    print(tree.value + "F");
                    break;
                case TypeTags.DOUBLE:
                    print(tree.value.toString());
                    break;
                case TypeTags.CHAR:
                    print("\'" +
                            Convert.quote(
                            String.valueOf((char)((Number)tree.value).intValue())) +
                            "\'");
                    break;
		case TypeTags.BOOLEAN:
		    print(((Number)tree.value).intValue() == 1 ? "true" : "false");
		    break;
		case TypeTags.BOT:
		    print("null");
		    break;
                default:
                    print("\"" + Convert.quote(tree.value.toString()) + "\"");
                    break;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitTypeIdent(JCPrimitiveTypeTree tree) {
        try {
            switch(tree.typetag) {
                case TypeTags.BYTE:
                    print("byte");
                    break;
                case TypeTags.CHAR:
                    print("char");
                    break;
                case TypeTags.SHORT:
                    print("short");
                    break;
                case TypeTags.INT:
                    print("int");
                    break;
                case TypeTags.LONG:
                    print("long");
                    break;
                case TypeTags.FLOAT:
                    print("float");
                    break;
                case TypeTags.DOUBLE:
                    print("double");
                    break;
                case TypeTags.BOOLEAN:
                    print("boolean");
                    break;
                case TypeTags.VOID:
                    print("void");
                    break;
                default:
                    print("error");
                    break;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitTypeArray(JCArrayTypeTree tree) {
        try {
            printBaseElementType(tree);
            printBrackets(tree);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // Prints the inner element type of a nested array
    private void printBaseElementType(JCArrayTypeTree tree) throws IOException {
        JCTree elem = tree.elemtype;
        while (elem instanceof JCWildcard)
            elem = ((JCWildcard) elem).inner;
        if (elem instanceof JCArrayTypeTree)
            printBaseElementType((JCArrayTypeTree) elem);
        else
            printExpr(elem);
    }

    // prints the brackets of a nested array in reverse order
    private void printBrackets(JCArrayTypeTree tree) throws IOException {
        JCTree elem;
        while (true) {
            elem = tree.elemtype;
            print("[]");
            if ((codeGenMode == NONE) && tree.rpl != null) {
        	print("<");
        	printExpr(tree.rpl);
        	print(">");
        	if (tree.indexParam != null) {
        	    print("#");
        	    printExpr(tree.indexParam);
        	}
            }
            if (!(elem instanceof JCArrayTypeTree)) break;
            tree = (JCArrayTypeTree) elem;
        }
    }

    public void visitTypeApply(JCTypeApply tree) {
	try {
            printExpr(tree.functor);
            Symbol functorSymbol = tree.functor.getSymbol();
            if (functorSymbol != null && 
        	    isArrayClass(functorSymbol.type))
        	return;
            boolean rplsToPrint = (codeGenMode == NONE) && tree.rplArgs != null &&
            	tree.rplArgs.nonEmpty();
            boolean effectsToPrint = (codeGenMode == NONE) && tree.effectArgs != null &&
            	tree.effectArgs.nonEmpty();
            if (tree.typeArgs.nonEmpty() || rplsToPrint || effectsToPrint) {
        	print("<");
        	printExprs(tree.typeArgs);
        	if (rplsToPrint) {
        	    if (tree.typeArgs.nonEmpty()) {
        		print(", ");
        	    }
        	    printExprs(tree.rplArgs);
        	}
        	if (effectsToPrint) {
        	    if (tree.typeArgs.nonEmpty() || rplsToPrint) {
        		print(", ");
        	    }
        	    // printExprs(tree.effectArgs);
        	    // TODO:  WHY DOES THIS CAUSE STACK OVERFLOW???
        	}
        	print(">");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    public void visitTypeParameter(JCTypeParameter tree) {
        try {
            print(tree.name);
            if (tree.bounds.nonEmpty()) {
                print(" extends ");
                printExprs(tree.bounds, " & ");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitRegionParameter(DPJRegionParameter tree) {
	try {
	    if (tree.isAtomic)
		print("atomic ");
	    print(tree.name);
	    if (tree.bound != null) {
		print(" under ");
		printExpr(tree.bound);
	    }
	} catch (IOException e) {
            throw new UncheckedIOException(e);
        }	
    }
    
    @Override
    public void visitWildcard(JCWildcard tree) {
        try {
            print(tree.kind);
            if (tree.kind.kind != BoundKind.UNBOUND)
                printExpr(tree.inner);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void visitTypeBoundKind(TypeBoundKind tree) {
        try {
            print(String.valueOf(tree.kind));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitErroneous(JCErroneous tree) {
        try {
            print("(ERROR)");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitLetExpr(LetExpr tree) {
        try {
            print("(let " + tree.defs + " in " + tree.expr + ")");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitModifiers(JCModifiers mods) {
        try {
            printAnnotations(mods.annotations);
            if (codeGenMode == NONE)
        	printFlags(mods.flags);
            else 
        	printFlags(mods.flags & ~Flags.ISCOMMUTATIVE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitAnnotation(JCAnnotation tree) {
        try {
            print("@");
            printExpr(tree.annotationType);
            print("(");
            printExprs(tree.args);
            print(")");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitTree(JCTree tree) {
        try {
            print("(UNKNOWN: " + tree + ")");
            println();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
