/**
 * These tests verify that the compiler correctly catches interfering effects
 * in the context of a 'foreach' loop.
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;

import com.sun.tools.javac.tree.Pretty;

public class InterferenceForeachBad extends DPJTestCase {
    
    public InterferenceForeachBad() {
	super("InterferenceForeachBad");
    }
    
    @Test public void testStackIntererenceInForeach() throws Throwable {
	compileExpectingWarnings("StackInterferenceNestedForeach", 1);
    }
    
    @Test public void testCommutativeMethod() throws Throwable {
	compileExpectingWarnings("CommutativeMethod", 1);
    }
    
    @Test public void testScopedLocalEffects() throws Throwable {
	compileExpectingWarnings("ScopedLocalRegions", 1);
    }

}
