/**
 * These tests test effect checking for noninterfering cobegin
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;

public class InterferenceCobeginGood extends DPJTestCase {
    
    public InterferenceCobeginGood() {
	super("InterferenceCobeginGood");
    }
    
    @Test public void testStackRegions() throws Throwable {
	compile("StackRegions");
    }
    
    @Test public void testLocalRegions() throws Throwable {
	compile("LocalRegions");
    }
    
}
