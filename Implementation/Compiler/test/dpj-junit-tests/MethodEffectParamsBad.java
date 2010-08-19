/**
 * These tests verify that the compiler correctly catches problems with
 * effect params.
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;

import com.sun.tools.javac.tree.Pretty;

public class MethodEffectParamsBad extends DPJTestCase {
    
    public MethodEffectParamsBad() {
	super("MethodEffectParamsBad");
    }
     
    @Test public void testUseInMethodEffect() throws Throwable {
	compileExpectingErrors("UseInMethodEffect", 1);
    }

    @Test
    public void testMethodEffectParam() throws Throwable {
	compileExpectingErrors("EffectArgument", 1);
    }
    
    @Test
    public void testConstraintsNotSatisfied() throws Throwable {
	compileExpectingWarnings("ConstraintsNotSatisfied", 1);
    }

}
