/**
 * These tests verify that errors involving class instantiation are being
 * caught
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class ClassInstantiationBad extends DPJTestCase {
    
    public ClassInstantiationBad() {
	super("ClassInstantiationBad");
    }
    
    @Test public void testTooManyParams() throws Throwable {
	compileExpectingErrors("TooManyParams", 1);
    }
    
    @Test public void testDisjoint() throws Throwable {
	compileExpectingWarnings("Disjoint", 1);
    }
    
    @Test public void testEffectConstraintNotSatisfied() throws Throwable {
	compileExpectingWarnings("EffectConstraintNotSatisfied", 1);
    }
}
