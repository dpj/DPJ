package com.sun.tools.javac.comp;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.RPLElementElement;
import javax.lang.model.util.ElementScanner6;
import javax.lang.model.util.SimpleElementVisitor6;

import com.sun.source.tree.RegionTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;
import com.sun.source.util.SimpleTreeVisitor;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.RPL;
import com.sun.tools.javac.code.RPLElement;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Symbol.RegionNameSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.parser.Lexer;
import com.sun.tools.javac.parser.Parser;
import com.sun.tools.javac.parser.Token;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.Pretty;
import com.sun.tools.javac.tree.TreeCopier;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.tree.JCTree.DPJParamInfo;
import com.sun.tools.javac.tree.JCTree.DPJRegionPathListElt;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.DPJRegionDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.Name;

/**
 * <h1>DPJ Change Log</h1>
 * 
 * <i>This is a comprehensive list of changes made to implement Deterministic
 * Parallel Java on javac.  Note that adding or modifying AST nodes requires
 * changing many other classes, such as {@link TreeCopier} and {@link Pretty},
 * which are not listed here since the changes are uninteresting.</i>
 * 
 * <h1>Spring, 2008</h1>
 * 
 * <h2>Field region declarations (4/1/2008, 4/8)</h2>
 * <ul>
 * <li> Reserved "region" and "under" as keywords:
 * {@link Token#REGION},
 * {@link Token#UNDER}
 * <li> Modified parser:
 * {@link Parser#classOrInterfaceBodyDeclaration},
 * descends to {@link Parser#regionDeclaration} and {@link Parser#singleRegionPhrase}
 * <li> New AST node:
 * {@link RegionTree},
 * {@link Tree.Kind#REGION},
 * {@link TreeVisitor#visitRegion(RegionTree, Object)},
 * {@link SimpleTreeVisitor#visitRegion(RegionTree, Object)},
 * {@link com.sun.source.util.TreeScanner#visitRegion(RegionTree, Object)},
 * {@link JCTree#REGIONDEF},
 * {@link DPJRegionDecl},
 * {@link JCTree.Visitor#visitRegionDecl},
 * {@link Pretty#visitRegionDecl(DPJRegionDecl)},
 * {@link TreeCopier#visitRegion(RegionTree, Object)},
 * {@link TreeMaker#RegionDecl(com.sun.tools.javac.tree.JCTree.JCModifiers, com.sun.tools.javac.util.Name, com.sun.tools.javac.tree.JCTree.JCIdent)},
 * {@link TreeTranslator#visitRegionDecl(DPJRegionDecl)}
 * {@link Attr#visitRegionDecl(DPJRegionDecl)}
 * <li> New symbol table entry type:
 * {@link RegionNameSymbol},
 * {@link Kinds#RPL_ELT},
 * {@link Kinds#ABSENT_REGION},
 * {@link MemberEnter#visitRegionDecl(DPJRegionDecl)},
 * {@link ElementKind#FIELD_RPL_ELEMENT},
 * {@link ElementKind#LOCAL_RPL_ELEMENT},
 * {@link RPLElementElement},
 * {@link ElementVisitor#visitRPLElement(javax.lang.model.element.RPLElementElement, Object)},
 * {@link ElementScanner6#visitRPLElement(RPLElementElement, Object)},
 * {@link SimpleElementVisitor6#visitRPLElement(RPLElementElement, Object)}
 * <ul><li>The ordering in {@link Kinds} matters; every value greater than
 * {@link Kinds#ERRONEOUS} is treated as an erroneous value.</ul>
 * <li> Region symbol lookup:
 * {@link Attr#checkId(JCTree, com.sun.tools.javac.code.Type, com.sun.tools.javac.code.Symbol, Env, int, com.sun.tools.javac.code.Type, boolean)},
 * {@link Resolve#regionNotFound},
 * {@link Resolve#findFieldRegion(Env, com.sun.tools.javac.code.Type, com.sun.tools.javac.util.Name, com.sun.tools.javac.code.Symbol.TypeSymbol)},
 * {@link Resolve#findRegion(Env, com.sun.tools.javac.util.Name)},
 * {@link Resolve#absentKindName(int)},
 * added "region" kind name string to compiler.properties/compiler_ja.properties/compiler_zh_CN.properties,
 * {@link Resolve.ResolveError#exists()}
 * <li> Set modifiers permitted on field region declarations:
 * {@link Flags#RegionFlags},
 * {@link Flags#InterfaceRegionFlags},
 * {@link Flags#LocalRegionFlags},
 * {@link Check#checkFlags(com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition, long, com.sun.tools.javac.code.Symbol, JCTree)}
 * <li> Check for no cyclic region nesting:
 * {@link Check#checkNonCyclic(com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition, com.sun.tools.javac.code.Symbol.RegionNameSymbol)},
 * added error message to compiler.properties/compiler_ja.properties/compiler_zh_CN.properties
 * </ul>
 * 
 * <h2>Region annotations on field/variable declarations (4/8)</h2>
 * <ul>
 * <li> Modified parser:
 * {@link Parser#variableDeclaratorRest},
 * {@link Parser#variableDeclaratorId};
 * both descend to {@link Parser#regionOpt}
 * <li> Modified AST node for variables:
 * {@link JCVariableDecl#rgn},
 * {@link JCTree.Factory#VarDef},
 * {@link Pretty#visitVarDef(JCVariableDecl)},
 * {@link TreeCopier#visitVariable(com.sun.source.tree.VariableTree, Object)},
 * {@link TreeMaker#VarDef(com.sun.tools.javac.tree.JCTree.JCModifiers, com.sun.tools.javac.util.Name, com.sun.tools.javac.tree.JCTree.JCIdent, com.sun.tools.javac.tree.JCTree.JCExpression, com.sun.tools.javac.tree.JCTree.JCExpression)},
 * {@link com.sun.tools.javac.tree.TreeScanner#visitVarDef(JCVariableDecl)}
 * <li> Added region field to variables in symbol table:
 * {@link VarSymbol#rgn},
 * {@link Attr#visitVarDef(com.sun.tools.javac.tree.JCTree.JCVariableDecl)}
 * <li> Added declaration of Root region - {@link Symtab#rootRegion}
 * and declared it in the predefined class (see {@link Symtab} ctor)
 * </ul>
 * 
 * <h2>Effect declarations</h2>
 * <ul>
 * <li> Reserved "reads" and "writes" as keywords:
 * {@link Token#READS},
 * {@link Token#WRITES}
 * </ul>
 * 
 * <h2>Effect checking</h2>
 * <ul>
 * <li> Replaced {@link Attr} singleton with DPJAttr:
 * {@link Attr#instance(com.sun.tools.javac.util.Context)}
 * </ul>
 * 
 * <hr></hr>
 * 
 * <h1>Fall, 2008</h1>
 * 
 * <h2>Region/effect checking (9/11-12/08)</h2>
 * <ul>
 * <li> Renamed RegionSymbol to {@link RegionNameSymbol}
 * <li> Created {@link RPL} and {@link RPLElement} and created {@link RPLElement} hierarchy
 * <li> Changed {@link DPJRegionPathListElt} to store {@link JCIdent}s instead of {@link Name}s
 *      (to facilitate idiomatic usage of {@link Attr#attribTree(JCTree, Env, int, com.sun.tools.javac.code.Type)}
 * <li> Similarly changed {@link DPJParamInfo} to use {@link JCIdent} 
 * <li> Implemented {@link Enter#visitRegionParameter(com.sun.tools.javac.tree.JCTree.DPJRegionParameter)}
 * <li> Augmented {@link Enter#visitClassDef(com.sun.tools.javac.tree.JCTree.JCClassDecl)} to enter region parameters
 * <li> Modified {@link Attr#check(JCTree, com.sun.tools.javac.code.Type, int, int, com.sun.tools.javac.code.Type)}
 *      to correctly treat REGION_PARAM as a sub-kind of REGION
 * <li> Added declaration of Local region - {@link Symtab#localElement}
 *      and declared it in the predefined class (see {@link Symtab} ctor)
 * <li> Added region parameter handling to 
 *      {@link MemberEnter#complete(com.sun.tools.javac.code.Symbol)}
 *      and {@link MemberEnter#signature(com.sun.tools.javac.util.List, com.sun.tools.javac.util.List, JCTree, com.sun.tools.javac.util.List, com.sun.tools.javac.tree.JCTree.DPJMethodEffects, Env)}
 * <li> Modified MemberEnter#baseEnv to include region parameters so that extends and implements clauses will validate
 * </ul>
 * 
 * @author Jeff Overbey
 */
public class DPJChanges { // DPJ
    private DPJChanges() {
    }
}
