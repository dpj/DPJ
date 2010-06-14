/**
 * These tests verify that correct cases of variables declared in
 * regions are working
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class VariablesInRegionsGood extends DPJTestCase {
    
    public VariablesInRegionsGood() {
	super("VariablesInRegionsGood");
    }
    
    @Test public void testFieldRegions() throws Throwable {
	compile("FieldRegions");
    }

    @Test public void testRegionSelection() throws Throwable {
	compile("RegionSelection");
    }

}
