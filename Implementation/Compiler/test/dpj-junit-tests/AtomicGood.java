/**
 * These tests verify that atomic statements and effects are working
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;

public class AtomicGood extends DPJTestCase {
    
    public AtomicGood() {
	super("AtomicGood");
    }
    
    @Test public void testAtomicStatement() throws Throwable {
	compile("AtomicStatement");
    }
    
    @Test public void testForeachND() throws Throwable {
	compile("ForeachND");
    }
    
    @Test public void testCobeginND() throws Throwable {
	compile("CobeginND");
    }
    
    @Test public void testAtomicEffectsSimple() throws Throwable {
	compile("AtomicEffectsSimple");
    }
    
    @Test public void testAtomicEffectsInvocation() throws Throwable {
	compile("AtomicEffectsInvocation");
    }
    
    @Test public void testAtomicSubeffects() throws Throwable {
	compile("AtomicSubeffects");
    }
    
    @Test public void testInvocationAtomicEffects() throws Throwable {
	compile("InvocationAtomicEffects");
    }

    @Test public void testAtomicSubeffectsInvocation() throws Throwable {
	compile("AtomicSubeffectsInvocation");
    }
    
    @Test public void testAtomicParameterEffects() throws Throwable {
	compile("AtomicParameterEffects");
    }
}
