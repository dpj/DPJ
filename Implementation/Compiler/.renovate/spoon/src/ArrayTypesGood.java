/**
 * These tests check the handling of valid array types
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;

import com.sun.tools.javac.tree.Pretty;

public class ArrayTypesGood extends DPJTestCase {
    
    private  void doTest(String name) throws Throwable {
	analyze(Pretty.NONE, parse("ArrayTypesGood/" + name + ".java"));
    }
    
    @Test public void testOneDim() throws Throwable {
	doTest("OneDim");
    }
    
    @Test public void testTwoDim() throws Throwable {
	doTest("TwoDim");
    }

}
