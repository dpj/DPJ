/**
 * These tests check the construction of arrays using 'new'
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;

public class NewArraysGood extends DPJTestCase {
    
    public NewArraysGood() {
	super("NewArraysGood");
    }
    
    @Test public void testOneDim() throws Throwable {
	compile("OneDim");
    }
    
    @Test public void testTwoDimOneSize() throws Throwable {
	compile("TwoDimOneSize");
    }

    @Test public void testTwoDimTwoSize() throws Throwable {
	compile("TwoDimTwoSize");
    }

}
