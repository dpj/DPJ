/**
 * These tests verify that errors involving type region params are
 * being caught.
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;

public class TypeRegionParamsBad extends DPJTestCase {
    
    public TypeRegionParamsBad() {
	super("TypeRegionParamsBad");
    }
 
    @Test public void testWrongNumberArgs() throws Throwable {
	compileExpectingErrors("WrongNumberArgs", 1);
    }
    
    @Test public void testParamSubtype() throws Throwable {
	compileExpectingErrors("ParamSubtype", 1);
    }
    
    @Test public void testClassSubtype() throws Throwable {
	compileExpectingErrors("ClassSubtype", 1);
    }

}
