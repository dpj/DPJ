/**
 * These tests verify that correct field access cases are working
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;

import com.sun.tools.javac.tree.Pretty;

public class FieldAccessGood extends DPJTestCase {
    
    private  void correctTest(String name) throws Throwable {
	analyze(Pretty.NONE, parse("FieldAccessGood/" + name + ".java"));
    }
    
    @Test public void testIndexTypeAssign() throws Throwable {
	correctTest("ZRegion");
    }
}
