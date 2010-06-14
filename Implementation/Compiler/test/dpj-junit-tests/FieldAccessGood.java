/**
 * These tests verify that correct field access cases are working
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class FieldAccessGood extends DPJTestCase {
    
    public FieldAccessGood() {
	super("FieldAccessGood");
    }
    
    @Test public void testIndexTypeAssign() throws Throwable {
	compile("ZRegion");
    }
}
