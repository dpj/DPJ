/**
 * These tests verify that array subtyping errors are being caught
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class ArraySubtypingBad extends DPJTestCase {
    
    public ArraySubtypingBad() {
	super("ArraySubtypingBad");
    }
    
    @Test public void testClassElementMismatch() throws Throwable {
	compileExpectingErrors("ClassElementMismatch", 1);
    }
    
    @Test public void testRPLMismatch() throws Throwable {
	compileExpectingErrors("RPLMismatch", 1);
    }
    
}
