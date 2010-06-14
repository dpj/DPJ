/**
 * These tests verify that method region params are working in correct
 * cases.
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;

import com.sun.tools.javac.tree.Pretty;

public class MethodParamsGood extends DPJTestCase {
    
    private  void doTest(String name) throws Throwable {
	analyze(Pretty.NONE, parse("MethodParamsGood/" + name + ".java"));
    }
    
    @Test public void testOneExplicit() throws Throwable {
	doTest("OneExplicit");
    }

    @Test public void testOneInferred() throws Throwable {
	doTest("OneInferred");
    }

    @Test public void testTwoExplicit() throws Throwable {
	doTest("TwoExplicit");
    }

    @Test public void testTwoDisjointExplicit() throws Throwable {
	doTest("TwoDisjointExplicit");
    }
    
    @Test public void testTwoInferred() throws Throwable {
	doTest("TwoInferred");
    }

    @Test public void testOneWithTypeExplicit() throws Throwable {
	doTest("OneWithTypeExplicit");
    }
    
    @Test public void testOneWithTypeInferred() throws Throwable {
	doTest("OneWithTypeInferred");
    }

}
