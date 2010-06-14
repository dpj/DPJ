/**
 * These tests verify that correct class instantiation is working
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class ClassInstantiationGood extends DPJTestCase {
    
    public ClassInstantiationGood() {
	super("ClassInstantiationGood");
    }
    
    @Test public void testDefaultClassParam() throws Throwable {
	compile("DefaultParam");
    }
    
    @Test public void testDisjointParams() throws Throwable {
	compile("DisjointParams");
    }
    
    @Test public void testDisjointParamRPL() throws Throwable {
	compile("DisjointParamRPL");
    }
    
    @Test public void testInner() throws Throwable {
	compile("Inner");
    }
    
    @Test public void testSyntax() throws Throwable {
	compile("Syntax");
    }
    
            
}
