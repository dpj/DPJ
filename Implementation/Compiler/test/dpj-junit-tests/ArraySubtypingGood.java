/**
 * These tests verify that correct array subtyping cases are working
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class ArraySubtypingGood extends DPJTestCase {
    
    public ArraySubtypingGood() {
	super("ArraySubtypingGood");
    }
    
    @Test public void testOneDimEqualTypes() throws Throwable {
	compile("OneDimEqualTypes");
    }
    
    @Test public void testOneDimIncludedRPLs() throws Throwable {
	compile("OneDimIncludedRPLs");
    }

}
