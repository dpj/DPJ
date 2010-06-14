/**
 * Use this file if you want to run a single test, without other tests
 * mucking things up.
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class SingletonTest extends DPJTestCase {
    
    public SingletonTest() {
	super("SingletonTest");
    }
    
    @Test public void testSingletonTest() throws Throwable {
	compile("SingletonTest");
    }
    
}
