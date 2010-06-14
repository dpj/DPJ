/**
 * These tests verify that correct cases of variables declared in
 * regions are working
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;

import com.sun.tools.javac.tree.Pretty;

public class VariablesInRegionsGood extends DPJTestCase {
    
    private  void correctTest(String name) throws Throwable {
	analyze(Pretty.NONE, parse("VariablesInRegionsGood/" + name + ".java"));
    }

    @Test public void testFieldRegions() throws Throwable {
	correctTest("FieldRegions");
    }

    @Test public void testRegionSelection() throws Throwable {
	correctTest("RegionSelection");
    }

}
