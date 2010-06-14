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
 * <p>
 * Subclasses may call {@link #parse(String)} to parse a DPJ program stored in
 * the test/dpj-programs directory and {@link #analyze(JCCompilationUnit)} to
 * perform attribution (symbol table construction and type checking), flow
 * analysis, and desugaring.
 * 
 * @author Jeff Overbey
 */
// Code based on Main#compile and JavaCompiler#compile
public abstract class DPJTestCase extends TestCase {
    protected Context context;
    
    protected JCCompilationUnit parse(String filename) {
	context = new Context();
	JavacFileManager.preRegister(context);
	
	// Force Java 1.5 parsing and code generation
	Options.instance(context).put(OptionName.SOURCE, Source.JDK1_5.name);
	Options.instance(context).put(OptionName.TARGET, Target.JDK1_5.name);
	
	JavaCompiler comp = JavaCompiler.instance(context);
	JavacFileManager fileMgr =
	    (JavacFileManager)context.get(JavaFileManager.class);
	JCCompilationUnit ast =
	    comp.parse(fileMgr.getRegularFile(loadFile(filename)));
	assertNotNull(ast);
	return ast;
    }

    protected File loadFile(String filename) {
	String curdir = new File(".").getAbsolutePath();
	String target = "Compiler/";
	int index = curdir.lastIndexOf(target);
	if (index < 0)
	    throw new Error("Cannot locate directory " + target + "test/dpj-programs");
	int len = index + target.length();
	String dir = curdir.substring(0, len) + "test/dpj-programs/";
	return new File(dir + filename);
    }

    protected List<Pair<Env<AttrContext>, JCClassDecl>> analyze(
	    int codeGenMode, JCCompilationUnit ast) throws Throwable {
	return analyze(codeGenMode, ast, false, 0);
    }

    protected List<Pair<Env<AttrContext>, JCClassDecl>> analyzeExpectingError(
	    int codeGenMode, JCCompilationUnit ast) throws Throwable {
	return analyze(codeGenMode, ast, true, 0);
    }

    protected List<Pair<Env<AttrContext>, JCClassDecl>> analyzeExpectingWarnings(
	    int warnings, JCCompilationUnit ast) throws Throwable {
	return analyze(Pretty.NONE, ast, false, warnings);
    }

//    protected abstract List<Pair<Env<AttrContext>, JCClassDecl>> analyze(
//	    JCCompilationUnit ast, boolean expectErrors) throws Throwable;
    private List<Pair<Env<AttrContext>, JCClassDecl>> analyze(
	    int codeGenMode, JCCompilationUnit ast, boolean expectErrors, int expectWarnings) throws Throwable {
	assertNotNull(context); // Must call #parse first
	
	JavaCompiler comp = JavaCompiler.instance(context);
	comp.eraseDPJ = false; // Keep DPJ annotations so we can ensure they're there
	comp.enterTrees(List.of(ast));
	comp.totallyBogusFlag = true; // Turn off erasure of generics and regions
	List<Pair<Env<AttrContext>, JCClassDecl>> result =
	    comp.desugar(comp.flow(comp.checkEffects(comp.attribute(comp.todo))));
	assertNotNull(result);
	if (!expectErrors) assertFalse(result.isEmpty());

	context.get(JavaFileManager.class).close();
	assertEquals(expectErrors, Log.instance(context).nerrors != 0);
	if (expectWarnings > 0) assertEquals(expectWarnings, Log.instance(context).nwarnings); // Don't run unless we're specifically checking the typechecker
	return result;
    }

//    abstract protected void compareWithExpected(String filename, String expectedName) throws Throwable;
    protected void compareWithExpected(int codeGenMode,
	                               String filename,
	                               String expectedName) throws Throwable {
	StringBuilder actual = new StringBuilder();
	for (Pair<Env<AttrContext>, JCClassDecl> pair : analyze(codeGenMode, parse(filename))) {
	    JCTree.codeGenMode = codeGenMode;
	    actual.append(pair.snd);
	}
	
	ByteArrayOutputStream expected = new ByteArrayOutputStream();
	PrintWriter p = new PrintWriter(expected);
	BufferedReader r = new BufferedReader(new FileReader(loadFile(expectedName)));
	for (String line = r.readLine(); line != null; line = r.readLine()) {
	    p.print(line);
	    p.println();
	}
	p.flush();
	expected.close();
	
	assertEquals(expected.toString().trim(), actual.toString().trim());
    }

    protected void compareWithExpected(int codeGenMode, String filename) throws Throwable {
	compareWithExpected(codeGenMode, filename, filename + ".expected");
    }
    
}
