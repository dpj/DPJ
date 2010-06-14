/**
 * These tests verify that region resolution errors are being caught
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;

import com.sun.tools.javac.tree.Pretty;


public class RegionResolutionBad extends DPJTestCase {
    
    private  void errorTest(String name) throws Throwable {
	analyzeExpectingError(Pretty.NONE, parse("RegionResolutionBad/" + name + ".java"));
    }

    @Test public void testDuplicateRegions() throws Throwable {
	errorTest("DuplicateFieldRegionDecls");
    }

    @Test public void testBadRPLRoot() throws Throwable {
	errorTest("BadRPLRoot");
    }

    @Test public void testBadRPLParam() throws Throwable {
	errorTest("BadRPLParam");
    }
    
    
}
