/**
 * These tests verify that type region parameters are working in correct
 * cases.
 */

import org.junit.Test;

public class TypeRegionParamsGood extends DPJTestCase {
    
    public TypeRegionParamsGood() {
	super("TypeRegionParamsGood");
    }
    
    @Test public void testDefinition() throws Throwable {
	compile("Definition");
    }
    
    @Test public void testUse() throws Throwable {
	compile("Use");
    }
    
    @Test public void testParamSubtype() throws Throwable {
	compile("ParamSubtype");
    }
    
    @Test public void testDefault() throws Throwable {
	compile("Default");
    }
    
    @Test public void testClassSubtype() throws Throwable {
	compile("ClassSubtype");
    }
    
    @Test public void testClassSubstThroughFieldAccess() throws Throwable {
	compile("ClassSubstThroughFieldAccess");
    }
    
    @Test public void testParamSubstThroughFieldAccess() throws Throwable {
	compile("ParamSubstThroughFieldAccess");
    }
    
    @Test public void testRegionResolutionThroughSelect() throws Throwable {
	compile("RegionResolutionThroughSelect");
    }
    
    @Test public void testMethod() throws Throwable {
	compile("Method");
    }
    
    @Test public void testMethodInvocation() throws Throwable {
	compile("MethodInvocation");
    }
}
