/**
 * These tests verify that errors involving class subtyping are being
 * caught
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class ClassSubtypingBad extends DPJTestCase {
    
    public ClassSubtypingBad() {
	super("ClassSubtypingBad");
    }
    
    @Test public void testBadAssignDefault() throws Throwable {
	compileExpectingErrors("NameDefaultMismatch", 1);
    }
    
    @Test public void testClassRgnParams() throws Throwable {
	compileExpectingErrors("MismatchedNames", 1);
    }
    
    @Test public void testCapture() throws Throwable {
	compileExpectingErrors("Capture", 1);
    }
    
    @Test public void testNameParamMismatch() throws Throwable {
	compileExpectingErrors("NameParamMismatch", 1);
    }

    @Test public void testDifferentParams() throws Throwable {
	compileExpectingErrors("DifferentParams", 1);
    }

    @Test public void testUnsoundCast() throws Throwable {
	// Warning disabled here
	compile("UnsoundCast");
    }

}
