/**
 * These tests verify that errors involving class instantiation are being
 * caught
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;

import com.sun.tools.javac.tree.Pretty;

public class ClassInstantiationBad extends DPJTestCase {
    
    private  void errorTest(String name) throws Throwable {
	analyzeExpectingError(Pretty.NONE, parse("ClassInstantiationBad/" + name + ".java"));
    }
    
    private void warningTest(String name) throws Throwable {
	analyzeExpectingWarnings(1, parse("ClassInstantiationBad/" + name + ".java"));	
    }
    
    @Test public void testTooManyParams() throws Throwable {
	errorTest("TooManyParams");
    }
    
    @Test public void testDisjoint() throws Throwable {
	warningTest("Disjoint");
    }
}
