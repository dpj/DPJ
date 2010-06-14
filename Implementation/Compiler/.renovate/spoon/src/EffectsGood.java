import org.junit.Test;

import com.sun.tools.javac.tree.Pretty;

public class EffectsGood extends DPJTestCase {

    private void correctTest(String name) throws Throwable {
	analyze(Pretty.NONE, parse("EffectsGood/" + name + ".java"));
    }
    
    @Test
    public void testInheritedRPL() throws Throwable {
	correctTest("InheritedRPL");
    }

    @Test
    public void testFlatCallChain() throws Throwable {
	correctTest("FlatCallChain");
    }

    @Test
    public void testZRegionFieldAccess() throws Throwable {
	correctTest("ZRegionFieldAccess");
    }

    @Test
    public void testInvokeBeforeDecl() throws Throwable {
	correctTest("InvokeBeforeDecl");
    }
    
    @Test
    public void testConstructorEffects() throws Throwable {
	correctTest("ConstructorEffects");
    }
    
    @Test public void testInheritedField() throws Throwable {
	correctTest("InheritedField");
    }
    
}
