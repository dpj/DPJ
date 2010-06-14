/**
 * These tests verify that array subtyping errors are being caught
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class ArrayAccessBad extends DPJTestCase {
    
    public ArrayAccessBad() {
	super("ArrayAccessBad");
    }
    
    @Test public void testIndexMismatch() throws Throwable {
	compileExpectingErrors("IndexMismatch", 1);
    }
    
    @Test public void testIndexTypeAssignNonFinal() throws Throwable {
	compileExpectingErrors("IndexTypeAssignNonFinal", 1);
    }
    
}
