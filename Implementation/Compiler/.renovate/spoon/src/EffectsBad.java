/**
 * Test for catching errors in effect checking
 */

import org.junit.Test;

import com.sun.tools.javac.tree.Pretty;

public class EffectsBad extends DPJTestCase {

    private  void errorTest(String name) throws Throwable {
	analyzeExpectingError(Pretty.NONE, parse("EffectsBad/" + name + ".java"));
    }
    
    @Test
    public void testFieldAccess() throws Throwable {
	errorTest("FieldAccess");
    }

    @Test
    public void testFieldAssign() throws Throwable {
	errorTest("FieldAssign");
    }
    
    @Test
    public void testUseBeforeDecl() throws Throwable {
	errorTest("UseBeforeDecl");
    }

    @Test
    public void testUseAfterDecl() throws Throwable {
	errorTest("UseAfterDecl");
    }

    @Test
    public void testReadNestedRegionParams() throws Throwable {
	errorTest("ReadNestedRegionParams");
    }

    @Test
    public void testIfEffects() throws Throwable {
	errorTest("IfEffects");
    }
    
    @Test
    public void testBadOverridingEffects() throws Throwable {
	errorTest("BadOverridingEffects");
    }
    
    @Test
    public void testConstructorEffects() throws Throwable {
	errorTest("ConstructorEffects");
    }

}
