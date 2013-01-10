import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;

import javax.tools.JavaFileManager;

import junit.framework.TestCase;

import org.junit.Test;

import com.sun.tools.javac.code.Source;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.jvm.Target;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.main.OptionName;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.Pretty;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.JavacFileManager;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Options;
import com.sun.tools.javac.util.Pair;

/**
 * Base class for JUnit test cases for Deterministic Parallel Java.
 * 
 * IMPLEMENTATION NOTE: This class creates a new Context and a new compiler instance for
 * every single test.  While this is a bit inefficient, the extra initialization work
 * is minimal, and it lets us avoid having any persistent state in the DPJTestCase object
 * itself (for instance, a Context field storing a single Context used by all tests).
 * Unfortunately, such persistent state causes a memory leak, because JUnit apparently
 * does not destroy TestCase objects until all the tests are run.  The memory leak was
 * causing out of memory errors as the number of test cases got large.
 * 
 * @author Jeff Overbey
 * @author Rob Bocchino
 */
public abstract class DPJTestCase extends TestCase {
    
    /**
     * Directory containing this test case
     */
    protected final String dirname;
    
    /* CONSTRUCTOR */
    
    DPJTestCase(String dirname) {
	this.dirname = dirname;
    }
    
    /* API METHODS */

    /**
     * Compile a file.  Compilation consists of parsing and analysis.  We can specify
     * the filename to compile, the number of expected errors, and the number of 
     * expected warnings.
     * 
     * @param filename
     * @param nerrors
     * @param nwarnings
     * @return
     * @throws Throwable
     */
    protected List<Pair<Env<AttrContext>, JCClassDecl>> 
	compile(String filename, int nerrors, int nwarnings) throws Throwable {
	Context context = getContext();
	JCCompilationUnit ast = parse(dirname + "/" + filename + ".java", context);
	return analyze(context, ast, nerrors, nwarnings);
    }
    
    /**
     * Overloaded interface to compile: nerrors = 0, nwarnings = 0
     * @param filename
     * @return
     * @throws Throwable
     */
    protected List<Pair<Env<AttrContext>, JCClassDecl>> 
    	compile(String filename) throws Throwable {
	return compile(filename, 0, 0);
    }
    
    /**
     * Overloaded interface to compile: nwarnings = 0
     * @param filename
     * @return
     * @throws Throwable
     */
    protected List<Pair<Env<AttrContext>, JCClassDecl>> 
	compileExpectingErrors(String filename, int nerrors) throws Throwable {
	return compile(filename, nerrors, 0);
    }
    
    /**
     * Overloaded interface to compile: nerrors = 0
     * @param nwarnings
     * @param filename
     * @return
     * @throws Throwable
     */
    protected List<Pair<Env<AttrContext>, JCClassDecl>> 
	compileExpectingWarnings(String filename, int nwarnings) throws Throwable {
	return compile(filename, 0, nwarnings);
    }

    /**
     * Compare given string with expected file
     * @param actual
     * 
     * @throws Throwable
     */
    protected void compareWithExpected(String actual, 
	    String expectedName) throws Throwable {
	ByteArrayOutputStream expected = new ByteArrayOutputStream();
	PrintWriter p = new PrintWriter(expected);
	BufferedReader r = new BufferedReader(new FileReader(loadFile(expectedName)));
	for (String line = r.readLine(); line != null; line = r.readLine()) {
	    p.print(line);
	    p.println();
	}
	p.flush();
	expected.close();
	
	assertEquals(expected.toString().trim(), actual.trim());
    }

    /* PRIVATE HELPER FUNCTIONS */
    
    /**
     * Load a file from disk
     * @param	filename
     */
    private File loadFile(String filename) {
	String curdir = new File(".").getAbsolutePath();
	String target = "Compiler" + System.getProperty("file.separator");
	int index = curdir.lastIndexOf(target);
	if (index < 0) {
	    throw new Error("Cannot locate directory " + target + "test/dpj-programs");
	}
	int len = index + target.length();
	String dir = curdir.substring(0, len) + "test/dpj-programs/";
	return new File(dir + filename);	
    }

    /**
     * Make a new context
     * @return
     */
    private Context getContext() {
	Context context = new Context();
	JavacFileManager.preRegister(context); 
	// Force Java 1.5 parsing and code generation
	Options.instance(context).put(OptionName.SOURCE, Source.JDK1_5.name);
	Options.instance(context).put(OptionName.TARGET, Target.JDK1_5.name);
	return context;
    }

    /**
     * Parse a file
     */
    private JCCompilationUnit parse(String filename, Context context) {
	JavaCompiler comp = JavaCompiler.instance(context);
	JavacFileManager fileMgr =
	    (JavacFileManager)context.get(JavaFileManager.class);
	JCCompilationUnit ast =
	    comp.parse(fileMgr.getRegularFile(loadFile(filename)));
	assertNotNull(ast);
	return ast;	
    }

    /**
     * Analyze a file. Analysis consists of attribution, effect checking, 
     * flow analysis, and desugaring.
     * @param context
     * @param ast
     * @param nerrors
     * @param nwarnings
     * @return
     * @throws Throwable
     */
    private List<Pair<Env<AttrContext>, JCClassDecl>> analyze(
	    Context context, JCCompilationUnit ast, 
	    int nerrors, int nwarnings) throws Throwable {
	JavaCompiler comp = JavaCompiler.instance(context);
	comp.eraseDPJ = false; // Keep DPJ annotations so we can ensure they're there
	comp.enterTrees(List.of(ast));
	comp.suppressErasure = true; // Turn off erasure of generics and regions
	List<Pair<Env<AttrContext>, JCClassDecl>> result =
	    comp.desugar(comp.flow(comp.checkEffects(comp.attribute(comp.todo))));

	context.get(JavaFileManager.class).close();
	assertEquals(nerrors, Log.instance(context).nerrors);
	assertEquals(nwarnings, Log.instance(context).nwarnings);
	return result;
    }
    

}
