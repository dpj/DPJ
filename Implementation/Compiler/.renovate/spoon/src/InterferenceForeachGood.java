/**
 * These tests check effect checking for noninterfering foreach
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;
import com.sun.tools.javac.tree.Pretty;

public class InterferenceForeachGood extends DPJTestCase {
    
    private  void doTest(String name) throws Throwable {
	analyze(Pretty.NONE, parse("InterferenceForeachGood/" + name + ".java"));
    }
    
    @Test public void testCommutativeMethod() throws Throwable {
	doTest("CommutativeMethod");
    }
    
}
