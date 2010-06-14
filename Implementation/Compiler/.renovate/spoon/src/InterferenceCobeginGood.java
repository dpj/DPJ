/**
 * These tests test effect checking for noninterfering cobegin
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;
import com.sun.tools.javac.tree.Pretty;

public class InterferenceCobeginGood extends DPJTestCase {
    
    private  void doTest(String name) throws Throwable {
	analyze(Pretty.NONE, parse("InterferenceCobeginGood/" + name + ".java"));
    }
    
    @Test public void testStackRegions() throws Throwable {
	doTest("StackRegions");
    }
    
    @Test public void testLocalRegions() throws Throwable {
	doTest("LocalRegions");
    }
    
}
