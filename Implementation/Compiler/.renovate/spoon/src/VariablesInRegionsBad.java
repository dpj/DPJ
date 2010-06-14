/**
 * These tests verify that errors related to 'in' declarations are being caught
 *  
 * @author Rob Bocchino
 */

import org.junit.Test;

import com.sun.tools.javac.tree.Pretty;


public class VariablesInRegionsBad extends DPJTestCase {
    
    private  void errorTest(String name) throws Throwable {
	analyzeExpectingError(Pretty.NONE, parse("VariablesInRegionsBad/" + name + ".java"));
    }

    @Test public void testLocalVariableDeclaredInRegion() throws Throwable {
	errorTest("LocalVariableDeclaredInRegion");
    }
    
    @Test public void testMethodFormalDeclaredInRegion() throws Throwable {
	errorTest("MethodFormalDeclaredInRegion");
    }
    
    @Test public void testBadFieldRegion() throws Throwable {
	errorTest("BadFieldRegion");
    }

}
