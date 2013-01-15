/**
 * Error tests for array classes
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class ArrayClassBad extends DPJTestCase {
    
    public ArrayClassBad() {
	super("ArrayClassBad");
    }
    
    @Test public void testInterference() throws Throwable {
	compileExpectingWarnings("Interference", 1);
    }

}
