/**
 * These tests verify that array subtyping errors are being caught
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;

import com.sun.tools.javac.tree.Pretty;

public class ArraySubtypingBad extends DPJTestCase {
    
    private  void doTest(String name) throws Throwable {
	analyzeExpectingError(Pretty.NONE, parse("ArraySubtypingBad/" + name + ".java"));
    }
    
    @Test public void testClassElementMismatch() throws Throwable {
	doTest("ClassElementMismatch");
    }
    
    @Test public void testRPLMismatch() throws Throwable {
	doTest("RPLMismatch");
    }
    
}
