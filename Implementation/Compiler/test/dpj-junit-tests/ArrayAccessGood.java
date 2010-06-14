/**
 * These tests verify that correct array access cases are working
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class ArrayAccessGood extends DPJTestCase {
    
    public ArrayAccessGood() {
	super("ArrayAccessGood");
    }
    
    @Test public void testIndexTypeAssign() throws Throwable {
	compile("IndexTypeAssign");
    }
    
    @Test public void testParamAsIndex() throws Throwable {
	compile("ParamAsIndex");
    }
}
