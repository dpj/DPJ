/**
 * These tests verify that the compiler correctly catches interfering effects.
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;

import com.sun.tools.javac.tree.Pretty;

public class InterferenceCobeginBad extends DPJTestCase {
    
    public InterferenceCobeginBad() {
	super("InterferenceCobeginBad");
    }
    
    @Test public void testOverlappingWrites() throws Throwable {
	compileExpectingWarnings("OverlappingWrites", 1);
    }
    
    @Test public void testOverlappingReadWrite() throws Throwable {
	compileExpectingWarnings("OverlappingReadWrite", 1);
    }
    
    @Test public void testOverlappingInvoke() throws Throwable {
	compileExpectingWarnings("OverlappingInvoke", 1);
    }
    
    @Test public void testInterferingStackVariables() throws Throwable {
	compileExpectingWarnings("StackVariableOutside", 1);
    }
    
    @Test public void testStackIntererenceInForeach() throws Throwable {
	compileExpectingWarnings("StackInterferenceInForeach", 1);
    }
    
    @Test public void testLocalRegions() throws Throwable {
	compileExpectingWarnings("LocalRegions", 1);
    }
}
