/**
 * These tests verify that correct class subtyping is working
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;

import com.sun.tools.javac.tree.Pretty;

public class ClassSubtypingGood extends DPJTestCase {
    
    private  void doTest(String name) throws Throwable {
	analyze(Pretty.NONE, parse("ClassSubtypingGood/" + name + ".java"));
    }

    @Test public void testInterfaceRPLParam() throws Throwable {
	doTest("InterfaceRPLParam");
    }

    @Test public void testMultipleParams() throws Throwable {
	doTest("MultipleParams");
    }

    @Test public void testThisInSuper() throws Throwable {
	doTest("ThisInSuper");
    }
    
    @Test public void testRPLInExtends() throws Throwable {
	doTest("RPLInExtends");
    }

}

