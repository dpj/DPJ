package DPJRuntime;

import java.util.Stack;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Collections;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * {@code Instrument} is the support class for DPJ instrumentation.
 *
 * When a DPJ program is compiled with the {@code -instrument} flag,
 * the compiler generates calls to the methods of this class at
 * appropriate points in the code.
 *
 * @author Robert L. Bocchino Jr.
 * @author Mohsen Vakilian
 */
public class Instrument {

    /**
     * A simple free list allocator for managing environments
     */
    private static Freelist allocator = new Freelist();
    private static class Freelist {
	LinkedList<Environment> freelist = new LinkedList<Environment>();

	/**
	 * Get an environment off the list, or make one if the list is
	 * empty
	 */
	public Environment newEnv() {
	    Environment result;
	    if (freelist.size() > 0) {
		result = freelist.removeLast();
	    } else {
		result = new Environment();
	    }
	    result.ID = Environment.numIDs++;
	    return result;
	}

	public Environment newEnv(Environment oldEnv) {
	    Environment result = newEnv();
	    result.parallelStartTime = 
		oldEnv.parallelStartTime + oldEnv.parallelBranchTime;
	    return result;
	}

	/**
	 * Put an environment on the list when we're done with it
	 */
	public void freeEnv(Environment e) {
	    e.clear();
	    freelist.addLast(e);
	}
    }

    /**
     * Execution environment for measuring times
     */
    private static class Environment {

	/**
	 * Timer for measuring execution times
	 */
	private Timer timer = new Timer();

	public int ID = 0;
	public static int numIDs = 0;

	/**
	 * Serial execution time (all branches)
	 */
	private long serialTime;

	/**
	 * Pure serial time, i.e., total time when only one thread is
	 * executing.
	 */
	private long pureSerialTime;

	/**
	 * Parallel execution time (all branches)
	 */
	private long parallelTime;

	private long parallelStartTime;

	/**
	 * Parallel execution time (one branch).  Ultimately,
	 * parallelTime will be the maximum of all parallelBranchTimes
	 * we encountered in this environment.
	 */
	private long parallelBranchTime;

	public Environment() {
	}

	public Environment(Environment prevEnv) {
	    parallelStartTime = prevEnv.parallelStartTime + prevEnv.parallelTime;
	}

	public void clear() {
	    serialTime = 0;
	    parallelTime = 0;
	    parallelBranchTime = 0;
	}

	public void startTiming() {
	    timer.start();
	}

	public void stopTiming() {
	    timer.stop();
	    serialTime += timer.getElapsedTime();
	    if (envStack.size() == 1) {
		pureSerialTime += timer.getElapsedTime();
	    }
	    parallelBranchTime += timer.getElapsedTime();
	}
    }

    /**
     * A simple timer class for performance measurement
     */
    private static class Timer {

	/**
	 * Whether the timer is on
	 */
	private boolean on = false;
	
	/**
	 * Absolute time when the timer started
	 */
	private long startTime = 0;
	
	/**
	 * Absolute time when the timer ended
	 */
	private long endTime = 0;
	
	/**
	 * Time between start and end
	 */
	private long elapsedTime = 0;
	
	/**
	 * Start the timer
	 */
	public void start() { 
	    elapsedTime = 0;
	    startTime = System.nanoTime(); 
	}
	
	/**
	 * Stop the timer
	 */
	public void stop() {
	    endTime = System.nanoTime();
	    long difference = endTime - startTime;
	    if (difference > 0) elapsedTime = difference;
	}
	
	/**
	 * Get the elapsed time
	 */
	public long getElapsedTime() {
	    return elapsedTime;
	}
    }

    /**
     * A stack of environments for managing scopes
     */
    private static Stack<Environment> envStack = 
	new Stack<Environment>();

    /**
     * The final results
     */
    private static long serialTime;
    private static long pureSerialTime;
    private static long parallelTime;

    /**
     * Switch to turn the timing on and off
     */
    private static boolean on;

    /**
     * Enter time of each spawned task.
     */
    private static Map<Integer, Long> enterTimes = 
	new HashMap<Integer, Long>();

    /**
     * Exit time of each spawed task.
     */
    private static Map<Integer, Long> exitTimes = 
	new HashMap<Integer, Long>();

    private static TreeMap<Long,Integer> tasksDeltaAtTime = 
	new TreeMap<Long,Integer>();

    /** 
     * Entry tasksDeltaAtTime[t] denotes the number of generated tasks
     * at time `t'.
     */
    private static TreeMap<Long,Integer> numOfTasksAtTime = null;

    // Private helper methods

    private static void addToMapEntry(Map<Long, Integer> map, 
				      Long key, Integer delta) {
        if (!map.containsKey(key))
            map.put(key, 0);
        map.put(key, map.get(key) + delta);
    }

    private static Map<Long,Integer> 
	partialSum(TreeMap<Long,Integer> map) {
	if (numOfTasksAtTime != null) return numOfTasksAtTime;

        Long[] taskTimes = map.navigableKeySet().
	    toArray(new Long[]{});
        numOfTasksAtTime = new TreeMap<Long,Integer>();
	numOfTasksAtTime.put(taskTimes[0], 
			     map.get(taskTimes[0]));
        for (int i = 1; i < taskTimes.length; ++i) {
            numOfTasksAtTime.put(taskTimes[i], 
				 map.get(taskTimes[i]) + 
				 numOfTasksAtTime.
				 get(taskTimes[i-1]));
        } 
        return numOfTasksAtTime;
    }

    // Public instrumentation interface

    /**
     * Starts the timing.
     */
    public static void start() {
	//
	// Clear the map holding the number of active tasks at each
	// moment.
	//
	tasksDeltaAtTime = new TreeMap<Long, Integer>();
	//
	// Turn on the instrumentation
	//
	on = true;
	//
	// Start with a fresh stack of environments
	//
	envStack.clear();
	//
	// Push a new environment on the stack
	//
	Environment env = allocator.newEnv();
	envStack.push(env);
	addToMapEntry(tasksDeltaAtTime, 0L, 1); 
	//
	// Start timing in the current environment
	//
	env.startTiming();
    }

    /**
     * Called upon entry to a {@code foreach} statement.
     *
     * @param numIters The number of iterations in the {@code foreach}
     */
    public static void enterForeach(int numIters) {
	if (on) {
	    //
	    // Stop timing in the current environment
	    //
	    Environment env = envStack.peek();
	    env.stopTiming();
	    //
	    // Start a new environment
	    //
	    Environment newEnv = allocator.newEnv(env);
	    // Add (newEnv.parallelStartTime, numIters) to map
	    addToMapEntry(tasksDeltaAtTime, 
			  newEnv.parallelStartTime, numIters);
	    envStack.push(newEnv);
	}
    }

    /**
     * Called upon entry to a {@code foreach} iteration
     */
    public static void enterForeachIter() {
	if (on) {
	    //
	    // Get the current environment off the stack
	    //
	    Environment env = envStack.peek();
	    //
	    // Starting a new branch: reset the parallel branch time
	    //
	    env.parallelBranchTime = 0;
	    //
	    // Start timing
	    //
	    env.startTiming();
	}
    }

    /**
     * Called upon exit from a {@code foreach} iteration
     */
    public static void exitForeachIter() {
	if (on) {
	    //
	    // Get the current environment off the stack and stop
	    // timing
	    //
	    Environment env = envStack.peek();
	    env.stopTiming();
	    //
	    // If the current branch took longer than the previous
	    // longest branch, update the parallel time
	    //
	    if (env.parallelBranchTime > env.parallelTime)
		env.parallelTime = env.parallelBranchTime;
	    //
	    // Record end of iteration in map
	    //
	    // Add (env.parallelStartTime+env.parallelBranchTime,-1)
	    // to map
	    //
	    addToMapEntry(tasksDeltaAtTime, env.parallelStartTime +
			  env.parallelBranchTime, -1);
	}
    }

    /**
     * Called upon exit from a {@code foreach} statement.
     */
    public static void exitForeach() {
	if (on) {
	    //
	    // Get the environment of the foreach off the stack and
	    // pop out to the outer environment
	    //
	    Environment foreachEnv = envStack.pop();
	    //
	    // Get the new current (outer) environment off the stack
	    //
	    Environment env = envStack.peek();
	    //
	    // Serial and parallel times of the foreach contribute to
	    // the serial and parallel branch times of the outer
	    // environment.  We use the parallel time of the foreach
	    // environment because it's done, so we know its parallel
	    // time.  However, we update the parallel *branch* time of
	    // the outer environment because it's not done yet so we
	    // are computing the time of a parallel branch.  For
	    // example, it itself could be a foreach.
	    //
	    env.serialTime += foreachEnv.serialTime;
	    env.parallelBranchTime += foreachEnv.parallelTime;
	    //
	    // Put the popped environment back in the allocator pool
	    //
	    allocator.freeEnv(foreachEnv);
	    //
	    // Start timing again in the outer environment
	    //
	    env.startTiming();
	}
    }

    /**
     * Called upon entry to a {@code cobegin} statement.
     */
    public static void enterCobegin() {
	if (on) {
	    //
	    // Stop timing in the current environment
	    //
	    Environment env = envStack.peek();
	    env.stopTiming();
	    //
	    // Start a new environment
	    //
	    env = allocator.newEnv(env);
	    envStack.push(env);
	    //
	    // Start timing
	    //
	    env.startTiming();
	}
    }

    /**
     * Called between statements in a {@code cobegin} statement.
     */
    public static void cobeginSeparator() {
	if (on) {
	    //
	    // Get the current environment off the stack and stop
	    // timing
	    //
	    Environment env = envStack.peek();
	    env.stopTiming();
	    //
	    // If the current branch took longer than the previous
	    // longest branch, update the parallel time
	    //
	    if (env.parallelBranchTime > env.parallelTime)
		env.parallelTime = env.parallelBranchTime;
	    //
            if (env.parallelBranchTime > 0) {
	        addToMapEntry(tasksDeltaAtTime, 
			      env.parallelStartTime, 1);
	        addToMapEntry(tasksDeltaAtTime, 
			      env.parallelStartTime + 
			      env.parallelBranchTime, -1);
            }
	    //
	    // Starting a new branch: reset the parallel branch time
	    //
	    env.parallelBranchTime = 0;
	    //
	    // Start timing
	    //
	    env.startTiming();
	}
    }

    /**
     * Called upon exit from a {@code cobegin} statement.
     */
    public static void exitCobegin() {
	if (on) {
	    //
	    // Get the environment of the cobegin off the stack and
	    // pop out to the outer environment
	    //
	    Environment cobeginEnv = envStack.pop();
	    //
	    // Get the new current (outer) environment off the stack
	    //
	    Environment env = envStack.peek();
	    //
	    // Analogous to what we do for foreach; see comments
	    // there.
	    //
	    env.serialTime += cobeginEnv.serialTime;
	    env.parallelBranchTime += cobeginEnv.parallelTime;
	    //
	    // Put the popped environment back in the allocator pool
	    //
	    allocator.freeEnv(cobeginEnv);
	    //
	    // Start timing again in the outer environment
	    //
	    env.startTiming();
	}
    }

    /**
     * Called upon entry to a {@code finish} statement.
     */
    public static void enterFinish() {
	if (on) {
	    //
	    // Get the current environment off the stack and stop its
	    // timer
	    //
	    Environment env = envStack.peek();
	    env.timer.stop();
	    //
	    // Push a new environment to handle the finish and start
	    // timing in that environment
	    //
	    env = allocator.newEnv(env);
	    envStack.push(env);
	    env.timer.start();
	}
    }

    /**
     * Called upon exit from a {@code finish} statement.
     */
    public static void exitFinish() {
	if (on) {
	    //
	    // Get the current environment off the stack, stop its
	    // timer, and compute its elapsed time
	    //
	    Environment finishEnv = envStack.pop();
	    finishEnv.stopTiming();

	    if (finishEnv.parallelBranchTime > 
		finishEnv.parallelTime)
		finishEnv.parallelTime = 
		    finishEnv.parallelBranchTime;

	    Environment env = envStack.peek();
	    env.serialTime += finishEnv.serialTime;
	    env.parallelBranchTime += finishEnv.parallelTime;

	    //
	    // Free the popped environment and restart the timer in
	    // the outer scope
	    //
	    allocator.freeEnv(finishEnv);
	    env.timer.start();
	}
    }

    /**
     * Called upon entry to a {@code spawn} statement.
     */
    public static void enterSpawn() {
	if (on) {
	    //
	    // Get the current environment off the stack and stop its
	    // timer
	    //
	    Environment env = envStack.peek();
	    env.stopTiming();
	    //
	    // Push a new environment to handle the finish and start
	    // timing in that environment
	    //
	    Environment oldEnv = env;
	    env = allocator.newEnv(env);
	    //
            //Record the start time of the spawned task.
	    //
            enterTimes.put(env.ID, env.parallelStartTime);
	    envStack.push(env);
	    //
	    // Add (env.parallelStartTime, 1) to map
	    //
            addToMapEntry(tasksDeltaAtTime, env.parallelStartTime, 1);
	    env.startTiming();
	}
    }

    /**
     * Called upon exit from a {@code spawn} statement.
     */
    public static void exitSpawn() {
	if (on) {

	    //
	    // Get the current environment off the stack, stop its
	    // timer, and compute its elapsed time
	    //
	    Environment spawnEnv = envStack.pop();
	    spawnEnv.stopTiming();
	    //
	    // Update parallel time of spawn env
	    //
	    if (spawnEnv.parallelBranchTime > spawnEnv.parallelTime)
		spawnEnv.parallelTime = spawnEnv.parallelBranchTime;
	    //
	    // Update serial and parallel time of outer env
	    //
	    Environment env = envStack.peek();
	    env.serialTime += spawnEnv.serialTime;
	    long spawnTime = env.parallelBranchTime + 
		spawnEnv.parallelTime;
	    if (spawnTime > env.parallelTime)
		env.parallelTime = spawnTime;
	    //
	    // Record end of spawn in map
	    //
	    // Add (env.parallelStartTime+spawnTime, -1) to map
	    //
	    addToMapEntry(tasksDeltaAtTime, 
			  env.parallelStartTime+spawnTime, -1);
	    //
            // Record the end time of the spawned task.
	    //
            exitTimes.put(spawnEnv.ID, env.parallelStartTime+spawnTime);
	    //
	    // Free the popped environment and restart the timer in
	    // the outer scope
	    //
	    allocator.freeEnv(spawnEnv);
	    env.startTiming();
	}
    }

    /**
     * Called at the end of the computation.
     */
    public static void end() {
	Environment env = envStack.pop();
	env.stopTiming();
	on = false;
	parallelTime = env.parallelBranchTime;
	serialTime = env.serialTime;
	pureSerialTime = env.pureSerialTime;
	addToMapEntry(tasksDeltaAtTime, parallelTime, 0); 
    }

    // Output methods

    /**
     * Returns a map representing the program task graph.  The map
     * takes program points to number of tasks.  There is one entry in
     * the map for each point at which the number of tasks changed.
     *
     * @return Map representing program task graph
     */
    public static Map<Long,Integer> getTasksMap() {
        return Collections.
	    unmodifiableMap(partialSum(tasksDeltaAtTime));
    }

    /**
     * Prints a string representation of the task map to the given
     * file.
     *
     * @param filepath Pathname of file to print to
     */
    public static void printTasksMap(String filepath) 
	throws IOException {
        PrintWriter outputStream = null;
        try { 
            outputStream = 
		new PrintWriter(new FileWriter(filepath));
            Map<Long,Integer> tasksMap = getTasksMap();
            for (Long time : tasksMap.keySet()) {
              outputStream.println(time + 
				   "\t" + tasksMap.get(time));
            }
        } finally {
            if (outputStream != null)
                outputStream.close();
        }
    }

    /**
     * Prints the start and end point of each task to the given file.
     *
     * @param filepath Pathname of file to print to
     */
    public static void printTaskIntervals(String filepath) 
	throws IOException {
        PrintWriter outputStream = null;
        try { 
            outputStream = new PrintWriter(new FileWriter(filepath));
            for (Integer taskID : enterTimes.keySet()) {
              outputStream.println(taskID + "\t" + 
				   enterTimes.get(taskID) + "\t" + 
				   exitTimes.get(taskID));
            }
        } finally {
            if (outputStream != null)
                outputStream.close();
        }
    }

    /**
     * Returns the average width of the parallelism graph, i.e., the
     * average number of tasks active at every point of the program.
     * Computed as the quotient of (1) the area under the curve of
     * number of tasks vs. time and (2) the total time.
     *
     * @return Average width of the parallelism graph
     */
    public static double averageWidth() {
        Long[] taskTimes = Instrument.getTasksMap().
	    keySet().toArray(new Long[]{});

	long totalWidth = 0;

        for (int i = 1; i < taskTimes.length; ++i) {
		totalWidth += (taskTimes[i] - 
			       taskTimes[i-1]) * 
		    numOfTasksAtTime.get(taskTimes[i-1]);
	}

	return ((double) totalWidth / taskTimes[taskTimes.length-1]);
    }

    /**
     * Returns the ideal speedup, computed as the serial time divided
     * by the parallel time.
     *
     * @return Ideal speedup
     */
    public static double idealSpeedup() {
	return ((double) serialTime) / parallelTime;
    }

    /**
     * Returns the measured serial time of the computation.  Within a
     * {@code cobegin} or {@code foreach} construct, the serial time
     * is the sum of the times of the individual tasks.
     *
     * @return Serial time
     */
    public static long getSerialTime() { return serialTime; }

    /**
     * Returns the measured parallel time of the computation.  Within
     * a {@code cobegin} or {@code foreach} construct, the parallel
     * time is the maximum of the times of the indiviudal tasks.
     *
     * @return Parallel time
     */
    public static long getParallelTime() { return parallelTime; }

    /**
     * Returns the best speedup we could achieve under Amdahl's law,
     * assuming perfect speedup of the parallel parts.  This is
     * accurate for programs that create all parallelism with {@code
     * cobegin} and {@code foreach}.  It is not accurate for programs
     * that use {@code spawn} and {@code finish}.
     *
     * @return Bound given by Amdahl's law
     */
    public static double amdahlBound() {
	return ((double) serialTime) / pureSerialTime;
    }

}
