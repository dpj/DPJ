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

    @Test public void testZRegion1() throws Throwable {
	compile("ZRegion1");
    }

    @Test public void testZRegion2() throws Throwable {
	compile("ZRegion2");
    }

}
