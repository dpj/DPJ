/**
 * These tests verify that region resolution errors are being caught
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;



public class RegionResolutionBad extends DPJTestCase {
    
    public RegionResolutionBad() {
	super("RegionResolutionBad");
    }

    @Test public void testDuplicateRegions() throws Throwable {
	compileExpectingErrors("DuplicateFieldRegionDecls", 1);
    }

    @Test public void testBadRPLRoot() throws Throwable {
	compileExpectingErrors("BadRPLRoot", 1);
    }

    @Test public void testBadRPLParam() throws Throwable {
	compileExpectingErrors("BadRPLParam", 1);
    }
    
    @Test
    public void testNonFinalZRegion() throws Throwable {
	compileExpectingErrors("NonFinalZRegion", 1);
    }
    
}
