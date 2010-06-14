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

//todo: one might eliminate uninits.andSets when monotonic

package com.sun.tools.javac.comp;

import com.sun.tools.javac.code.*;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;

import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.tree.JCTree.*;

import static com.sun.tools.javac.code.Flags.*;
import static com.sun.tools.javac.code.Kinds.*;
import static com.sun.tools.javac.code.TypeTags.*;

/** This pass implements boilerplate FJTask parallelization
 * transformations for DPJ programs.
 */
public class FJTaskHarness extends TreeScanner {
    protected static final Context.Key<FJTaskHarness> harnessKey =
	new Context.Key<FJTaskHarness>();

    private final Name.Table names;
    private final Log log;
    private final Symtab syms;
    private final Types types;
    private final Check chk;
    private       TreeMaker make;
    private       Lint lint;
    
    private boolean classCanBeRun;

    public static FJTaskHarness instance(Context context) {
	FJTaskHarness instance = context.get(harnessKey);
	if (instance == null)
	    instance = new FJTaskHarness(context);
	return instance;
    }

    protected FJTaskHarness(Context context) {
	context.put(harnessKey, this);

	names = Name.Table.instance(context);
	log = Log.instance(context);
	syms = Symtab.instance(context);
        types = Types.instance(context);
	chk = Check.instance(context);
	make = TreeMaker.instance(context);
	lint = Lint.instance(context);
	classCanBeRun = false;
    }

/* ***********************************************************************
 * Visitor methods for statements and definitions
 *************************************************************************/

    /* ------------ Visitor methods for various sorts of trees -------------*/

    //Hi, my name is Ugly Hack.  How may I be of service today?
    public void visitSpawn(DPJSpawn that) {}
    public void visitFinish(DPJFinish that) {}
    public void visitCobegin(DPJCobegin that) {}
    public void visitDPJForLoop(DPJForLoop that) {}
    
    public void visitClassDef(JCClassDecl tree) {
	boolean oldClassCanBeRun = classCanBeRun;
	classCanBeRun = false;
	super.visitClassDef(tree);
    	if(classCanBeRun & (tree.mods.flags & INTERFACE)==0)
    	{
    	    //We need to create a new class to handle FJTask

    	    //make class extend FJTask if it does not already extend a class
    	    JCTree extending = make.Ident(names.fromString("RecursiveAction"));

    	    JCExpression originalMain = make.Ident(names.fromString("__dpj_run"));
    	    originalMain.setType(new Type(TypeTags.CLASS,null));
    	    JCExpressionStatement dpjrun = make.Exec(make.App(originalMain));
    	    
    	    List<JCExpression> thrown = List.<JCExpression>nil().
    	    	append(make.Ident(names.fromString("InterruptedException")));

    	    //Create a boilerplate main method to invoke the main class
    	    
    	    //add "private static String args" to class variable list
            tree.defs = tree.defs.prepend(make.VarDef(
                         make.Modifiers(PRIVATE | STATIC),
                         names.fromString("args"), null,
                         make.TypeArray(
                          make.Ident(names.fromString("String")),
                          null, null),
                         null));

    	    //add boilerplate "public static void main()" method to method list
    	    List<JCExpression> literalList = List.<JCExpression>nil();
    	    JCFieldAccess runSelect = make.Select(make.Ident(
    		    			names.fromString("DPJRuntime")),
    		    			names.fromString("RuntimeState"));
    	    /*WHY IS THE FOLLOWING LINE NECESSARY???*/
    	    runSelect.setType(new Type(TypeTags.CLASS,null));
    	    
            //Intermission: assign statement
    	    JCFieldAccess initSelect = make.Select(runSelect, names.fromString("initialize"));
    	    initSelect.setType(new Type(TypeTags.CLASS,null));
    	    JCExpressionStatement assign = make.Exec(
	            make.Assign(
	        	make.Select(make.Ident(tree.name),names.fromString("args")),
	        	make.App(initSelect,
	        		 List.<JCExpression>nil().append(
	        			 make.Ident(names.fromString("args"))))));
    	    //End intermission
    	    
    	    JCFieldAccess poolVar = make.Select(runSelect, names.fromString("pool"));
    	    
    	    runSelect = make.Select(runSelect, names.fromString("dpjNumThreads"));
    	    /*AGAIN: WHY IS THE FOLLOWING LINE NECESSARY???*/
    	    runSelect.setType(new Type(TypeTags.CLASS,null));
    	    literalList = literalList.append(runSelect);
    	    JCNewClass poolInstance = make.NewClass(null, null, null, null,
    		    make.Ident(names.fromString("ForkJoinPool")),
    		    literalList,null);
    	    
    	    JCExpressionStatement invocation = 
    		make.Exec(make.Assign(poolVar,poolInstance));
    	    List<JCVariableDecl> params = List.<JCVariableDecl>nil();
	    params = params.append(make.VarDef(
	      		make.Modifiers(0),
		        names.fromString("args"), null,
		        make.TypeArray(
		         make.Ident(names.fromString("String")),
		         null, null),
		        null));

	    thrown = List.<JCExpression>nil().append(
		    make.Ident(names.fromString("Throwable")));
    	    
	    List<JCStatement> block = List.<JCStatement>nil();
	    block = block.append(assign);
    	    block = block.append(invocation);
    	    block = block.append(dpjrun);
	    tree.defs = tree.defs.append(make.MethodDef(
    		    make.Modifiers(PUBLIC | STATIC), names.fromString("main"),
    		    make.TypeIdent(TypeTags.VOID), null,
    		    List.<JCTypeParameter>nil(), params, thrown,
    		    make.Block(0, block), null, null));
    	}

    	classCanBeRun = oldClassCanBeRun;
    }

    public void visitMethodDef(JCMethodDecl tree) {
    	//change public static void main() to public void run()
	if(tree.name.equals(names.fromString("main")))
	{
	    classCanBeRun = true;
	    tree.name = names.fromString("__dpj_run");
	    tree.params = List.<JCVariableDecl>nil();
	    /*tree.params = tree.params.append(make.VarDef(
	        		make.Modifiers(0),
			        names.fromString("args"), null,
			        make.TypeArray(
			          make.Ident(names.fromString("String")),
			          null),
			        null));
	      tree.thrown = tree.thrown.append(
	      	make.Ident(names.fromString("InterruptedException")));*/
	}
	
    	super.visitMethodDef(tree);
	}

    public void visitTopLevel(JCCompilationUnit tree) {
        //Import FJTask library
	tree.defs = tree.defs.prepend(make.Import(make.Select(make.Select(
				      make.Ident(names.fromString("jsr166y")),
				      names.fromString("forkjoin")),
				      names.asterisk),false));
    	super.visitTopLevel(tree);
    }

}