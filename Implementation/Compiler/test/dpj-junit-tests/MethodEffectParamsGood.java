/**
 * These tests verify that method effect params are working in correct
 * cases.
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;

public class MethodEffectParamsGood extends DPJTestCase {

    public MethodEffectParamsGood() {
	super("MethodEffectParamsGood");
    }

    @Test public void testUseInMethodEffect() throws Throwable {
	compile("UseInMethodEffect");
    }

    @Test public void testMethodEffectParam() throws Throwable {
	compile("EffectArgument");
    }

    @Test public void testClassAndMethodEffectParam() throws Throwable {
	compile("ClassAndMethodEffectParam");
    }
    
}