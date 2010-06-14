/**
 * These tests verify that array subtyping errors are being caught
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;

import com.sun.tools.javac.tree.Pretty;

public class ArrayAccessBad extends DPJTestCase {
    
    private  void doTest(String name) throws Throwable {
	analyzeExpectingError(Pretty.NONE, parse("ArrayAccessBad/" + name + ".java"));
    }
    
    @Test public void testIndexMismatch() throws Throwable {
	doTest("IndexMismatch");
    }
    
    @Test public void testRPLMismatch() throws Throwable {
	doTest("RPLMismatch");
    }
    
    @Test public void testIndexTypeAssignNonFinal() throws Throwable {
	doTest("IndexTypeAssignNonFinal");
    }
    
}
