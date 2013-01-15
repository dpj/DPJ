package com.sun.tools.javac.comp;

import static com.sun.tools.javac.code.Kinds.*;

import java.util.ArrayList;

import javax.lang.model.element.ElementKind;

import com.sun.tools.javac.code.Effects;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.RPL;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTags;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.OperatorSymbol;
import com.sun.tools.javac.code.Symbol.RegionParameterSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Symbol.PackageSymbol;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeCopier;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.tree.JCTree.DPJAtomic;
import com.sun.tools.javac.tree.JCTree.DPJEffect;
import com.sun.tools.javac.tree.JCTree.DPJForLoop;
import com.sun.tools.javac.tree.JCTree.DPJNonint;
import com.sun.tools.javac.tree.JCTree.DPJRegionPathList;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCArrayAccess;
import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCAssignOp;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCCatch;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCExpressionWithRPL;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCParens;
import com.sun.tools.javac.tree.JCTree.JCReturn;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTry;
import com.sun.tools.javac.tree.JCTree.JCUnary;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.JCTree.JCWhileLoop;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Options;
import com.sun.tools.javac.util.Pair;
import com.sun.tools.javac.util.Name.Table;

public class InsertBarriers extends TreeTranslator {
    private static final String DPJRUNTIME = "DPJRuntime";
    private static final String CONTEXT_DELEGATOR = "DPJContextDelegator";
    private static final String NONINT_FLAG_CLASS = "NonintFlag";
    private static final String BEFORE_READ_ACCESS = "beforeReadAccess";
    private static final String ON_READ_ACCESS = "onReadAccess";
    private static final String ON_WRITE_ACCESS = "onWriteAccess";
    private static final String BEFORE_WRITE_ACCESS_LOG_ONLY = "beforeWriteAccessLogOnly";
    private static final String RETURN_SECOND_VALUE = "returnSecondValue";
    private static final String ON_ARRAY_READ_ACCESS = "onArrayReadAccess";
    private static final String ON_ARRAY_WRITE_ACCESS = "onArrayWriteAccess";
    private static final String ON_ARRAY_WRITE_ACCESS_LOG_ONLY = "onArrayWriteAccessLogOnly";
    private static final String GET_FIELD_OFFSET = "getFieldOffset";
    
    private static final String CLONE = "Clone";
    private static final String OBJECT = "Object";
    
    private static final String CONTEXT_CLASS = "Context";
    private static final String NONINT_CONTEXT_CLASS = "NonintContext";
    private static final String ORG = "org";
    private static final String DEUCE = "deuce";
    private static final String TRANSACTION = "transaction";
    private static final String TRANSACTION_EXCEPTION = "TransactionException";
    private static final String REFLECTION = "reflection";
    private static final String ADDRESS_UTIL = "AddressUtil";
    private static final String STATIC_FIELD_BASE = "staticFieldBase";
    
    private static final String SUFFIX = "$DPJ_STM";
    private static final String TEMP_VAR_PREFIX = "$DPJ_STM_temp";
    private static final String CONTEXT_VAR = "$DPJ_STM_context";
    private static final String NONINT_FLAG_VAR = "$DPJ_STM_nonint_flag";
    private static final String FIELD_OFFSET_SUFFIX = "$DPJ_STM_offset";
    private static final String CLASS_BASE = "$DPJ_STM_class_base";
    private static final String TRANSACTIONAL_METHOD_SUFFIX = "_T$DPJ_STM";
    
    // Mox number of times to retry a transaction
    private static final int max_retries = 10;
    
    private static int nextAtomicBlockID = 1;
    
    private Name dpjruntime;
    private Name contextDelegator;
    private Name nonintFlagClass;
    private Name beforeReadAccess;
    private Name onReadAccess;
    private Name onWriteAccess;
    private Name beforeReadAccessLogOnly;
    private Name returnSecondValue;
    private Name onArrayReadAccess;
    private Name onArrayWriteAccess;
    private Name onArrayWriteAccessLogOnly;
    private Name getFieldOffset;
    private Name clone;
    private Name contextClass;
    private Name nonintContextClass;
    private Name org;
    private Name deuce;
    private Name transaction;
    private Name transactionException;
    private Name reflection;
    private Name addressUtil;
    private Name staticFieldBase;
    private Name suffix;
    private Name tempVarPrefix;
    private Name contextVar;
    private Name nonintFlagVar;
    private Name fieldOffsetSuffix;
    private Name classBase;
    private Name object;
    private Name transactionalMethodSuffix;
 
    private void initNames() {
	dpjruntime = names.fromString(DPJRUNTIME);
	contextDelegator = names.fromString(CONTEXT_DELEGATOR);
	nonintFlagClass = names.fromString(NONINT_FLAG_CLASS);
	beforeReadAccess = names.fromString(BEFORE_READ_ACCESS);
	onReadAccess = names.fromString(ON_READ_ACCESS);
	onWriteAccess = names.fromString(ON_WRITE_ACCESS);
	beforeReadAccessLogOnly = names.fromString(BEFORE_WRITE_ACCESS_LOG_ONLY);
	returnSecondValue = names.fromString(RETURN_SECOND_VALUE);
	onArrayReadAccess = names.fromString(ON_ARRAY_READ_ACCESS);
	onArrayWriteAccess = names.fromString(ON_ARRAY_WRITE_ACCESS);
	onArrayWriteAccessLogOnly = names.fromString(ON_ARRAY_WRITE_ACCESS_LOG_ONLY);
	getFieldOffset = names.fromString(GET_FIELD_OFFSET);
	clone = names.fromString(CLONE);
	contextClass = names.fromString(CONTEXT_CLASS);
	nonintContextClass = names.fromString(NONINT_CONTEXT_CLASS);
	org = names.fromString(ORG);
	deuce = names.fromString(DEUCE);
	transaction = names.fromString(TRANSACTION);
	transactionException = names.fromString(TRANSACTION_EXCEPTION);
	reflection = names.fromString(REFLECTION);
	addressUtil = names.fromString(ADDRESS_UTIL);
	staticFieldBase = names.fromString(STATIC_FIELD_BASE);
	suffix = names.fromString(SUFFIX);
	tempVarPrefix = names.fromString(TEMP_VAR_PREFIX);
	contextVar = names.fromString(CONTEXT_VAR);
	nonintFlagVar = names.fromString(NONINT_FLAG_VAR);
	fieldOffsetSuffix = names.fromString(FIELD_OFFSET_SUFFIX);
	classBase = names.fromString(CLASS_BASE);
	object = names.fromString(OBJECT);
	transactionalMethodSuffix = names.fromString(TRANSACTIONAL_METHOD_SUFFIX);
    }
    
    // Types of expressions that can be assigned into
    private enum AssigneeType { 
	FIELD_ID, 	// An identifier naming a field (in the current class)
	FIELD_ACCESS, 	// A field access expression ('e1.f')
	ARRAY_ACCESS, 	// An array access expression ('e1[e2]')
	DONT_TRANSLATE 	// Local variables, or anything else not for which we
			// don't need to translate the assignment into a barrier.
    }
    
    // Translation mode currently in effect
    private enum Mode {
	NORMAL,		// No barriers or logging.  Translate atomic blocks 
			// and switch to transactional mode inside them.
			// Switch to noninterfering mode in nonint blocks.
	
	NONINTERFERING,	// No barriers or logging.  Ignore nonint and
			// atomic blocks.
	
	TRANSACTIONAL,	// Insert barriers.  Ignore atomic blocks.  Switch 
			// to logging-only mode in nonint blocks.
	
	LOGGING_ONLY	// Insert logging for field writes, but no barriers.
			// Ignore nonint and atomic blocks.
	
			// Each mode also translates function calls to the
			// corresponding versions of functions.
    }
    private Mode mode = Mode.NORMAL;
    
    // Are we generating the code for the body of an atomic block 
    // (even in a nested nonint block)?
    private boolean inAtomic = false;
    
    protected static final Context.Key<InsertBarriers> insertBarriersKey =
	new Context.Key<InsertBarriers>();

    public static InsertBarriers instance(Context context) {
	InsertBarriers instance = context.get(insertBarriersKey);
	if (instance == null)
	    instance = new InsertBarriers(context);
	return instance;
    }

    private TreeMaker make;
    private TreeCopier copy;
    private Log log;
    private Name.Table names;
    private Resolve rs;
    private Types types;
    private Symtab syms;
    private Options options;
    private CheckEffects checkEffects;
    
    // Environment for symbol lookup
    Env<AttrContext> attrEnv;
    
    // Enclosing method (we insert new variable decls at the top of this)
    private JCMethodDecl enclMethod = null;
    private JCClassDecl enclClass = null;
    
    // Have we already inserted a class_base field for in this class
    // (used for transactional access to static fields)
    private boolean madeClassBase = false;
    
    // Index for unique naming of new variables within methods
    private int nextVarIndex = 1;
    
    // New statements (variable decls) to insert at top of current function
    private List<JCStatement> newStats;
    
    // Should we translate the current method?
    private boolean translateMethod;
    
    // List of multiple results that should be substituted for the original one element.
    // If result == null, get results here instead (used to duplicate methods).
    List<JCTree> multipleResults = null;
    
    boolean translateWrites = true;
    
    protected InsertBarriers(Context context) {
	context.put(insertBarriersKey, this);
	make = TreeMaker.instance(context);
	copy = new TreeCopier(make);
	log = Log.instance(context);
	names = Name.Table.instance(context);
	rs = Resolve.instance(context);
	types = Types.instance(context);
	syms = Symtab.instance(context);
	options = Options.instance(context);
	checkEffects = CheckEffects.instance(context);
	initNames();
	//TODO: Set attrEnv (or remove it)
    }
    
    @Override
    public <T extends JCTree> T translate(T tree) {
	T newTree = super.translate(tree);
	// Make translation preserve types (unless they're explicitly changed)
	if (newTree != null && newTree.type == null)
	    newTree.type = tree.type;
	return newTree;
    }
    
    // Assignment operations
    
    private Pair<AssigneeType, JCExpression> translateAssignee(JCExpression tree) {
	if (tree instanceof JCIdent) {
	    JCIdent ident = (JCIdent) tree;
	    if (isTranslatableFieldSymbol(ident.sym) && translateWrites) {
		return Pair.of(AssigneeType.FIELD_ID, tree);    
	    } else {
		return Pair.of(AssigneeType.DONT_TRANSLATE, tree); 
	    }
	}  
	
	if (tree instanceof JCFieldAccess) {
	    JCFieldAccess fieldAccess = (JCFieldAccess) tree;
	    fieldAccess.selected = translate(fieldAccess.selected); 
	    
	    if (isTranslatableFieldAccess(fieldAccess) && translateWrites) {
		return Pair.of(AssigneeType.FIELD_ACCESS, tree);
	    } else {
		return Pair.of(AssigneeType.DONT_TRANSLATE, tree);		
	    }
		
	}
	
	if (tree instanceof JCArrayAccess) {
	    JCArrayAccess arrayAccess = (JCArrayAccess) tree;
	    arrayAccess.indexed = translate(arrayAccess.indexed);
	    arrayAccess.index = translate(arrayAccess.index);
	    
	    if (translateWrites) {
		return Pair.of(AssigneeType.ARRAY_ACCESS, tree);
	    } else {
		return Pair.of(AssigneeType.DONT_TRANSLATE, tree);		
	    }	}

	if (tree instanceof JCParens) {
	    JCParens parens = (JCParens) tree;
	    return translateAssignee(parens.expr);
	}
	
	log.error(tree.pos, "unexpected.form.of.assignment");
	return Pair.of(AssigneeType.DONT_TRANSLATE, tree);
    }
    
    @Override
    public void visitAssign(JCAssign tree) {
	switch (mode) {
	case NORMAL:
	case NONINTERFERING:
	    super.visitAssign(tree);
	    break;
	case TRANSACTIONAL:
	case LOGGING_ONLY:
	    visitAssign_transactional(tree);
	    break;
	}
    }
    
    private void visitAssign_transactional(JCAssign tree) {
	Pair<AssigneeType, JCExpression> translated = translateAssignee(tree.lhs);
	
	switch (translated.fst) {
	case FIELD_ID:
	    JCIdent ident = (JCIdent)translated.snd;
	    JCExpression selected;
	    if ((ident.sym.flags() & Flags.STATIC) != 0) {
		selected = make.Ident(ident.sym.owner);
		selected.type = ident.sym.owner.type;
	    } else {
		selected = make.Ident(names._this);
		selected.type = enclClass.type;
	    }
	    result = makeWriteAccessTree(ident,
		    null, selected, 
		    makeCastIfNeeded(ident.type, translate(tree.rhs)),
		    ident.sym);
	    break;
	    
	case FIELD_ACCESS:
	    JCFieldAccess fieldAccess = (JCFieldAccess)translated.snd;
	    Name temp1 = insertLocalVariable(types.erasure(fieldAccess.selected.type));
	    result = makeWriteAccessTree(fieldAccess,
		    temp1, fieldAccess.selected, 
		    makeCastIfNeeded(fieldAccess.type, translate(tree.rhs)),
		    fieldAccess.sym);
	    break;
	    
	case ARRAY_ACCESS:
	    JCArrayAccess arrayAccess = (JCArrayAccess)translated.snd;
	    result = makeArrayWriteAccessTree(arrayAccess,
		    null, arrayAccess.indexed,
		    null, arrayAccess.index,
		    translate(tree.rhs));
	    break;
	
	case DONT_TRANSLATE:
	    tree.lhs = translated.snd;
	    tree.rhs = translate(tree.rhs);
	    result = tree;
	    break;
	}
    }

    @Override
    public void visitAssignop(JCAssignOp tree) {
	switch (mode) {
	case NORMAL:
	case NONINTERFERING:
	    super.visitAssignop(tree);
	    break;
	case TRANSACTIONAL:
	case LOGGING_ONLY:
	    visitAssignop_transactional(tree);
	    break;
	}
    }
    
    private void visitAssignop_transactional(JCAssignOp tree) {
	Pair<AssigneeType, JCExpression> translated = translateAssignee(tree.lhs);
	int op = tree.getTag() - JCTree.ASGOffset;
	
	switch (translated.fst) {
	case FIELD_ID:
	    JCIdent ident = (JCIdent)translated.snd;
	    JCExpression selected;
	    if ((ident.sym.flags() & Flags.STATIC) != 0) {
		selected = make.Ident(ident.sym.owner);
		selected.type = ident.sym.owner.type;
	    } else {
		selected = make.Ident(names._this);
		selected.type = enclClass.type;
	    }
	    
	    JCIdent identExp = (JCIdent)copy.copy(ident);
	    identExp.rpl = ident.rpl;
	    
	    JCExpression binopExp = translate(
		    make.Binary(op, identExp, make.Parens(tree.rhs)));
	    
	    result = makeWriteAccessTree(ident,
		    null, selected, 
		    binopExp, 
		    ident.sym);
	    break;
	    
	case FIELD_ACCESS:
	    JCFieldAccess fieldAccess = (JCFieldAccess)translated.snd;
	    Name temp1 = insertLocalVariable(types.erasure(fieldAccess.selected.type));
	    
	    JCExpression temp1id = make.Ident(temp1);
	    temp1id.type = types.erasure(fieldAccess.selected.type);
	    JCFieldAccess selectExp = (JCFieldAccess)make.Select(temp1id, fieldAccess.sym);
	    selectExp.type = fieldAccess.sym.type;
	    selectExp.rpl = fieldAccess.rpl;
	    
	    result = makeWriteAccessTree(fieldAccess,
		    temp1, fieldAccess.selected,
		    translate(make.Binary(op, selectExp, make.Parens(tree.rhs))),
		    fieldAccess.sym);
	    break;
	    
	case ARRAY_ACCESS:
	    JCArrayAccess arrayAccess = (JCArrayAccess)translated.snd;
	    Name tempArr = insertLocalVariable(arrayAccess.indexed.type);
	    Name tempIdx = insertLocalVariable(arrayAccess.index.type);
	    
	    JCExpression tempArrId = make.Ident(tempArr);
	    tempArrId.type = arrayAccess.indexed.type;
	    JCExpression tempIdxId = make.Ident(tempIdx);
	    tempIdxId.type = arrayAccess.index.type;
	    JCArrayAccess readExp = make.Indexed(tempArrId, tempIdxId);
	    readExp.type = arrayAccess.type;
	    readExp.rpl = arrayAccess.rpl;
	    
	    //onWriteAccess(tempArr = e1, tempIdx = e2, translate(tempArr[tempIdx] BINOP value), context)
	    result = makeArrayWriteAccessTree(arrayAccess,
		    tempArr, arrayAccess.indexed,
		    tempIdx, arrayAccess.index,
		    translate(make.Binary(op, readExp, make.Parens(tree.rhs))));
	    break;
	
	case DONT_TRANSLATE:
	    tree.lhs = translated.snd;
	    tree.rhs = translate(tree.rhs);
	    result = tree;
	    break;
	}
    }
    
    // Transform "++e" into "e += 1", and translate that    
    private void visitPreIncDec(JCUnary tree, boolean inc) {
	int op = inc ? JCTree.PLUS_ASG : JCTree.MINUS_ASG;

	JCExpression literalOne = make.Literal(TypeTags.INT, 1);
	literalOne.type = new Type(TypeTags.INT, null);
	JCExpression assignop = make.Assignop(op, tree.arg, literalOne);
	JCExpression parens = make.Parens(assignop);
	assignop.type = parens.type = tree.arg.type;
	
	result = translate(parens);
    }
    
    private void visitPostIncDec(JCUnary tree, boolean inc) {
	Pair<AssigneeType, JCExpression> translated = translateAssignee(tree.arg);
	int op = inc ? JCTree.PLUS : JCTree.MINUS;
	Name temp1, temp2;
	
	switch (translated.fst) {
	case FIELD_ID:
	    JCIdent ident = (JCIdent)translated.snd;
	    temp1 = insertLocalVariable(tree.arg.type);
	    JCExpression selected;
	    if ((ident.sym.flags() & Flags.STATIC) != 0) {
		selected = make.Ident(ident.sym.owner);
		selected.type = ident.sym.owner.type;
	    } else {
		selected = make.Ident(names._this);
		selected.type = enclClass.type;
	    }
	    result = makeWriteAccessTree(ident,
		    null, selected, 
		    temp1, translate(ident),
		    make.Binary(op, make.Ident(temp1), make.Literal(TypeTags.INT, 1)), 
		    ident.sym);
	    break;
	    
	case FIELD_ACCESS:
	    JCFieldAccess fieldAccess = (JCFieldAccess)translated.snd;
	    temp1 = insertLocalVariable(types.erasure(fieldAccess.selected.type));
	    temp2 = insertLocalVariable(tree.arg.type);
	    
	    JCExpression temp1id = make.Ident(temp1);
	    temp1id.type = types.erasure(fieldAccess.selected.type);
	    JCFieldAccess val = (JCFieldAccess)make.Select(temp1id, fieldAccess.sym);
	    val.type = fieldAccess.sym.type;
	    val.rpl = fieldAccess.rpl;
	    
	    result = makeWriteAccessTree(fieldAccess,
		    temp1, fieldAccess.selected, 
		    temp2, translate(val),
		    make.Binary(op, make.Ident(temp2), make.Literal(TypeTags.INT, 1)), 
		    fieldAccess.sym);
	    break;
	    
	case ARRAY_ACCESS:
	    JCArrayAccess arrayAccess = (JCArrayAccess)translated.snd;
	    Name tempArr = insertLocalVariable(arrayAccess.indexed.type);
	    Name tempIdx = insertLocalVariable(arrayAccess.index.type);
	    Name tempVal = insertLocalVariable(arrayAccess.type);
	    
	    JCExpression tempArrId = make.Ident(tempArr);
	    tempArrId.type = arrayAccess.indexed.type;
	    JCExpression tempIdxId = make.Ident(tempIdx);
	    tempIdxId.type = arrayAccess.index.type;
	    JCExpression readExp = make.Indexed(tempArrId, tempIdxId);
	    readExp.type = arrayAccess.type;
	    JCExpression tempValId = make.Ident(tempVal);
	    tempValId.type = readExp.type;
	    
	    //onWriteAccess(tempArr = e1, tempIdx = e2, tempVal = tempArr[tempIdx], tempVal BINOP 1, context)
	    result = makeArrayWriteAccessTree(arrayAccess,
		    tempArr, arrayAccess.indexed,
		    tempIdx, arrayAccess.index,
		    tempVal, readExp,
		    make.Binary(op, tempValId, make.Literal(TypeTags.INT, 1)));
	    break;
	
	case DONT_TRANSLATE:
	    tree.arg = translated.snd;
	    result = tree;
	    break;
	}
    }

    @Override
    public void visitUnary(JCUnary tree) {
	switch (mode) {
	case NORMAL:
	case NONINTERFERING:
	    super.visitUnary(tree);
	    break;
	case TRANSACTIONAL:
	case LOGGING_ONLY:
	    visitUnary_transactional(tree);
	    break;
	}
    }
    
    private void visitUnary_transactional(JCUnary tree) {
	int opcode = tree.getTag();
	
	switch (opcode) {
	case JCTree.PREINC: case JCTree.PREDEC:
	    visitPreIncDec(tree, opcode == JCTree.PREINC);
	    break;
	    
	case JCTree.POSTINC: case JCTree.POSTDEC:
	    visitPostIncDec(tree, opcode == JCTree.POSTINC);
	    break;
	    
	default:
	    tree.arg = translate(tree.arg);
	    result = tree;
	    break;	
	}
    }
    
    // Potential field reference operations: may be used for reads or writes
    // Basic rule: If a field reference is _directly_ used as the left side 
    //    of an assignment, or if it's on the left side of an assignment 
    //    nested only in parentheses, then it's a write (& maybe also read) of 
    //    that field.  If it's further nested in the AST for the left side of 
    //    an assignment, or if it's anywhere else, then it's a read.
    //
    // The write cases should be handled by the assignment operation visitors,
    // so the below visitors should only get called for the read cases.
    // (In the NORMAL and NONINTERFERING modes, these methods do get called
    // for the write cases, but that's OK because the translation is a no-op.)
    
    @Override
    public void visitIndexed(JCArrayAccess tree) {
	switch (mode) {
	case NORMAL:
	case NONINTERFERING:
	    super.visitIndexed(tree);
	    break;
	case LOGGING_ONLY:
	case TRANSACTIONAL:
	    visitIndexed_transactional(tree);
	    break;
	}
    }
    
    private void visitIndexed_transactional(JCArrayAccess tree) {
	if (isLogOnlyAccess(tree)) {
	    super.visitIndexed(tree);
	    return;
	}
	
	// An optimization would be not to track local arrays whose references don't escape.
	result = makeArrayReadAccessTree(translate(tree.indexed), translate(tree.index));
    }

    @Override
    public void visitSelect(JCFieldAccess tree) {
	switch (mode) {
	case NORMAL:
	case NONINTERFERING:
	    super.visitSelect(tree);
	    break;
	case LOGGING_ONLY:
	case TRANSACTIONAL:
	    visitSelect_transactional(tree);
	    break;
	}
    }
    
    private void visitSelect_transactional(JCFieldAccess tree) { 
	if (!isTranslatableFieldAccess(tree) || isLogOnlyAccess(tree)) {
	    super.visitSelect(tree);
	    return;
	}
	    
	if (enclMethod == null) {
	    super.visitSelect(tree);
	    return;
	}
	    
	// Generate simpler tree without temp var for fields of 'this'
	if (tree.selected instanceof JCIdent && 
		((JCIdent)tree.selected).name.equals(names._this)) {
	    result = makeThisFieldReadTree(tree, tree.sym);
	    return;
	}
	   
	// This is definitely a read of a field
	
	if ((tree.sym.flags() & Flags.STATIC) != 0) {
	    result = makeReadAccessTree(
		    null,
		    make.Select(make.Type(types.erasure(tree.selected.type)), classBase),
		    make.Select(translate(tree.selected), tree.sym),
		    makeOffsetofTree(tree.selected.type, tree.sym));
	} else {
	    Name tempVar = insertLocalVariable(tree.selected.type);

	    result = makeReadAccessTree(
		    tempVar, 
		    translate(tree.selected),
		    make.Select(make.Ident(tempVar), tree.sym),
		    makeOffsetofTree(tree.selected.type, tree.sym));	    
	}
    }

    @Override
    public void visitIdent(JCIdent tree) {
	switch (mode) {
	case NORMAL:
	case NONINTERFERING:
	    super.visitIdent(tree);
	    break;
	case LOGGING_ONLY:
	case TRANSACTIONAL:
	    visitIdent_transactional(tree);
	    break;
	}
    }
    
    private void visitIdent_transactional(JCIdent tree) {
	// Figure out if this is a field access (of this) or not.
	if (isTranslatableFieldSymbol(tree.sym) && !isLogOnlyAccess(tree)) {
	    result = makeThisFieldReadTree(tree, tree.sym);
	} else {
	    result = tree;
	}
    }
    
    // Convert [this.]f to "DPJContextDelegator.onReadAccess(this, f, offsetof(f), context);"
    private JCTree makeThisFieldReadTree(JCExpression tree, Symbol sym) {
	JCExpression obj;
	
	if ((sym.flags() & Flags.STATIC) != 0) {
	    obj = make.Select(make.Ident(sym.owner), classBase);
	} else {
	    obj = make.Ident(names._this);
	}
	
	return makeReadAccessTree(
	    null,
	    obj,
	    tree, 
	    makeOffsetofTree(enclClass.type, sym));
    }
    
    /** Generate the JCTree for an onReadAccess(...) call (and preceding 
     *  beforeReadAccess() call), given trees for the arguments.
     *  obj should already be translated (if necessary).
     */
    private JCTree makeReadAccessTree(Name tempVarName, JCExpression obj, JCExpression val, JCExpression field) {
	JCExpression beforeReadExpr = makeBeforeReadAccessTree(tempVarName, obj, field);
	
	JCExpression meth = makeContextDelegatorMethod(ON_READ_ACCESS);

	// Build list of arguments to onReadAccess
	List<JCExpression> args = List.nil();
	args = args.prepend(makeContextTree());
	args = args.prepend(field);
	args = args.prepend(val);
	args = args.prepend(beforeReadExpr);
	
	return make.App(meth, args);
    }
    
    /** Generate the JCExpression for "tempVar = beforeReadAccess(obj, field, context)".
     *  obj should already be translated (if necessary).
     */
    private JCExpression makeBeforeReadAccessTree(Name tempVarName, JCExpression obj, JCExpression field) {
	JCExpression meth = makeContextDelegatorMethod(BEFORE_READ_ACCESS);
	
	List<JCExpression> args = List.nil();
	args = args.prepend(makeContextTree());
	args = args.prepend(field);
	args = args.prepend(obj);
	
	return makeAssignOpt(tempVarName, make.App(meth, args));
    }
    
    /** Make an expression "varName = expr" (or just "expr" if varName is null)
     */
    private JCExpression makeAssignOpt(Name varName, JCExpression expr) {
	if (varName != null) {
	    return make.Assign(make.Ident(varName), expr);
	} else {
	    return expr;
	}	
    }
    
    /** Make a context delegator call with a default type (which is bogus, but
     *  sufficient to make the pretty-printer generate appropriate code).
     */
    // TODO Make this assign the right type (or change invocations of this to invocations with the correct type specified.)
    private JCExpression makeContextDelegatorMethod(String name) {
	return makeContextDelegatorMethod(name, new Type(TypeTags.METHOD, null));
    }
    
    /** Make an expression for a context delegator method call, and give it the specified type
     */
    private JCExpression makeContextDelegatorMethod(String name, Type type) {
	JCExpression meth = make.Select(
		make.Select(make.Select(make.Select(make.Ident(
			org), deuce), transaction), contextDelegator),
		names.fromString(name));
	meth.setType(type);
	return meth;
    }

    /** Generate 4-argument form (no old value) of onWriteAccess call
     *  Expressions passed in should already be translated (if necessary).
     */
    private JCExpression makeWriteAccessTree(JCExpressionWithRPL tree,
	    				     Name varName1, JCExpression obj, 
	    				     JCExpression value, Symbol field) {
	return makeWriteAccessTree(tree, varName1, obj, null, null, value, field);
    }
    
    /** Generate 4- or 5-argument form (with old value) of onWriteAccess call
     */
    private JCExpression makeWriteAccessTree(JCExpressionWithRPL tree,
	    				     Name varName1, JCExpression obj, 
	    				     Name varName2, JCExpression oldValue,
	    				     JCExpression value, Symbol field) {
	if (isLogOnlyAccess(tree)) {
	    return makeWriteAccessTreeLogOnly(varName1, obj, varName2, oldValue, value, field);
	}
	
	JCExpression meth = makeContextDelegatorMethod(ON_WRITE_ACCESS);
	
	if (varName1 == null && !(obj instanceof JCIdent)) {
	    log.error("No object symbol for onWriteAccess call");
	}
	
	Name objName = (varName1 != null) ?
				varName1 :
				((JCIdent)obj).name;

	List<JCExpression> args = List.nil();
	if ((field.flags() & Flags.STATIC) != 0) {
	    // If this is a write to a static field, we put the argument for the classBase
	    // object at the end.  This results in calling a version of the onWriteAccess
	    // methods where the first argument is a dummy.  Note that we still need to
	    // evaluate the 'obj' expression because it may have other effects, so
	    // we can't just replace the first argument with this one.
	    // But if the 'obj' expression is just a type specifier, we have to change it.
	    JCExpression type = make.Type(types.erasure(field.owner.type));
	    args = args.prepend(		    
		    make.Select(type, classBase));
	    
	    if ((obj instanceof JCIdent && ((JCIdent)obj).sym instanceof TypeSymbol) ||
		    (obj instanceof JCFieldAccess && ((JCFieldAccess)obj).sym instanceof TypeSymbol)) {
		obj = make.Literal(TypeTags.BOT, null);
	    }
	}
	args = args.prepend(makeContextTree());
	args = args.prepend(makeOffsetofTree(make.Ident(objName), field));
	args = args.prepend(value);
	if (oldValue != null)
	    args = args.prepend(makeAssignOpt(varName2, oldValue));
	args = args.prepend(makeAssignOpt(varName1, obj));
	
	return make.App(meth, args);
    }
    
    private JCExpression makeWriteAccessTreeLogOnly(Name varName1, JCExpression obj,
	    					    Name varName2, JCExpression oldValue,
	    					    JCExpression newValue, Symbol field) {
	JCExpression meth = makeContextDelegatorMethod(BEFORE_WRITE_ACCESS_LOG_ONLY);
	JCExpression returnSecondValue = makeContextDelegatorMethod(RETURN_SECOND_VALUE);
	List<JCExpression> args = List.nil();
	
	if (varName1 == null && !(obj instanceof JCIdent)) {
	    log.error("No object symbol for onWriteAccess call");
	}
	
	Name objName = (varName1 != null) ?
		varName1 :
		((JCIdent)obj).name;
	
	// Case 1: Corresponds to onWriteAccess(obj, value, field, context).  For non-static fields
	
	// Non - post-inc/dec case
	if (oldValue == null) {
	    // Non-static field: case 1
	    // "(beforeWriteAccessLogOnly(v1 = obj, v1.f, fieldOffset, context).f = value)"
	    if((field.flags() & Flags.STATIC) == 0) {
		if (varName1 == null)
		    varName1 = insertLocalVariable(obj.type);

		args = args.append(makeAssignOpt(varName1, obj));
		args = args.append(make.Select(make.Ident(varName1), field));
		args = args.append(makeOffsetofTree(make.Ident(objName), field));
		args = args.append(makeContextTree());
		
		JCMethodInvocation callExp = make.App(meth, args);
		return make.Parens(make.Assign(make.Select(callExp, field), newValue));
	    }
	    // Static field: case 2
	    // "Class.static_f = beforeWriteAccessLogOnly(obj, Class.static_f, newValue, fieldOffset, context, classBase)"
	    else {
		// The class that contains the field
		JCExpression type = make.Type(types.erasure(field.owner.type));
		
		if ((obj instanceof JCIdent && ((JCIdent)obj).sym instanceof TypeSymbol) ||
			(obj instanceof JCFieldAccess && ((JCFieldAccess)obj).sym instanceof TypeSymbol)) {
		    obj = make.Literal(TypeTags.BOT, null);
		}
		
		args = args.append(makeAssignOpt(varName1, obj));
		args = args.append(make.Select(type, field));
		args = args.append(newValue);
		args = args.append(makeOffsetofTree(make.Ident(objName), field));
		args = args.append(makeContextTree());
		args = args.append(make.Select(type, classBase));
		
		JCMethodInvocation callExp = make.App(meth, args);
		return make.Parens(make.Assign(make.Select(type, field), callExp));
	    }
	}
	
	// Post-inc/dec case
	else {
	    int op = ((JCBinary)newValue).getTag(); //JCTree.PLUS for post-int, JCTree.MINUS for post-dec
	    
	    // Non-static field: case 3
	    // "returnSecondValue(beforeWriteAccessLogOnly(obj, v2=oldValue, fieldOffset, context).f = v2 +/- 1, v2)"
	    if((field.flags() & Flags.STATIC) == 0) {
		args = args.append(makeAssignOpt(varName1, obj));
		args = args.append(makeAssignOpt(varName2, oldValue));
		args = args.append(makeOffsetofTree(make.Ident(objName), field));
		args = args.append(makeContextTree());
		JCMethodInvocation callExp = make.App(meth, args);
		
		JCExpression assignExp = make.Assign(
			make.Select(callExp, field),
			make.Binary(op, make.Ident(varName2), make.Literal(TypeTags.INT, 1)));
		
		List<JCExpression> args2 = List.nil();
		args2 = args2.append(assignExp);
		args2 = args2.append(make.Ident(varName2));
		return make.App(returnSecondValue, args2);
		
	    }
	    // Static field: case 4
	    // "returnSecondValue(Class.static_f = beforeWriteAccessLogOnly(obj, v2=oldValue, v2 +/- 1, fieldOffset, context, classBase), v1)"
	    else {
		// The class that contains the field
		JCExpression type = make.Type(types.erasure(field.owner.type));

		if ((obj instanceof JCIdent && ((JCIdent)obj).sym instanceof TypeSymbol) ||
			(obj instanceof JCFieldAccess && ((JCFieldAccess)obj).sym instanceof TypeSymbol)) {
		    obj = make.Literal(TypeTags.BOT, null);
		}
		
		args = args.append(makeAssignOpt(varName1, obj));
		args = args.append(makeAssignOpt(varName2, oldValue));
		args = args.append(make.Binary(op, make.Ident(varName2), make.Literal(TypeTags.INT, 1)));
		args = args.append(makeOffsetofTree(make.Ident(objName), field));
		args = args.append(makeContextTree());
		args = args.append(make.Select(type, classBase));
		JCMethodInvocation callExp = make.App(meth, args);
		
		JCExpression assignExp = make.Assign(make.Select(type, field), callExp);
		
		List<JCExpression> args2 = List.nil();
		args2 = args2.append(assignExp);
		args2 = args2.append(make.Ident(varName2));
		return make.App(returnSecondValue, args2);
	    }
	}
	
    }
    
    /** Generate 4-argument form (no one value) of onArrayWriteAccess call
     *  Expressions passed should already be translated (if necessary).
     */
    private JCExpression makeArrayWriteAccessTree(JCExpressionWithRPL tree,
	    					  Name arrayVarName, JCExpression arr,
	    					  Name indexVarName, JCExpression index,
	    					  JCExpression value) {
	return makeArrayWriteAccessTree(tree, arrayVarName, arr, indexVarName, index, null, null, value);
    }
 
    private JCExpression makeArrayWriteAccessTree(JCExpressionWithRPL tree,
	    					  Name arrayVarName, JCExpression arr,
		  				  Name indexVarName, JCExpression index,
		  				  Name oldvalVarName, JCExpression oldValue,
		  				  JCExpression value) {
	JCExpression meth;
	if (isLogOnlyAccess(tree)) {
	    meth = makeContextDelegatorMethod(ON_ARRAY_WRITE_ACCESS_LOG_ONLY);
	} else {
	    meth = makeContextDelegatorMethod(ON_ARRAY_WRITE_ACCESS);	
	}
	    
	List<JCExpression> args = List.nil();
	args = args.prepend(makeContextTree());
	args = args.prepend(value);
	if (oldValue != null)
	    args = args.prepend(makeAssignOpt(oldvalVarName, oldValue));
	args = args.prepend(makeAssignOpt(indexVarName, index));
	args = args.prepend(makeAssignOpt(arrayVarName, arr));
	
	return make.App(meth, args);
    }
    
    // Should be called with a tree representing a read or write operation.
    // Returns true if this is an access that should be done in log-only mode (i.e. an access to a nonint location)
    private boolean isLogOnlyAccess(JCExpressionWithRPL tree) {
	if (options.get("-disablenonintopt") != null)
	    return false;
	
	// TODO Remove nonint block support?
	if (mode == Mode.LOGGING_ONLY)
	    return true;
	
	// TODO Does this work right for accesses in constructors?
	if (tree.rpl.isAtomic()) {
	    return false;
	} else {
	    return true;
	}
    }
    
    private JCTree makeArrayReadAccessTree(JCExpression indexed, JCExpression index) {
	JCExpression meth = makeContextDelegatorMethod(ON_ARRAY_READ_ACCESS);
	
	List<JCExpression> args = List.nil();
	args = args.prepend(makeContextTree());
	args = args.prepend(index);
	args = args.prepend(indexed);
	
	return make.App(meth, args);
    }
    
    /** Generate the tree for an expression to access the current context
     */
    private JCExpression makeContextTree() {
	return make.Ident(contextVar);
    }
    
    /** Generate an expression to give the offset of a field (which can
     *  be used to access it with sum.misc.Unsafe methods)
     *  @param e	expression for the class name, or an object of that type
     */
    private JCExpression makeOffsetofTree(JCExpression e, Symbol field) {
	return make.Select(e, field.name.append(fieldOffsetSuffix));
    }
    
    private JCExpression makeOffsetofTree(Type t, Symbol field) {
	return makeOffsetofTree(make.Type(types.erasure(t)), field);
    }
    
    /** We need to cast some 'value' arguments to the type of the field being accessed in
     *  order to ensure that the right polymorphic method is called.  This isn't needed
     *  for object types, since the context delegator methods for them don't use polymorphism.
     */
    private JCExpression makeCastIfNeeded(Type type, JCExpression exp) {
	JCExpression retVal;
	if (!type.isPrimitive() || types.isSameType(type, exp.type))
	    retVal = exp;
	else {
	    retVal = make.TypeCast(type, make.Parens(exp));
	    retVal.type = type;
	}
	return retVal;
    }
    
    /** Insert a new local variable of a given type in the current method, and return its name
     */
    private Name insertLocalVariable(Type type) {
	if (enclMethod == null)
	    log.error("Can't insert variable outside of method");
	
	Name name = names.fromString(TEMP_VAR_PREFIX + nextVarIndex++);
	JCStatement declaration = make.VarDef(new VarSymbol(0, name, type, enclMethod.sym), null);
	newStats = newStats.prepend(declaration);
	
	return name;
    }
    
    
    /** Determine whether sym is a symbol referring to a field
     *  for which we need to do the translation of reads/writes
     */
    private boolean isTranslatableFieldSymbol(Symbol sym) {
	// TODO Handle accesses in initializers (outside methods)
	if (enclMethod == null)
	    return false;
	
	if (sym == null) {
	    return false;
	}
	
	// Don't translate final field accesses
	// TODO Is this always legitimate?
	if ((sym.flags() & Flags.FINAL) != 0)
	    return false;
	
	return (sym instanceof VarSymbol && 
		((VarSymbol)sym).getKind() == ElementKind.FIELD &&
		((VarSymbol)sym).name != names._this &&
		((VarSymbol)sym).name != names._super &&
		((VarSymbol)sym).name != names._class);
    }
    
    /** Determine whether a 'select' expression needs to be translated
     *  (i.e. it's actually a field access, not a method call or special pseudo-field access)
     */
    private boolean isTranslatableFieldAccess(JCFieldAccess tree) {
	return isTranslatableFieldSymbol(tree.sym);
    }
    
    // Change a method invocation to the appropriate version
    // for our current mode.  This requires inserting a context
    // parameter for the transactional and logging_only versions.
    // TODO Figure out how we want to distinguish the versions,
    //      and refactor accordingly.
    @Override
    public void visitApply(JCMethodInvocation tree) {
	switch (mode) {
	case NORMAL:
	    super.visitApply(tree);
	    break;
	case NONINTERFERING:
	case TRANSACTIONAL:
	case LOGGING_ONLY:
	    visitApply_addParameter(tree);
	    break;
	}
    }
    
    public void visitNewClass(JCNewClass tree) {
	switch (mode) {
	case NORMAL:
	    super.visitNewClass(tree);
	    break;
	case NONINTERFERING:
	case TRANSACTIONAL:
	case LOGGING_ONLY:
	    visitNewClass_addParameter(tree);
	    break;
	}
    }
    
    /** Change method calls to call logging-only version & pass context
     */
    private void visitApply_addParameter(JCMethodInvocation tree) {
	tree.meth = translate(tree.meth);
	JCExpression contextExpr;
	
	// Don't insert extra param in super() call in constructors for classes that directly
	// descend from Object (since Object doesn't have a constructor of that form).
	if (tree.meth instanceof JCIdent) {
	    JCIdent ident = (JCIdent)tree.meth;
	    if (ident.name.equals(names._super) && enclClass.extending == null) {
		result = tree;
		return;
	    }
	}
	
	if (mode == Mode.TRANSACTIONAL) {
	    contextExpr = make.Ident(contextVar);
	} else if (mode == Mode.LOGGING_ONLY) {
	    // Make the context parameter a NonintContext, so that we get the
	    // logging-only version of the constructor, if available.
	    contextExpr = make.TypeCast(
		    make.Select(make.Select(make.Select(make.Ident(
			    org), deuce), transaction), nonintContextClass),
		    make.Ident(contextVar));   
	} else if (mode == Mode.NONINTERFERING) {
	    // Don't add NonintFlag parameter for calls to java.* and javax.* types.
	    // This is conservative but safe with respect to subclassing of
	    // standard library classes.
	    // TODO actually determine which classes we are compiling, and use that
	    String className = tree.getMethodSymbol().enclClass().toString();
	    if (className.startsWith("java.") || className.startsWith("javax.")) {
		result = tree;
		return;
	    }
	    
	    contextExpr = make.TypeCast(
		    make.Select(make.Ident(dpjruntime), nonintFlagClass),
		    make.Literal(TypeTags.BOT, null));
	} else {
	    log.error("invalid.mode");
	    return;
	}
	
	tree.args = translate(tree.args).append(contextExpr);
	result = tree;
    }
    
    private void visitNewClass_addParameter(JCNewClass tree) {
	super.visitNewClass(tree);
	tree = (JCNewClass)result;
	JCExpression contextExpr;
	
	if (mode == Mode.TRANSACTIONAL) {
	    contextExpr = make.Ident(contextVar);
	} else if (mode == Mode.LOGGING_ONLY) {
	    // Make the context parameter a NonintContext, so that we get the
	    // logging-only version of the constructor, if available.
	    contextExpr = make.TypeCast(
		    make.Select(make.Select(make.Select(make.Ident(
			    org), deuce), transaction), nonintContextClass),
		    make.Ident(contextVar));   
	} else if (mode == Mode.NONINTERFERING) {
	    // Don't add NonintFlag parameter for calls to java.* and javax.* types.
	    // This is conservative but safe with respect to subclassing of
	    // standard library classes.
	    // TODO actually determine which classes we are compiling, and use that
	    String className = tree.constructor.enclClass().toString();
	    if (className.startsWith("java.") || className.startsWith("javax.")) {
		result = tree;
		return;
	    }
	    
	    contextExpr = make.TypeCast(
		    make.Select(make.Ident(dpjruntime), nonintFlagClass),
		    make.Literal(TypeTags.BOT, null));
	} else {
	    log.error("invalid.mode");
	    return;
	}
	
	tree.args = tree.args.append(contextExpr);
	result = tree;
    }
    
    
    /** Decide whether to translate this method, based on its annotations.
     *  Also reset numbering of temp variables
     */
    // TODO Deal with field accesses in field and block initializers
    @Override
    public void visitMethodDef(JCMethodDecl tree) {
	JCMethodDecl prevEnclMethod = enclMethod;
	int prevVarIndex = nextVarIndex;
	List<JCStatement> prevStats = newStats;
	enclMethod = tree;
	nextVarIndex = 1;
	boolean prevInAtomic = inAtomic;
	inAtomic = false;
	
	// Examine annotations to decide if method needs translation
	if (hasCloneAnnotation(tree)) {
	    // TODO Do appropriate translation for any or all of the four modes.
	    mode = Mode.NORMAL;
	    super.visitMethodDef((JCMethodDecl)copy.copy(tree));
	    JCTree nonstmTree = (JCMethodDecl)result;
	    
	    mode = Mode.TRANSACTIONAL;
	    super.visitMethodDef((JCMethodDecl)copy.copy(tree));
	    JCMethodDecl stmTree = (JCMethodDecl)result;
	    
	    mode = Mode.LOGGING_ONLY;
	    super.visitMethodDef((JCMethodDecl)copy.copy(tree));
	    JCMethodDecl loggingOnlyTree = (JCMethodDecl)result;

	    mode = Mode.NONINTERFERING;
	    super.visitMethodDef((JCMethodDecl)copy.copy(tree));
	    JCMethodDecl noninterferingTree = (JCMethodDecl)result;
	    
	    // Don't mangle names, for compatibility with Deuce's bytecode rewriter
	    // TODO Can this create any problems?
	    
	    // Add context parameter to stm tree
	    TypeSymbol tsym = new TypeSymbol(0, contextClass, Type.noType, 
    				new PackageSymbol(transaction,
    				new PackageSymbol(deuce, 
    				new PackageSymbol(org,
    				syms.unnamedPackage))));
	    Type contextType = new ClassType(Type.noType, List.<Type>nil(), 
            			List.<RPL>nil(), 
            			List.<Effects>nil(),  tsym,
            			null);
	    JCVariableDecl contextParam = 
		make.VarDef(new VarSymbol(Flags.FINAL, contextVar, contextType, enclMethod.sym), null);
	    stmTree.params = stmTree.params.append(contextParam);
	    
	    // Add context parameter to logging-only tree
	    tsym = new TypeSymbol(0, nonintContextClass, Type.noType, 
			new PackageSymbol(transaction,
			new PackageSymbol(deuce, 
			new PackageSymbol(org,
			syms.unnamedPackage))));
	    contextType = new ClassType(Type.noType, List.<Type>nil(), 
			List.<RPL>nil(), 
			List.<Effects>nil(),  tsym,
			null);	    
	    contextParam = 
		make.VarDef(new VarSymbol(Flags.FINAL, contextVar, contextType, enclMethod.sym), null);
	    loggingOnlyTree.params = loggingOnlyTree.params.append(contextParam);

	    // Add nonint flag parameter to noninterfering tree
	    tsym = new TypeSymbol(0, nonintFlagClass, Type.noType, 
			new PackageSymbol(dpjruntime, syms.unnamedPackage));
	    contextType = new ClassType(Type.noType, List.<Type>nil(), 
			List.<RPL>nil(), 
			List.<Effects>nil(),  tsym, null);	    
	    contextParam = 
		make.VarDef(new VarSymbol(Flags.FINAL, nonintFlagVar, contextType, enclMethod.sym), null);
	    noninterferingTree.params = noninterferingTree.params.append(contextParam);
	    
	    multipleResults = List.of(nonstmTree, stmTree, loggingOnlyTree, noninterferingTree);
	    result = null;
	} else {
	    mode = Mode.NORMAL;
	    super.visitMethodDef(tree);
	}
	
	nextVarIndex = prevVarIndex;
	enclMethod = prevEnclMethod;
	inAtomic = prevInAtomic;
    }

    // TODO Deal with parameters to control generation of 4 versions
    private boolean hasCloneAnnotation(JCMethodDecl tree) {
	List<JCAnnotation> l = tree.mods.annotations;
	for (; l.nonEmpty(); l = l.tail) {
	    if (l.head.annotationType instanceof JCIdent) {
		JCIdent ident = (JCIdent)l.head.annotationType;
		if (ident.name.equals(clone))
		    return true;
	    }
	}
	return false;
    }
    
    public void visitDPJForLoop(DPJForLoop tree) {
	super.visitDPJForLoop(tree);
	tree = (DPJForLoop)result;
	
	// Always include 'this' as a used var in foreach loops (in instance methods),
	// because barrier insertion can create references to 'this'.
	if (mode != Mode.NONINTERFERING && (enclMethod.mods.flags & Flags.STATIC) == 0) {
	    for(VarSymbol var : tree.usedVars) {
		if (var.name.equals(names._this))
		    return;
	    }
	    tree.usedVars.add(new VarSymbol(Flags.FINAL, names._this, enclClass.type, enclMethod.sym));
	}
    }

    /** Insert new temp variables at the top of the enclosing block
     */
    @Override
    public void visitBlock(JCBlock tree) {
	List<JCStatement> prevStats = newStats;
	newStats = List.nil();
	
	super.visitBlock(tree);
	
	// If we're in a constructor that starts with a "super()" call,
	// we need to keep that before the stuff we insert.
	JCStatement superFirst = null;
	if (tree.stats.head instanceof JCExpressionStatement) {
	    JCExpression expr = ((JCExpressionStatement)tree.stats.head).expr;
	    if (expr instanceof JCMethodInvocation) {
		JCExpression meth = ((JCMethodInvocation)expr).meth;
		if (meth instanceof JCIdent) {
		    Name name = ((JCIdent)meth).name;
		    if (name.equals(names._super)) {
			superFirst = tree.stats.head;
			tree.stats = tree.stats.tail;
		    }
		}
	    }
	}
		
	tree.stats = tree.stats.prependList(newStats.reverse());
	
	if (superFirst != null)
	    tree.stats = tree.stats.prepend(superFirst);
	
	newStats = prevStats;
    }
    
    @Override
    public void visitClassDef(JCClassDecl tree) {
	JCClassDecl prevEnclClass = enclClass;
	enclClass = tree;
	boolean prevMadeClassBase = madeClassBase;
	madeClassBase = false;
	boolean prevInAtomic = inAtomic;
	inAtomic = false;
	
	tree.mods = translate(tree.mods);
	tree.typarams = translateTypeParams(tree.typarams);
	tree.extending = translate(tree.extending);
	tree.implementing = translate(tree.implementing);
	tree.defs = translateDefsList(tree.defs);
	result = tree;
	
	enclClass = prevEnclClass;
	madeClassBase = prevMadeClassBase;
	inAtomic = prevInAtomic;
    }

    /** Translate a list of definitions inside a class.  Allow one definition to
     *  be replaced by two (or more) versions: if the result is null, look at
     *  multipleResults for all the replacement definitions.
     */
    private List<JCTree> translateDefsList(List<JCTree> trees) {
	if (trees == null) return null;
	
	List<JCTree> newList = List.nil();
	for (List<JCTree> l = trees; l.nonEmpty(); l = l.tail) {
	    JCTree result = translate(l.head);
	    if (result != null) {
		newList = newList.prepend(result);
	    } else {
		newList = newList.prependList(multipleResults.reverse());
		multipleResults = null;
	    }
	}
	return newList.reverse();
    }
    
    @Override
    public void visitTypeArray(JCArrayTypeTree tree) {
	tree.elemtype = translate(tree.elemtype);
	tree.rpl = translate(tree.rpl);
	tree.indexParam = tree.indexParam;	// Don't translate
	//TODO Is this right?
	result = tree;
    }

    // Delete any extra parens inserted around expression statements,
    // to conform to Java syntax rules.
    @Override
    public void visitExec(JCExpressionStatement tree) {	
	// Convert post-inc/dec statements to pre-inc/dec for efficiency
	if (tree.expr instanceof JCUnary) {
	    JCUnary unary = (JCUnary)tree.expr;
	    int opcode = unary.getTag();
	    if (opcode == JCTree.POSTINC)
		unary.setTag(JCTree.PREINC);
	    if (opcode == JCTree.POSTDEC)
		unary.setTag(JCTree.PREDEC);
	}
	
	super.visitExec(tree);
	while (tree.expr instanceof JCParens)
	    tree.expr = ((JCParens)tree.expr).expr;
    }
    
    // Insert extra static fields for field offsets
    // Also, insert initializers for local vars.
    @Override
    public void visitVarDef(JCVariableDecl tree) {
	// Only do field transformation for fields of non-inner classes,
	// not on local variables.
	// TODO handle inner classes (which can't contain static fields)
	if (enclMethod != null ||
		enclClass.sym.owner.kind != PCK /* check for inner class */) {
	    super.visitVarDef(tree);
	    return;
	}
	// TODO Exclude some kinds of fields (e.g. final)?
	
	Name offsetVarName = tree.name.append(fieldOffsetSuffix);
	VarSymbol offsetVarSymbol = new VarSymbol(Flags.PUBLIC|Flags.STATIC|Flags.FINAL, 
		offsetVarName, new Type(TypeTags.LONG, null), enclClass.sym);
	List<JCExpression> args = List.of(
		make.Select(make.Ident(enclClass.name), names._class),
		make.Literal(tree.name.toString()));
	JCExpression initExpr = make.App(makeContextDelegatorMethod(GET_FIELD_OFFSET), args);
	JCTree offsetVarExpr = make.VarDef(offsetVarSymbol, initExpr);
	
	// TODO Set the mode here?
	tree.mods = translate(tree.mods);
	tree.vartype = translate(tree.vartype);
	// TODO Deal with (or disallow) calls in initializers
	//tree.init = translate(tree.init);
	
	result = null;
	multipleResults = List.of(tree, offsetVarExpr);
	
	// If this is the first static field we have encountered in this class,
	// use it to make the class_base field needed for transactional access to static fields
	if (!madeClassBase && (tree.mods.flags & Flags.STATIC) != 0) {
	    JCExpression meth = make.Select(make.Select(make.Select(make.Select(make.Ident(
		    org), deuce), reflection), addressUtil), staticFieldBase);
	    meth.setType(new Type(TypeTags.METHOD, null));
	    args = List.of(
		    make.Select(make.Ident(enclClass.name), names._class),
		    make.Literal(tree.name.toString()));
	    JCVariableDecl classBaseDecl = 
		make.VarDef(make.Modifiers(Flags.PUBLIC | Flags.STATIC | Flags.FINAL), 
			    classBase, null, make.Ident(object), make.App(meth, args));
		
	    multipleResults = multipleResults.append(classBaseDecl);
	    madeClassBase = true;
	}
    }
   
    // Translate nonint blocks based on mode
    //TODO Implement a switch to turn nonint optimization on/off
    @Override
    public void visitNonint(DPJNonint tree) {
	switch (mode) {
	case NORMAL:
	    mode = Mode.NONINTERFERING;
	    result = translate(tree.body);
	    mode = Mode.NORMAL;
	    break;
	case TRANSACTIONAL:
	    mode = Mode.LOGGING_ONLY;
	    result = translate(tree.body);
	    mode = Mode.TRANSACTIONAL;
	    break;
	case LOGGING_ONLY:
	case NONINTERFERING:
	    result = translate(tree.body);
	    break;
	}
    }
    
    // Translate atomic blocks based on mode
    @Override
    public void visitAtomic(DPJAtomic tree) {
	switch (mode) {
	case NORMAL:
	    inAtomic = true;
	    result = makeAtomicBlock(tree);
	    inAtomic = false;
	    break;
	case NONINTERFERING:
	case TRANSACTIONAL:
	case LOGGING_ONLY:
	    // Don't generate code to start transaction in these modes
	    result = translate(tree.body);
	    break;
	}
    }
    
    // Generate the code to set up an atomic block
    private JCStatement makeAtomicBlock(DPJAtomic tree) {
	Name n_Throwable = names.fromString("Throwable");
	Name n_ex = names.fromString("ex" + SUFFIX);
	String s_getInstance = "getInstance";
	Name n_commitVar = names.fromString("commit" + SUFFIX);
	Name n_commitMethod = names.fromString("commit");
	Name n_i = names.fromString("i" + SUFFIX);
	Name n_init = names.fromString("init");
	Name n_rollback = names.fromString("rollback");
	Name n_Error = names.fromString("Error");
	Name n_RuntimeException = names.fromString("RuntimeException");
	Name n_t = names.fromString("t" + SUFFIX);
	Name n_backup = names.fromString("_backup" + SUFFIX);
    
	Name n_returnVal = names.fromString("returnVal" + SUFFIX);
	Name n_returning = names.fromString("returning" + SUFFIX);
	Name n_tryBlock = names.fromString(SUFFIX + "_try_block");
	
	JCExpression txExceptionClass = make.Select(make.Select(make.Select(make.Ident(
		org), deuce), transaction), transactionException);
	
	// List of new statements being generated
	List<JCStatement> stats = List.nil();
	JCBlock whileBlock;
	JCBlock ifBlock;
	JCBlock ifExBlock;
	JCBlock elseBlock;
	JCTry tryCatch;
	
	Type methodType = new Type(TypeTags.METHOD, null);
	
	// List of types of throwables that we need to handle (as JCTrees).
	// We'll generate code to deal appropriately with catching and rethrowing
	// each of these types of throwables (any others will be wrapped in a
	// RuntimeException and rethrown in that form.)
	// TODO Calculate the list of checked exception types that we should
	//      handle here, based on what can be thrown by the calls in the
	//      atomic block.
	List<JCTree> throwableTypes = List.nil();
	throwableTypes = throwableTypes.append(make.Ident(n_Error));
	throwableTypes = throwableTypes.append(make.Ident(n_RuntimeException));
	
	// Generate unique ID for each atomic block in the program
	int id = nextAtomicBlockID++;
	
	// Label for the code of this atomic block (so we can break out of it)
	Name n_label = names.fromString(SUFFIX + "_atomic_block_" + id);

	// "Throwable ex;"
	stats = stats.append(
		make.VarDef(make.Modifiers(0), n_ex, null, make.Ident(n_Throwable), 
			null));
	// "final Context context = ContextDelegator.getInstance();"
	stats = stats.append(
		make.VarDef(make.Modifiers(Flags.FINAL), contextVar, null, 
			make.Select(make.Select(make.Select(make.Ident(
				org), deuce), transaction), contextClass), 
			make.App(makeContextDelegatorMethod(s_getInstance))));
	// "boolean commit;"
	stats = stats.append(
		make.VarDef(make.Modifiers(0), n_commitVar, null, make.Type(new Type(TypeTags.BOOLEAN, null)), 
			null));
	// Was: "int i = [max_retries];"
	// Commented out because it's not used in the while loop anymore
	/*
	stats = stats.append(
		make.VarDef(make.Modifiers(0), n_i, null, make.Type(new Type(TypeTags.INT, null)),
			make.Literal(TypeTags.INT, max_retries)));
	*/
	
	// Back up local vars written within the atomic block, so they can be restored on abort.
	// Don't include vars that were potentially uninitialized going into the atomic block
	// (see note in Flow:visitAtomic()).
	tree.definedVars.removeAll(tree.declaredVars);
	for (VarSymbol v: tree.definedVars) {
	    Name backup_name = v.name.append(n_backup);
	    // "[Type of v] v_backup = v;"
	    stats = stats.append(
		    make.VarDef(new VarSymbol(0, backup_name, v.type, enclClass.sym), make.Ident(v)));
	}
	
	// "while (true) { ... }"
	// Was: "while (i-- > 0) { ... }" 
	stats = stats.append(
		make.WhileLoop(
			make.Literal(TypeTags.BOOLEAN, 1),
			/*make.Binary(JCTree.GT, make.Unary(JCTree.POSTDEC, make.Ident(n_i)), 
				make.Literal(TypeTags.INT, 0)),*/ 
			whileBlock = make.Block(0, List.<JCStatement>nil())));
	
		// "commit = true;"
		whileBlock.stats = whileBlock.stats.append(make.Exec(
			make.Assign(make.Ident(n_commitVar), make.Literal(TypeTags.BOOLEAN, 1))));
		// "ex = null;"
		whileBlock.stats = whileBlock.stats.append(make.Exec(
			make.Assign(make.Ident(n_ex), make.Literal(TypeTags.BOT, null))));
		// "context.init(id);"
		whileBlock.stats = whileBlock.stats.append(make.Exec(
			make.App(make.Select(make.Ident(contextVar), n_init).setType(methodType),
				List.<JCExpression>of(make.Literal(TypeTags.INT, id)))));
		
		Type returnType = null;
		boolean haveReturnVal = false;
		if (enclMethod != null && enclMethod.restype != null)
		    returnType = enclMethod.restype.type;
		if (returnType != null && returnType.tag != TypeTags.VOID) {
		    // "[return type] returnVal = [dummy value];"
		    whileBlock.stats = whileBlock.stats.append(
			    make.VarDef(make.Modifiers(0), n_returnVal, null, make.Type(returnType), dummyVal(returnType)));
		    haveReturnVal = true;
		}
		// "boolean returning = false;"
		whileBlock.stats = whileBlock.stats.append(
			make.VarDef(make.Modifiers(0), n_returning, null, 
				make.Type(new Type(TypeTags.BOOLEAN, null)), make.Literal(TypeTags.BOOLEAN, 0)));
		
		// Translate body of atomic block in transactional mode
		mode = Mode.TRANSACTIONAL;
		JCStatement newBody = translate(tree.body);
		mode = Mode.NORMAL;
		
		// "try_block: try { [body] } ..."
		whileBlock.stats = whileBlock.stats.append(make.Labelled(n_tryBlock,
			tryCatch = make.Try(make.Block(0, List.of(newBody)), List.<JCCatch>nil(), null)));
	        // "catch (TransactionException t) { commit = false; }"
	        tryCatch.catchers = tryCatch.catchers.append(
	        	make.Catch(make.VarDef(make.Modifiers(0), n_t, null, (JCExpression)copy.copy(txExceptionClass), null),
				make.Block(0, List.<JCStatement>of(make.Exec(
					make.Assign(make.Ident(n_commitVar), make.Literal(TypeTags.BOOLEAN, 0)))))));
		// "catch (Throwable t) { ex = t; }"
		tryCatch.catchers = tryCatch.catchers.append(
			make.Catch(make.VarDef(make.Modifiers(0), n_t, null, make.Ident(n_Throwable), null), 
				make.Block(0, List.<JCStatement>of(make.Exec(
					make.Assign(make.Ident(n_ex), make.Ident(n_t)))))));
		
		// "if (commit) {
		//	if (context.commit()) {...}
		//  } else {...}"
		whileBlock.stats = whileBlock.stats.append(
			make.If(make.Ident(n_commitVar), 
				make.Block(0, List.<JCStatement>of(
					make.If(make.App(make.Select(make.Ident(contextVar), n_commitMethod).
								setType(methodType)), 
						ifBlock = make.Block(0, List.<JCStatement>nil()), null))),
				elseBlock = make.Block(0, List.<JCStatement>nil())));
			
			// if block: Successful commit case
			// "if (returning) return [returnVal];"
			ifBlock.stats = ifBlock.stats.append(
				make.If(make.Ident(n_returning), 
					make.Return(haveReturnVal ? make.Ident(n_returnVal) : null), null));
		
			// "if (ex != null) {...}"
			ifBlock.stats = ifBlock.stats.append(
				make.If(make.Binary(JCTree.NE, make.Ident(n_ex), make.Literal(TypeTags.BOT, null)), 
					ifExBlock = make.Block(0, List.<JCStatement>nil()), null));
			
				// Generate code to handle each throwable type that we need to deal with
				for (JCTree throwableType: throwableTypes) {
				    // "if (ex instanceof throwableType) throw (throwableType)ex;"
				    ifExBlock.stats = ifExBlock.stats.append(
					    make.If(make.TypeTest(make.Ident(n_ex), copy.copy(throwableType)),
					    	make.Throw(make.TypeCast(copy.copy(throwableType), 
					    		make.Ident(n_ex))), null));
				}
				// "throw new RuntimeException(ex);"
				ifExBlock.stats = ifExBlock.stats.append(
					make.Throw(make.NewClass(null, List.<DPJRegionPathList>nil(), 
						List.<JCExpression>nil(), List.<DPJEffect>nil(), 
						make.Ident(n_RuntimeException), 
						List.<JCExpression>of(make.Ident(n_ex)), null)));

			if (tree.aliveAtEnd) { 
			    // "break atomic_block_N;"
			    ifBlock.stats = ifBlock.stats.append(make.Break(n_label));
			} else {
			    // If control never reaches the end of the atomic block, then insert a 
			    // dummy throw statement (which should never be reached) to make
			    // javac's control flow analysis happy.
			    ifBlock.stats = ifBlock.stats.append(
				    make.Throw(make.NewClass(null, List.<DPJRegionPathList>nil(), 
						List.<JCExpression>nil(), List.<DPJEffect>nil(), 
						make.Ident(n_RuntimeException), 
						List.<JCExpression>nil(), null)));
			}
				
			// else block: roll back and retry case
			// "commit.rollback();"
			elseBlock.stats = elseBlock.stats.append(make.Exec(
				make.App(make.Select(make.Ident(contextVar), n_rollback).setType(methodType))));
		
		// Restore backed up local variables if transaction didn't commit successfully
		for (VarSymbol v: tree.definedVars) {
		    Name backup_name = v.name.append(n_backup);
		    // "v = v_backup;"
		    whileBlock.stats = whileBlock.stats.append(make.Exec(
			    make.Assign(make.Ident(v), make.Ident(backup_name))));
		}
			
	// Was: "throw new TransactionException();"
	// Commented out because it's now unreachable when the while loop is "while (true) { ... }"
	/*
	stats = stats.append(
		make.Throw(make.NewClass(null, List.<DPJRegionPathList>nil(), List.<JCExpression>nil(), 
			List.<DPJEffect>nil(), (JCExpression)copy.copy(txExceptionClass), List.<JCExpression>nil(), null)));
	*/
	
	return make.Labelled(n_label, make.Block(0, stats));
    }
    
    //Translate return statements within atomic blocks
    public void visitReturn(JCReturn tree) {
	Name n_returnVal = names.fromString("returnVal" + SUFFIX);
	Name n_returning = names.fromString("returning" + SUFFIX);
	Name n_tryBlock = names.fromString(SUFFIX + "_try_block");
	
	if (inAtomic) {
	    JCBlock block = make.Block(0, List.<JCStatement>nil());
		
	    if (enclMethod != null && enclMethod.restype != null &&
		    enclMethod.restype.type.tag != TypeTags.VOID) {
		// "returnVal = ...;"
		block.stats = block.stats.append(make.Exec(
			make.Assign(make.Ident(n_returnVal), translate(tree.expr))));
	    }
	    // "returning = true;"
	    block.stats = block.stats.append(make.Exec(
		    make.Assign(make.Ident(n_returning), make.Literal(TypeTags.BOOLEAN, 1))));
	    // "break tryBlock;"
	    block.stats = block.stats.append(
		    make.Break(n_tryBlock));
	    
	    result = block;
	} else {
	    super.visitReturn(tree);
	}
    }
    
    // This return an expression for some value that can be used to initialize a variable of the given type
    JCExpression dummyVal(Type type) {
	if (type.tag == TypeTags.BOOLEAN) {
	    return make.Literal(TypeTags.BOOLEAN, 0);
	} else if (type.tag <= TypeTags.lastBaseTag) {
	    return make.Literal(TypeTags.INT, 0);
	} else {
	    return make.Literal(TypeTags.BOT, null);
	}
    }
}
