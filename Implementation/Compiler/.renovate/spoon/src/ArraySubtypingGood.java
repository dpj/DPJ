/**
 * These tests verify that correct array subtyping cases are working
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;

import com.sun.tools.javac.tree.Pretty;

public class ArraySubtypingGood extends DPJTestCase {
    
    private  void doTest(String name) throws Throwable {
	analyze(Pretty.NONE, parse("ArraySubtypingGood/" + name + ".java"));
    }
    
    @Test public void testOneDimEqualTypes() throws Throwable {
	doTest("OneDimEqualTypes");
    }
    
    @Test public void testOneDimIncludedRPLs() throws Throwable {
	doTest("OneDimIncludedRPLs");
    }

}
