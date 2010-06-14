/**
 * These tests verify that errors related to 'in' declarations are being caught
 *  
 * @author Rob Bocchino
 */

import org.junit.Test;



public class VariablesInRegionsBad extends DPJTestCase {
    
    public VariablesInRegionsBad() {
	super("VariablesInRegionsBad");
    }

    @Test public void testLocalVariableDeclaredInRegion() throws Throwable {
	compileExpectingErrors("LocalVariableDeclaredInRegion", 1);
    }
    
    @Test public void testMethodFormalDeclaredInRegion() throws Throwable {
	compileExpectingErrors("MethodFormalDeclaredInRegion", 1);
    }
    
    @Test public void testBadFieldRegion() throws Throwable {
	compileExpectingErrors("BadFieldRegion", 1);
    }

}
