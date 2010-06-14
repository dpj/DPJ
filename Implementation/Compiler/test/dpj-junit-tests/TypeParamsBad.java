/**
 * These tests verify that nothing strange is happening in regard to error
 * cases involving type params.
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class TypeParamsBad extends DPJTestCase {
    
    public TypeParamsBad() {
	super("TypeParamsBad");
    }
    
    @Test public void testMissingArg() throws Throwable {
	compileExpectingErrors("MissingArg", 1);
    }
    
}
