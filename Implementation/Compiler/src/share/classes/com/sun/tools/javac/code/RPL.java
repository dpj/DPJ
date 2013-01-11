package com.sun.tools.javac.code;

import java.util.Iterator;

import com.sun.tools.javac.code.RPLElement.ArrayIndexRPLElement;
import com.sun.tools.javac.code.RPLElement.RPLCaptureParameter;
import com.sun.tools.javac.code.RPLElement.RPLParameterElement;
import com.sun.tools.javac.code.RPLElement.UndetRPLParameterElement;
import com.sun.tools.javac.code.RPLElement.VarRPLElement;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Substitute.AsMemberOf;
import com.sun.tools.javac.code.Substitute.SubstRPLs;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.Type.TypeVar;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.Resolve;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

/** A class for representing DPJ region path lists (RPLs).  An RPL is a list
 *  of RPL elements.  Various operations on RPLs, pairs of RPLs, and lists
 *  of RPLs required by the DPJ type system are supported.
 */
public class RPL implements 
	SubstRPLs<RPL>,
	AsMemberOf<RPL>
{

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    /** The elements comprising this RPL */
    public List<RPLElement> elts;
        
    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public RPL() {
	this.elts = List.nil();
    }
    
    public RPL(RPLElement singletonElement) {
	this.elts = List.of(singletonElement);
    }
    
    public RPL(List<RPLElement> elts) {
	this.elts = elts;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // RPL query methods
    ///////////////////////////////////////////////////////////////////////////
    
    public int size() {
	return elts.size();
    }
    
    public boolean isEmpty() {
	return elts.isEmpty();
    }

    public boolean isAtomic() {
	for (RPLElement elt : elts) {
	    if (elt.isAtomic()) return true;
	}
	return false;
    }
    
    /** RPL under relation
     *  See Section 1.2.2 of the DPJ Tech Report
     */
    public boolean isNestedUnder(RPL that) {
	// Deal with z regions and capture parameters by converting first element
	// to its upper bound
	RPL upperBound = this.upperBound();
	if (upperBound != this && upperBound.isNestedUnder(that))
	    return true;
	// UNDER-ROOT
	if (that.isRoot()) return true;
	// UNDER-NAME
	if (!this.isEmpty()) {
	    if (this.withoutLastElement().isNestedUnder(that)) return true;
	}
	// UNDER-STAR
	if (this.endsWithStar() && this.withoutLastElement().isNestedUnder(that)) return true;
	// UNDER-INCLUDE
	if (this.isIncludedIn(that)) return true;
	return false;
    }
    
    /**
     * Does this RPL start with a local region name, or is it under an RPL
     * that starts with a local region name?
     */
    public boolean isUnderLocal() {
	RPL upperBound = this.upperBound();
	if (upperBound != this && upperBound.isUnderLocal())
	    return true;
	if (elts.isEmpty() || elts.head == null)
	    return false;
	return elts.head.isLocalName();
    }

    /** RPL inclusion relation
     *  See Section 1.2.3 of the DPJ Tech Report
     */
    public boolean isIncludedIn(RPL that) {
	// Handle capture parameters
	if (this.elts.head instanceof RPLCaptureParameter) {
	    return this.upperBound().isIncludedIn(that);
	}
	// Handle undetermined parameters
	if (that.elts.head instanceof UndetRPLParameterElement) {
	    UndetRPLParameterElement element = 
		(UndetRPLParameterElement) that.elts.head;
	    if (element.includedIn == null) {
		// Nothing to do
	    } else if (this.isIncludedIn(element.includedIn)) {
		element.includedIn = this;
	    } else {
		element.includedIn = null;
	    }
	    return true;	    
	}
	// Reflexivity
	if (this.equals(that)) return true;
	// INCLUDE-STAR
	if (that.endsWithStar()) {
	    if (this.isNestedUnder(that.withoutLastElement())) return true;
	}
	// INCLUDE-NAME
	if (!this.isEmpty() && !that.isEmpty()) {
	    if (this.elts.last().isIncludedIn(that.elts.last()) && 
		    this.withoutLastElement().isIncludedIn(that.withoutLastElement()))
		return true;
	}
	return false;
    }
    
    private boolean endsWithStar() {
	return size() > 1 && elts.last() == RPLElement.STAR;
    }
    
    private boolean isRoot() {
	return size() == 1 && (elts.last() == RPLElement.ROOT_ELEMENT);
    }
    
    /**
     * Is this RPL fully specified?  An RPL is fully specified if it contains
     * no * or [?].
     */
    
    public boolean isFullySpecified() {
	for (RPLElement e : elts) {
	    if (!e.isFullySpecified()) return false;
	}
	return true;
    }

    
    
    /**
     * Compute an upper bound for this RPL
     */
    public RPL upperBound() {
	if (elts.isEmpty() ||
		(!(elts.head instanceof VarRPLElement) &&
			!(elts.head instanceof RPLCaptureParameter))) 
	    return this;
	RPL upperBound = elts.head.upperBound();
	//if (elts.size() == 1) return upperBound;
	return new RPL(upperBound.elts.appendList(elts.tail));
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // RPL manipulation methods
    ///////////////////////////////////////////////////////////////////////////
    
    private RPL withoutLastElement() {
	ListBuffer<RPLElement> buf = new ListBuffer<RPLElement>();
	List<RPLElement> elts = this.elts;
	while (elts.tail != null) {
	    if (elts.tail.tail == null) break;
	    buf.append(elts.head);
	    elts = elts.tail;
	}
	return new RPL(buf.toList());
    }
    
    /** Replace 'from' RPL params with 'to' RPLs */
    public RPL substRPLParams(Iterable<RPL> from, Iterable<RPL> to) {
	return Substitute.substIterable(RPLs.substRPLParams, this, from, to);
    }

    public RPL substRPLParam(RPL from, RPL to) {
	if (this.elts.head.equals(from.elts.head))
	    return new RPL(to.elts.appendList(this.elts.tail));
	return this;
    }
    
    /**
     * Do TR substitutions implied by type bindings
     */
    public RPL substTRParams(Iterable<Type> from, Iterable<Type> to) {
	RPL result = this;
	Iterator<Type> fromIterator = from.iterator();
	Iterator<Type> toIterator = to.iterator();
	while (fromIterator.hasNext() && toIterator.hasNext()) {
	    Type fromType = fromIterator.next();
	    Type toType = toIterator.next();
	    if (fromType instanceof TypeVar) {
		List<RPL> params = fromType.tsym.type.getRPLArguments();
		List<RPL> args = toType.getRPLArguments();
		result = this.substRPLParams(params, args);
		if (result != this)
		    return result;
	    }
	}
	return result;
    }
    
    public static RPL exprToRPL(JCExpression tree) {
	Symbol sym = tree.getSymbol();
	if (sym != null) return symToRPL(sym);
	RPL owner = tree.type.getOwner();
	RPL result = new RPL(owner.elts.append(RPLElement.STAR));
	return result;
    }
    
    public static RPL symToRPL(Symbol sym) {
	RPL result = null;
	if ((sym instanceof VarSymbol) &&
		(sym.owner.kind == Kinds.MTH || sym.name.toString().equals("this"))
	    && (sym.flags() & Flags.FINAL) != 0) {
	    // If the variable is a final local variable, use it as the RPL
	    result = new RPL(List.<RPLElement>of(new RPLElement.VarRPLElement((VarSymbol) sym)));
	} else {
	    // Otherwise, use the owner region
	    RPL owner = sym.type.getOwner();
	    result = new RPL(owner.elts.append(RPLElement.STAR));
	}
	return result;	
    }

    public RPL substRPLForVar(VarSymbol from, RPL to) {
	if (!(this.elts.head instanceof RPLElement.VarRPLElement)) return this;
	RPLElement.VarRPLElement vrs = (RPLElement.VarRPLElement) this.elts.head;
	if (vrs.vsym != from) return this;
	return new RPL(to.elts.appendList(this.elts.tail));
    }

    public RPL substVar(VarSymbol from, VarSymbol to) {
	RPL toRPL = symToRPL(to);
	return (toRPL == null) ? this : substRPLForVar(from, toRPL);	
    }

    public RPL substVars(List<VarSymbol> from, List<VarSymbol> to) {
	RPL result = this;
	while (from.nonEmpty()) {
	    result = this.substVar(from.head, to.head);
	    from = from.tail;
	    to = to.tail;
	}
	return result;		
    }
    
    public RPL substExpForVar(VarSymbol from, JCExpression to) {
	RPL toRPL = exprToRPL(to);
	return (toRPL == null) ? this : substRPLForVar(from, toRPL);
    }

    public RPL substExpsForVars(List<VarSymbol> from, List<JCExpression> to) {
	RPL result = this;
	while (from.nonEmpty() && to.nonEmpty()) {
	    result = this.substExpForVar(from.head, to.head);
	    if (result != this) {
		break;
	    }
	    from = from.tail;
	    to = to.tail;
	}
	return result;	
    }
    
    public RPL substIndices(List<VarSymbol> from, List<JCExpression> to) {
	RPL result = this;
	while (!from.isEmpty() && !to.isEmpty()) {
	    result = result.substIndex(from.head, to.head);
	    from = from.tail;
	    to = to.tail;
	}
	return result;
    }

    public RPL substIndex(VarSymbol from, JCExpression to) {
	ListBuffer<RPLElement> buf = ListBuffer.<RPLElement>lb();
	for (RPLElement e : elts) {
	    if (e instanceof ArrayIndexRPLElement) {
		ArrayIndexRPLElement ai = (ArrayIndexRPLElement) e;
		JCExpression subst = substIndex(ai.indexExp, from, to);
		if (subst != ai.indexExp)
		    e = new ArrayIndexRPLElement(subst);
	    }
	    buf.append(e);
	}
	return new RPL(buf.toList());
    }

    
    protected JCExpression substIndex(JCExpression tree, VarSymbol from,
	    JCExpression to) {
	return (new SubstIndexVisitor(from, to)).substIndex(tree);
    }

    /**
     * A simple visitor for substituting expressions for index variables.
     * Note the following:
     * 
     *  1. We can't use TreeTranslator here, because we don't want to mess
     *     up the actual AST used by the program!  So we have to replicate
     *     every node that we want to modify.
     *     
     *  2. This simple visitor only handles singleton indices and (recursively)
     *     binary expressions containing singleton indices.  However, that's
     *     enough for now: because we can only disambiguate constants, negated
     *     variables, and binary expressions in array typing, expanding this
     *     visitor to do more would be pointless.  If and when we add more
     *     robust expression disambiguation, we can expand this visitor as
     *     necessary.
     */
    private static class SubstIndexVisitor extends JCTree.Visitor {
	private VarSymbol from = null;
	private JCExpression to = null;
	public JCExpression result = null;
	public SubstIndexVisitor(VarSymbol from, JCExpression to) {
	    this.from = from;
	    this.to = to;
	}
	public JCExpression substIndex(JCExpression tree) {
	    result = tree;
	    if (tree != null)
		tree.accept(this);
	    return result;
	}
        public void visitIdent(JCIdent tree) {
	    if (tree.sym == from) {
		result = to;
	    }
        }
        public void visitBinary(JCBinary tree) {
            JCExpression lhs = substIndex(tree.lhs);
            JCExpression rhs = substIndex(tree.rhs);
            if (lhs != tree.lhs || rhs != tree.rhs) {
        	result = new JCBinary(tree.getTag(), lhs, rhs, tree.getOperator());
        	result.pos = tree.pos;
            }
        }
        @Override
        public void visitTree(JCTree tree) {}
    };

    /** Compute the capture of an RPL:
     *  - If the RPL is fully specified, then the capture is the same as the input
     *  - If the RPL is partially specified, then the capture is a fresh RPL consisting
     *    of a fresh capture parameter under the input RPL
     */
    
    public RPL capture() {
	return this.isFullySpecified() ? this : 
	    new RPL(new RPLElement.RPLCaptureParameter(this));
    }
    
    /**
     * The RPL as a member of t
     * @param t     The type where we want this to be a member, after translation
     */
    public RPL asMemberOf(Type t, Types types) {
	RPLElement elt = this.elts.head;
        if (elt instanceof RPLParameterElement) {
            RPLParameterElement paramElt =
                    (RPLParameterElement) elt;
	    Symbol owner = paramElt.sym.enclClass();
            return this.asMemberOf(types, t, owner);
        }
        return this;
    }
    //where
    private RPL asMemberOf(Types types, Type t, Symbol owner) {
	RPL result = this;
	if (owner.type.hasRegionParams()) {
            Type base = types.asOuterSuper(t, owner);
            if (base != null) {
                List<RPL> from = owner.type.allrgnparams();
                List<RPL> to = base.allrgnactuals();
                if (from.nonEmpty()) {
                    result = result.substRPLParams(from, to);
                }
                result = result.substTRParams(owner.type.alltyparams(), 
                	base.alltyparams());
            }
        }
	return result;
	
    }
    

    
    /**
     * Conform the RPL to an enclosing environment.  An RPL may contain 
     * elements written in terms of local region names and/ or local variables
     * that are no longer in scope.  If so, we need to either (1) replace the RPL 
     * with a more general RPL whose elements are in scope; or (2) delete the
     * RPL (i.e., return null), if all regions it represents are out of scope.
     */
    public RPL inEnvironment(Resolve rs, Env<AttrContext> env, boolean pruneLocalEffects) {
	// If the RPL is a capture parameter, compute its bound in the enclosing
	// environment
	if (elts.head instanceof RPLCaptureParameter) {
	    RPLCaptureParameter capture = (RPLCaptureParameter) elts.head;
	    RPL includedIn = capture.includedIn.inEnvironment(rs, env, pruneLocalEffects);
	    // If bound is out of scope, so is capture parameter
	    if (includedIn == null) return null;
	    // Otherwise return new parameter only if bound changed
	    return (includedIn == capture.includedIn) ? this :
		new RPL(List.<RPLElement>of(new RPLCaptureParameter(includedIn)).
			appendList(elts.tail));
	}	
	// If the RPL starts with a variable v that is out of scope, then
	// replace the whole thing with R : *, where R is the owner parameter 
	// of v's type, in this environment.  Note that if R itself is out of
	// scope, the whole RPL may be deleted.
	if (elts.head instanceof RPLElement.VarRPLElement) {
	    RPLElement.VarRPLElement vrs = (RPLElement.VarRPLElement) elts.head;
	    if (!rs.isInScope(vrs.vsym, env)) {
		if (vrs.vsym.type instanceof ClassType) {
		    ClassType ct = (ClassType) vrs.vsym.type;
		    List<RPL> actuals = ct.getRPLArguments();
		    RPL owner = actuals.isEmpty() ? RPLs.ROOT : actuals.head;
			return new RPL(owner.elts.append(RPLElement.STAR).appendList(elts.tail)).inEnvironment(rs, env, pruneLocalEffects);
		}		
	    }
	}
	// Truncate an RPL containing a non-variable element E that is out of scope.  If
	// E occurs in the first position, then the whole RPL is out of scope; return null.  
	// Otherwise, replace E and all following elements with *.
	for (RPLElement elt : elts) {
	    if (!rs.isInScope(elt, env))
		return this.truncateTo(elt);
	    if (pruneLocalEffects && elt.isLocalName())
		return this.truncateTo(elt);
	}
	// Otherwise, go through the elements and look for array index elements
	// [e] where e is out of scope.  Replace every such [e] with [?].
	ListBuffer<RPLElement> buf = ListBuffer.lb();
	boolean added = false;
	for (RPLElement elt : elts) {
	    if (elt instanceof ArrayIndexRPLElement) {
		ArrayIndexRPLElement ae = (ArrayIndexRPLElement) elt;
		if (!rs.isInScope(ae.indexExp, env)) {
		    // Replace elt with [?]
		    elt = new ArrayIndexRPLElement(null);
		    added = true;
		}
	    }
	    buf.append(elt);
	}
	return added ? new RPL(buf.toList()) : this;
    }
    
    RPL truncateTo(RPLElement elt) {
	ListBuffer<RPLElement> buf = ListBuffer.lb();
	List<RPLElement> list = elts;
	while (list.nonEmpty() && list.head != elt) {
	    buf.append(list.head);
	    list = list.tail;
	}
	if (buf.isEmpty()) return null;
	buf.append(RPLElement.STAR);
	return new RPL(buf.toList());
    }
    
    ///////////////////////////////////////////////////////////////////////////

    @Override public boolean equals(Object other) {
	if (this == other)
	    return true;
	else if (other != null && other instanceof RPL)
	    return this.elts.equals(((RPL)other).elts);
	else
	    return false;
    }
  
    @Override public int hashCode() {
	return elts.hashCode();
    }
    
    ///////////////////////////////////////////////////////////////////////////

    @Override public String toString() {
	StringBuilder sb = new StringBuilder();
	boolean first = true;
	for (RPLElement e : elts) {
	    if (first) first = false; else sb.append(" : ");
	    sb.append(e);
	}
	return sb.toString();
    }

    /**
     * The Java source which this RPL list represents.  A List is
     * represented as a comma-separated listing of the elements in
     * that list.
     */
    public static String toString(java.util.List<RPL> rpls) {
	return rpls.toString();
    }
    
    public boolean containsArrayAccess() {
	for (RPLElement e : elts)
	    if (e instanceof ArrayIndexRPLElement)
		return true;
	return false;
    }
    
}
