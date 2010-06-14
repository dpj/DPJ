/**
 * These tests verify that nonint statements and effects are working
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;

public class NonintGood extends DPJTestCase {
    
    public NonintGood() {
	super("NonintGood");
    }
    
    @Test public void testNonintStatement() throws Throwable {
	compile("NonintStatement");
    }
    
    @Test public void testNonintSubeffects() throws Throwable {
	compile("NonintSubeffects");
    }
    
    @Test public void testNonintInAtomic() throws Throwable {
	compile("NonintInAtomic");
    }

    @Test public void testAtomicInNonint() throws Throwable {
	compile("AtomicInNonint");
    }
    
    @Test public void testCompareNonintAndUnqualified() throws Throwable {
	compile("CompareNonintAndUnqualified");
    }
    
    @Test public void testCompareTwoNonint() throws Throwable {
	compile("CompareTwoNonint");
    }

}
