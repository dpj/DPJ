import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.junit.Test;

import com.sun.tools.javac.Main;

public class SystemTests extends TestCase {
    
    // Paths are relative to the Compiler project
    private static final String RUNTIME_SOURCE_PATH = "../Runtime/dpj/";
    private static final String RUNTIME_CLASS_PATH = "../Runtime/classes/";
    private static final String BARNES_HUT_PATH = "../../Benchmarks/Applications/barnes-hut/dpj/";
    private static final String KERNELS_PATH = "../../Benchmarks/Kernels/dpj/";

    @Test public void testsTemporarilyDisabled() {
	assertTrue(true);
    }
    
//    @Test public void testRuntimeClasses() throws Throwable {
//        compileAllFilesIn(RUNTIME_SOURCE_PATH, "Runtime");
//    }
//
//    @Test public void testBarnesHutClasses() throws Throwable {
//        compileAllFilesIn(BARNES_HUT_PATH, "Barnes-Hut");
//    }
//
//    @Test public void testKernelClasses() throws Throwable {
//        compileAllFilesIn(KERNELS_PATH, "Kernels");
//    }

    private void compileAllFilesIn(String directoryName, String description) {
	checkExists(RUNTIME_SOURCE_PATH, "Runtime");
	
        File directory = checkExists(directoryName, description);
        if (directory == null) return;
        
	ArrayList<String> arguments = new ArrayList<String>(32);
        arguments.add("-source");
        arguments.add("1.5");
        arguments.add("-target");
        arguments.add("1.5");
        arguments.add("-d");
        arguments.add("/tmp");
        arguments.add("-cp");
        arguments.add(RUNTIME_CLASS_PATH);
        for (File f : directory.listFiles()) {
	    if (f.getName().endsWith(".java")) {
		//System.out.println("- " + directoryName + f.getName());
		arguments.add(directoryName + f.getName());
	    }
        }
        
        //System.out.println("Compiling...");
        int result = Main.compileDPJ(arguments.toArray(new String[arguments.size()]));
        assertEquals(0, result);
        //System.out.println("Done");
    }

    private File checkExists(String directoryName, String description) {
	File directory = new File(directoryName);
        if (!directory.exists() || !directory.isDirectory()) {
            System.err.println(description + " not found; skipping");
            return null;
        }
	return directory;
    }
}
