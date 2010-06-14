/**
 * These tests verify that the compiler correctly catches interfering effects.
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;

import com.sun.tools.javac.tree.Pretty;

public class InterferenceCobeginBad extends DPJTestCase {
    
    private  void doTest(String name) throws Throwable {
	analyzeExpectingWarnings(1, parse("InterferenceCobeginBad/" + name + ".java"));
    }
    
    @Test public void testOverlappingWrites() throws Throwable {
	doTest("OverlappingWrites");
    }
    
    @Test public void testOverlappingReadWrite() throws Throwable {
	doTest("OverlappingReadWrite");
    }
    
    @Test public void testOverlappingInvoke() throws Throwable {
	doTest("OverlappingInvoke");
    }
    
    @Test public void testInterferingStackVariables() throws Throwable {
	doTest("StackVariableOutside");
    }
    
    @Test public void testStackIntererenceInForeach() throws Throwable {
	doTest("StackInterferenceInForeach");
    }
    
    @Test public void testLocalRegions() throws Throwable {
	doTest("LocalRegions");
    }
}
