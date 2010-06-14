/**
 * These tests check the handling of valid array types
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class ArrayTypesGood extends DPJTestCase {
    
    public ArrayTypesGood() {
	super("ArrayTypesGood");
    }
    
    @Test public void testOneDim() throws Throwable {
	compile("OneDim");
    }
    
    @Test public void testTwoDim() throws Throwable {
	compile("TwoDim");
    }

}
