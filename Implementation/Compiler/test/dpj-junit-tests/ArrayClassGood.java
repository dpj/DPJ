/**
 * Tests for array classes
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class ArrayClassGood extends DPJTestCase {
    
    public ArrayClassGood() {
	super("ArrayClassGood");
    }
    
    @Test public void testArrayIntBasic() throws Throwable {
	compile("ArrayIntBasic");
    }

    @Test public void testArrayBasic() throws Throwable {
	compile("ArrayBasic");
    }
    
    @Test public void testArrayFieldRegion() throws Throwable {
	compile("ArrayFieldRegion");
    }
    
}
