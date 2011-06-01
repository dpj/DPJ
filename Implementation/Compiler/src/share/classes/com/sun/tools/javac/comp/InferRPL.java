package com.sun.tools.javac.comp;

import com.sun.tools.javac.code.RPL;
import com.sun.tools.javac.code.RPLElement;
import com.sun.tools.javac.code.RPLs;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.code.RPLElement.RPLParameterElement;
import com.sun.tools.javac.code.RPLElement.UndetRPLParameterElement;
import com.sun.tools.javac.code.Symbol.RegionParameterSymbol;
import com.sun.tools.javac.code.Type.MethodType;
import com.sun.tools.javac.code.Type.UndetVar;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Warner;

public class InferRPL {
    
    protected static final Context.Key<InferRPL> inferRPLKey =
	new Context.Key<InferRPL>();

    RPLs rpls;
    Types types;
    Symtab syms;

    public static InferRPL instance(Context context) {
	InferRPL instance = context.get(inferRPLKey);
	if (instance == null)
	    instance = new InferRPL(context);
	return instance;
    }

    protected InferRPL(Context context) {
	context.put(inferRPLKey, this);
	rpls = RPLs.instance(context);
	types = Types.instance(context);
	syms = Symtab.instance(context);
    }

    public Type instantiateMethod(List<RPL> rvars, List<Type> tvars,
	    MethodType mt, List<Type> actualtypes, boolean allowBoxing, Warner warn) {
	ListBuffer<RPL> buf = ListBuffer.lb();
	for (RPL rvar : rvars) {
	    if (rvar.size() == 1 && rvar.elts.head instanceof RPLParameterElement)
		buf.append(new RPL(new 
			UndetRPLParameterElement(((RPLParameterElement) rvar.elts.head).sym)));
	    else
		buf.append(rvar);
	}
	List<RPL> undetvars = buf.toList();
	List<Type> formaltypes = types.substRPL(mt.argtypes, rpls.toParams(rvars), undetvars);
	
	for (Type t : tvars) {
	    formaltypes = types.subst(formaltypes, List.of(t), List.<Type>of(new UndetVar(t)));
	}
	// Note:  This step fills in the bounds on the undet variables.  See RPL.isIncludedIn
	// for details.  This is kind of a hack, but it makes for a very lean coding of the
	// algorithm, i.e., no extra machinery is required to fill in the bounds.
	if (!types.isSubtypesUnchecked(actualtypes, formaltypes, allowBoxing, warn)) {
	    return null;
	}
	buf = ListBuffer.lb();
	for (RPL undetvar : undetvars) {
	    if (undetvar.size() == 1 && 
		    undetvar.elts.head instanceof UndetRPLParameterElement) {
		UndetRPLParameterElement element = (UndetRPLParameterElement) undetvar.elts.head;
		if (element.includedIn == null)
		    element.includedIn = 
			new RPL(List.<RPLElement>of(RPLElement.ROOT_ELEMENT, RPLElement.STAR));
		buf.append(element.includedIn);
	    } else {
		buf.append(undetvar);
	    }
	}
	//System.err.println("mt now ="+mt);
	//System.err.println("rvars as params="+rpls.toParams(rvars));
	//System.err.println("actuals="+buf.toList());
	mt = (MethodType) types.substRPL(mt, rpls.toParams(rvars), buf.toList());
	mt.regionActuals = buf.toList();
	return mt;
    }
}