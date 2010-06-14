/**
 * These tests verify that the compiler correctly catches interfering effects.
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;

import com.sun.tools.javac.tree.Pretty;

public class MethodParamsBad extends DPJTestCase {
    
    private  void doTest(String name) throws Throwable {
	analyzeExpectingWarnings(1, parse("MethodParamsBad/" + name + ".java"));
    }
    
    @Test public void testMethodParams() throws Throwable {
	doTest("MethodParams");
    }
    
    @Test public void testInferredMethodArgs() throws Throwable {
	doTest("InferredMethodArgs");
    }
    
    @Test public void testCantInferMethodArgss() throws Throwable {
	doTest("CantInferMethodArgs");
    }
    
}
