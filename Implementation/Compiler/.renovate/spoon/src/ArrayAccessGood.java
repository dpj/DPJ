/**
 * These tests verify that correct array access cases are working
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;

import com.sun.tools.javac.tree.Pretty;

public class ArrayAccessGood extends DPJTestCase {
    
    private  void doTest(String name) throws Throwable {
	analyze(Pretty.NONE, parse("ArrayAccessGood/" + name + ".java"));
    }
    @Test public void testIndexTypeAssign() throws Throwable {
	doTest("IndexTypeAssign");
    }
}
