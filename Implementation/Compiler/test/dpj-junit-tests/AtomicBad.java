/**
 * These tests verify that errors involving atomic statements and effects
 * are being caught.
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;

public class AtomicBad extends DPJTestCase {
    
    public AtomicBad() {
	super("AtomicBad");
    }
    
    @Test public void testCobegin() throws Throwable {
	compileExpectingWarnings("AtomicCobeginBad", 1);
    }
    
    @Test public void testCobeginNDInvocationBad() throws Throwable {
	compileExpectingWarnings("CobeginNDInvocationBad", 1);
    }
    
    @Test public void testAtomicSubeffectsBad() throws Throwable {
	compileExpectingErrors("AtomicSubeffectsBad", 1);
    }
    
    @Test public void testNonatomicRegionBad() throws Throwable {
	compileExpectingWarnings("NonatomicRegionBad", 1);
    }
    
    @Test public void testAtomicParameterEffectsBad() throws Throwable {
	compileExpectingWarnings("AtomicParameterEffectsBad", 1);
    }
    
    @Test public void testClassRegionParamsBad() throws Throwable {
	compileExpectingErrors("ClassRegionParamsBad", 1);
    }
    
    @Test public void testMethodRegionParamsBad() throws Throwable {
	compileExpectingErrors("MethodRegionParamsBad", 1);
    }
    
}
