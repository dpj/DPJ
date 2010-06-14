/**
 * These tests verify that the compiler correctly catches interfering effects
 * in the context of a 'foreach' loop.
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;

import com.sun.tools.javac.tree.Pretty;

public class InterferenceForeachBad extends DPJTestCase {
    
    private  void doTest(String name) throws Throwable {
	analyzeExpectingWarnings(1, parse("InterferenceForeachBad/" + name + ".java"));
    }
    
    @Test public void testStackIntererenceInForeach() throws Throwable {
	doTest("StackInterferenceNestedForeach");
    }
    
    @Test public void testCommutativeMethod() throws Throwable {
	doTest("CommutativeMethod");
    }
}
