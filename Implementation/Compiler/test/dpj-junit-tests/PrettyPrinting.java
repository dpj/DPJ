import java.io.File;

import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.Pretty;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.util.Pair;


import org.junit.Test;

/**
 * Test construction and pretty printing of the AST.
 * 
 * @author Jeff Overbey
 */
public class PrettyPrinting extends DPJTestCase {
    
    public PrettyPrinting() {
	super("PrettyPrinting");
    }
    
    private void prettyCompare(String name) throws Throwable {
	StringBuilder actual = new StringBuilder();
	String expectedName = dirname + "/" + name + ".java.expected";
	for (Pair<Env<AttrContext>, JCClassDecl> pair : 
	    compile(name)) {
	    JCTree.codeGenMode = Pretty.NONE;
	    actual.append(pair.snd);
	}
	compareWithExpected(actual.toString(), expectedName);
    }

    @Test public void testDecls() throws Throwable {
	prettyCompare("FieldRegionDecls");
    }
    
    @Test public void testLocalRegions() throws Throwable {
	prettyCompare("LocalRegionDecls");
    }
    
    @Test public void testFields() throws Throwable {
	prettyCompare("FieldsInRegions");
    }
    
    @Test public void testMethodEffects() throws Throwable {
	prettyCompare("MethodEffectAnnotations");
    }
    
    @Test public void testClassRegionParams() throws Throwable {
	prettyCompare("ClassRegionParams");
    }
    
    @Test public void testMethodRegionParams() throws Throwable {
	prettyCompare("MethodRegionParams");
    }

    @Test public void testArrayRegions() throws Throwable {
	prettyCompare("ArrayRegions");
    }

    @Test public void testSpawn() throws Throwable {
	prettyCompare("Spawn");
    }

    @Test public void testFinish() throws Throwable {
	prettyCompare("Finish");
    }

    @Test public void testForeach() throws Throwable {
	prettyCompare("Foreach");
    }
    
    @Test public void testCobegin() throws Throwable {
	prettyCompare("Cobegin");
    }
    
}
