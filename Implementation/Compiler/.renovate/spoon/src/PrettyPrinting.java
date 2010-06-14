import java.io.File;
import com.sun.tools.javac.tree.Pretty;


import org.junit.Test;

/**
 * Test construction and pretty printing of the AST.
 * 
 * @author Jeff Overbey
 */
public class PrettyPrinting extends DPJTestCase {
    private static final String DIR = "PrettyPrinting/";

    private void prettyCompare(String name) throws Throwable {
	compareWithExpected(Pretty.NONE, DIR + name, DIR + name +".expected");
    }

    @Test public void testDecls() throws Throwable {
	prettyCompare("FieldRegionDecls.java");
    }
    
    @Test public void testLocalRegions() throws Throwable {
	prettyCompare("LocalRegionDecls.java");
    }
    
    @Test public void testFields() throws Throwable {
	prettyCompare("FieldsInRegions.java");
    }
    
    @Test public void testMethodEffects() throws Throwable {
	prettyCompare("MethodEffectAnnotations.java");
    }
    
    @Test public void testClassRegionParams() throws Throwable {
	prettyCompare("ClassRegionParams.java");
    }
    
    @Test public void testMethodRegionParams() throws Throwable {
	prettyCompare("MethodRegionParams.java");
    }

    @Test public void testArrayRegions() throws Throwable {
	prettyCompare("ArrayRegions.java");
    }

    @Test public void testSpawn() throws Throwable {
	prettyCompare("Spawn.java");
    }

    @Test public void testFinish() throws Throwable {
	prettyCompare("Finish.java");
    }

    @Test public void testForeach() throws Throwable {
	prettyCompare("Foreach.java");
    }
    
    @Test public void testCobegin() throws Throwable {
	prettyCompare("Cobegin.java");
    }
    
}
