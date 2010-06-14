/**
 * These tests verify that correct class instantiation is working
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;

import com.sun.tools.javac.tree.Pretty;

public class ClassInstantiationGood extends DPJTestCase {
    
    private  void correctTest(String name) throws Throwable {
	analyze(Pretty.NONE, parse("ClassInstantiationGood/" + name + ".java"));
    }
    
    @Test public void testDefaultClassParam() throws Throwable {
	correctTest("DefaultParam");
    }
    
    @Test public void testDisjointParams() throws Throwable {
	correctTest("DisjointParams");
    }
    
    @Test public void testDisjointParamRPL() throws Throwable {
	correctTest("DisjointParamRPL");
    }
    
    @Test public void testInner() throws Throwable {
	correctTest("Inner");
    }
    
    @Test public void testSyntax() throws Throwable {
	correctTest("Syntax");
    }
            
}
