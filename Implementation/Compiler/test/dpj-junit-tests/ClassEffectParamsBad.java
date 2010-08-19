/**
 * These tests verify that errors involving class effect params 
 * are being caught
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class ClassEffectParamsBad extends DPJTestCase {
    
    public ClassEffectParamsBad() {
	super("ClassEffectParamsBad");
    }
 
    @Test
    public void testUndefinedParam() throws Throwable {
	compileExpectingErrors("UndefinedParam", 1);
    }
    
}
