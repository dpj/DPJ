/**
 * These tests verify that correct class subtyping is working
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class ClassSubtypingGood extends DPJTestCase {
    
    public ClassSubtypingGood() {
	super("ClassSubtypingGood");
    }

    @Test public void testInterfaceRPLParam() throws Throwable {
	compile("InterfaceRPLParam");
    }

    @Test public void testMultipleParams() throws Throwable {
	compile("MultipleParams");
    }

    @Test public void testThisInSuper() throws Throwable {
	compile("ThisInSuper");
    }
    
    @Test public void testRPLInExtends() throws Throwable {
	compile("RPLInExtends");
    }

}

