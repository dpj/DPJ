/**
 * These tests check effect checking for noninterfering foreach
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;

public class InterferenceForeachGood extends DPJTestCase {

    public InterferenceForeachGood() {
	super("InterferenceForeachGood");
    }
    
    @Test public void testCommutativeMethod() throws Throwable {
	compile("CommutativeMethod");
    }
    
    @Test public void testScopedLocalEffects() throws Throwable {
	compile("ScopedLocalRegions");
    }
    
}
