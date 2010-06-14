/**
 * These tests verify that errors involving class subtyping are being
 * caught
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;

import com.sun.tools.javac.tree.Pretty;

public class ClassSubtypingBad extends DPJTestCase {
    
    private  void doTest(String name) throws Throwable {
	analyzeExpectingError(Pretty.NONE, parse("ClassSubtypingBad/" + name + ".java"));
    }
 
    @Test public void testBadAssignDefault() throws Throwable {
	doTest("NameDefaultMismatch");
    }
    
    @Test public void testClassRgnParams() throws Throwable {
	doTest("MismatchedNames");
    }
    
    @Test public void testCapture() throws Throwable {
	doTest("Capture");
    }
    
    @Test public void testMoreClassRgnParams() throws Throwable {
	doTest("NameParamMismatch");
    }

    @Test public void testDifferentParams() throws Throwable {
	doTest("DifferentParams");
    }

    @Test public void testUnsoundCast() throws Throwable {
	// Warning disabled here
	analyze(Pretty.NONE, parse("ClassSubtypingBad/UnsoundCast.java"));
    }

}
