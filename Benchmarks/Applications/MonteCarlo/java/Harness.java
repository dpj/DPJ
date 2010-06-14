
import jsr166y.forkjoin.*;
import DPJRuntime.*;

public abstract class Harness {
    protected String progName;
    protected String[] args;
    protected String mode;
    protected int size;
    
    public Harness(String progName, String[] args, int min, int max) {
        super();
        this.progName = progName;
        if (args.length < min || args.length > max) {
            usage();
            System.exit(1);
        }
        this.args = args;
        this.mode = args[0];
        this.size = Integer.parseInt(args[1]);
    }
    
    public Harness(String progName, String[] args) {
        this(progName, args, 2, 2);
    }
    
    public void usage() {
        System.err.println("Usage:  java " + progName + ".java [mode] [size]");
        System.err.println("mode = TEST, IDEAL, TIME, TASK_GRAPH");
        System.err.println("size = problem size (int)");
    }
    
    public void initialize() {
    }
    
    public abstract void runTest();
    
    public abstract void runWork();
    
    public void runCleanup() {
    }
    
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
            long startTime;
            long endTime;
            startTime = System.nanoTime();
            runWork();
            endTime = System.nanoTime();
            runCleanup();
            System.out.println("Elapsed time: " + (endTime - startTime));
        } else if (mode.equals("TASK_GRAPH")) {
            Instrument.start();
            runWork();
            Instrument.end();
            runCleanup();
            Instrument.printTasks();
        } else {
            usage();
            System.exit(1);
        }
    }
}
