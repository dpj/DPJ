/**
 * These tests verify that method region params are working in correct
 * cases.
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class MethodParamsGood extends DPJTestCase {
    
    public MethodParamsGood() {
	super("MethodParamsGood");
    }

    @Test public void testOneExplicit() throws Throwable {
	compile("OneExplicit");
    }

    @Test public void testOneInferred() throws Throwable {
	compile("OneInferred");
    }

    @Test public void testTwoExplicit() throws Throwable {
	compile("TwoExplicit");
    }

    @Test public void testTwoDisjointExplicit() throws Throwable {
	compile("TwoDisjointExplicit");
    }
    
    @Test public void testTwoInferred() throws Throwable {
	compile("TwoInferred");
    }

    @Test public void testOneWithTypeExplicit() throws Throwable {
	compile("OneWithTypeExplicit");
    }
    
    @Test public void testOneWithTypeInferred() throws Throwable {
	compile("OneWithTypeInferred");
    }

    @Test public void testTypeParams() throws Throwable {
	compile("TypeParams");
    }
    
    @Test public void testDisjointStar() throws Throwable {
	compile("DisjointStar");
    }

}
