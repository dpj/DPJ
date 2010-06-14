/**
 * These tests verify that the compiler correctly catches incorrect uses
 * of method RPL parameters and arguments.
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;

import com.sun.tools.javac.tree.Pretty;

public class MethodParamsBad extends DPJTestCase {
    
    public MethodParamsBad() {
	super("MethodParamsBad");
    }
     
    @Test public void testMethodParams() throws Throwable {
	compileExpectingWarnings("MethodParams", 1);
    }
    
    @Test public void testInferredMethodArgs() throws Throwable {
	compileExpectingWarnings("InferredMethodArgs", 1);
    }
    
    @Test public void testCantInferMethodArgss() throws Throwable {
	compileExpectingWarnings("CantInferMethodArgs", 1);
    }
    
    @Test public void testConflictingParams() throws Throwable {
	compileExpectingErrors("ConflictingParams", 1);
    }
    
}
