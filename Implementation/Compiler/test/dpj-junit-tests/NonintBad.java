/**
 * These tests verify that errors involving nonint statements and effects
 * are being caught.
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;

public class NonintBad extends DPJTestCase {
    
    public NonintBad() {
	super("NonintBad");
    }
    
    @Test public void testAtomicDoesntCoverNonint() throws Throwable {
	compileExpectingErrors("AtomicDoesntCoverNonint", 1);
    }
    
    @Test public void testUnqualifiedDoesntCoverNonint() throws Throwable {
	compileExpectingErrors("UnqualifiedDoesntCoverNonint", 1);
    }

    @Test public void testNonintInAtomicBad() throws Throwable {
	compileExpectingErrors("NonintInAtomicBad", 1);
    }

    @Test public void testCompareNonintAndUnqualifiedBad() throws Throwable {
	compileExpectingWarnings("CompareNonintAndUnqualifiedBad", 1);
    }

    @Test public void testCompareTwoNonintBad() throws Throwable {
	compileExpectingWarnings("CompareTwoNonintBad", 1);
    }
    
}
