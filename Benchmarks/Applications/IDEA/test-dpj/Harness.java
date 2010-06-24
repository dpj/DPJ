/**
 * A harness for running DPJ kernel programs.  Supports three kinds of runs:
 *
 * 1. TEST: A user-defined test of correctness.
 * 2. IDEAL: Run the code, time the tasks, and measure ideal
 *    speedup on infinite processors (i.e., critical path length).
 * 3. TIME: Run the code and output the time of the work portion.
 * 4. TASK_GRAPH: Run the code, build a task graph, and print it out
 *    in deteval format.
 *
 * The default command-line parameters are MODE, specifying the kind
 * of run (shown above) and SIZE (specifying the problem size).
 * Additional command-line arguments can be defined by the user.
 *
 * The basic way to use this harness it to subclass it, provide a new
 * constructor, and override its abstract methods.  See the kernels in
 * this directory for examples.
 *
 * @author Robert L. Bocchino Jr.
 * August 2008
 */

import DPJRuntime.*;

public abstract class Harness {
    /**
     * The name of the program
     */
    protected String progName;

    /**
     * The command-line arguments as passed into Java
     */
    protected String[] args;

    /**
     * The mode of the run (TEST, IDEAL, TIME, or TASK_GRAPH)
     */
    protected String mode;

    /**
     * The problem size
     */
    protected int size;

    /**
     * Make a new harness and do some default initialization.
     * Ordinarily your constructor should call this one via "super".
     */
    public Harness(String progName, String[] args, int min, int max) {
	this.progName = progName;
	if (args.length < min || args.length > max) {
	    usage();
	    System.exit(1);
	}
	this.args = args;
	this.mode = args[0];
	this.size = Integer.parseInt(args[1]);
    }

    /**
     * Default case of 2 arguments:  size and mode
     */
    public Harness(String progName, String[] args) {
	this(progName, args, 2, 2);
    }
    
    /**
     * Default usage message.  You can override this to provide something different.
     */
    public void usage() {
	System.err.println("Usage:  java " + progName + ".java [mode] [size]");
	System.err.println("mode = TEST, IDEAL, TIME, TASK_GRAPH");
	System.err.println("size = problem size (int)");
    }

    /**
     * Override these abstract methods to implement your kernel.
     */
    public void initialize() {}
    public abstract void runTest();
    public abstract void runWork();
    public void runCleanup() {}

    /**
     * Call this to run the kernel.
     */
    public void run() {
	initialize();
	if (mode.equals("TEST")) {
	    runWork();
	    runCleanup();
	    runTest();
	} else if (mode.equals("IDEAL")) {
	    Instrument.start();
	    runWork();
	    Instrument.end();
	    runCleanup();
	    System.out.println("Ideal speedup: " + Instrument.idealSpeedup());
	    System.out.println("Amdahl bound: " + Instrument.amdahlBound());
	} else if (mode.equals("TIME")) {
	    long startTime, endTime;
	    startTime = System.nanoTime();
	    runWork();
	    endTime = System.nanoTime();
	    runCleanup();
	    System.out.println("Elapsed time: " + (endTime - startTime));
	} else {
	    usage();
	    System.exit(1);
	}
    }

}