/**
 * Test for catching errors in effect checking
 */

import org.junit.Test;


public class EffectsBad extends DPJTestCase {

    public EffectsBad() {
	super("EffectsBad");
    }
    	
    @Test
    public void testFieldAccess() throws Throwable {
	compileExpectingErrors("FieldAccess", 1);
    }

    @Test
    public void testFieldAssign() throws Throwable {
	compileExpectingErrors("FieldAssign", 1);
    }
    
    @Test
    public void testUseBeforeDecl() throws Throwable {
	compileExpectingErrors("UseBeforeDecl", 1);
    }

    @Test
    public void testUseAfterDecl() throws Throwable {
	compileExpectingErrors("UseAfterDecl", 1);
    }

    @Test
    public void testReadNestedRegionParams() throws Throwable {
	compileExpectingErrors("ReadNestedRegionParams", 1);
    }

    @Test
    public void testIfEffects() throws Throwable {
	compileExpectingErrors("IfEffects", 1);
    }
    
    @Test
    public void testBadOverridingEffects() throws Throwable {
	compileExpectingErrors("BadOverridingEffects", 1);
    }
    
    @Test
    public void testConstructorEffects() throws Throwable {
	compileExpectingErrors("ConstructorEffects", 1);
    }

    @Test
    public void testCoarseningNeeded() throws Throwable {
	compileExpectingErrors("CoarseningNeeded", 1);
    }
        
    @Test
    public void testEffectFromDifferentClass() throws Throwable {
	compileExpectingErrors("EffectFromDifferentClass", 1);
    }
    
    
}
