/**
 * These tests check the construction of arrays using 'new'
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;
import com.sun.tools.javac.tree.Pretty;

public class NewArraysGood extends DPJTestCase {
    
    private  void doTest(String name) throws Throwable {
	analyze(Pretty.NONE, parse("NewArraysGood/" + name + ".java"));
    }
    
    @Test public void testOneDim() throws Throwable {
	doTest("OneDim");
    }
    
    @Test public void testTwoDimOneSize() throws Throwable {
	doTest("TwoDimOneSize");
    }

    @Test public void testTwoDimTwoSize() throws Throwable {
	doTest("TwoDimTwoSize");
    }

}
