

import java.util.*;
import java.awt.*;
import java.util.concurrent.locks.ReentrantLock;

/**
  * Code, a test-harness for invoking and driving the Applications
  * Demonstrator classes.
  *
  * <p>To do:
  * <ol>
  *   <li>Very long delay prior to connecting to the server.</li>
  *   <li>Some text output seem to struggle to get out, without
  *       the user tapping ENTER on the keyboard!</li>
  * </ol>
  *
  * @author H W Yau
  * @version $Revision: 1.12 $ $Date: 1999/02/16 19:13:38 $
  */
public class AppDemo extends Universal {
  //------------------------------------------------------------------------
  // Class variables.
  //------------------------------------------------------------------------

  // DPJ: Region declarations
  // TaskR: for tasks
  // ResultR: for results (one result per task)
  // reductionR: for local reduction
  region ResultR, TaskR, reductionR;

  public static double JGFavgExpectedReturnRateMC =0.0;
  /**
    * A class variable.
    */
  public static boolean DEBUG=true;
  /**
    * The prompt to write before any debug messages.
    */
  protected static String prompt="AppDemo> ";

  public static final int Serial=1;
  //------------------------------------------------------------------------
  // Instance variables.
  //------------------------------------------------------------------------
  /**
    * Directory in which to find the historical rates.
    */
  private String dataDirname;
  /**
    * Name of the historical rate to model.
    */
  private String dataFilename;
  /**
    * The number of time-steps which the Monte Carlo simulation should
    * run for.
    */
  private int nTimeStepsMC=0;
  /**
    * The number of Monte Carlo simulations to run.
    */
  private int nRunsMC=0;
  /**
    * The default duration between time-steps, in units of a year.
    */
  private double dTime = 1.0/365.0;
  /**
    * Flag to determine whether initialisation has already taken place.
    */
  private boolean initialised=false;
  /**
    * Variable to determine which deployment scenario to run.
    */
  private int runMode;

  // An index-parameterized array of tasks under TaskR
    private ToTask<TaskR:[i]>[]<TaskR:[i]>#i tasks in TaskR;

  // DPJ
  // an index-parameterized array of results under ResultR
  private ToResult<ResultR:[i]>[]<ResultR:[i]>#i results in ResultR;

  public AppDemo(String dataDirname, String dataFilename, int nTimeStepsMC, int nRunsMC) {
    this.dataDirname    = dataDirname;
    this.dataFilename   = dataFilename;
    this.nTimeStepsMC   = nTimeStepsMC;
    this.nRunsMC        = nRunsMC;
    this.initialised    = false;
    set_prompt(prompt);
    set_DEBUG(DEBUG);
  }
  /**
    * Single point of contact for running this increasingly bloated
    * class.  Other run modes can later be defined for whether a new rate
    * should be loaded in, etc.
    * Note that if the <code>hostname</code> is set to the string "none",
    * then the demonstrator runs in purely serial mode.
    */

  /**
    * Initialisation and Run methods.
    */
    double pathStartValue = 100.0;
    double avgExpectedReturnRateMC in reductionR = 0.0;
    double avgVolatilityMC in reductionR = 0.0;

    ToInitAllTasks initAllTasks = null;

    public void initParallel() {
    
      try{
      //
      // Measure the requested path rate.
      // read from the file and store the data in an array
      // rateP contains the entire data as an array
      RatePath rateP = new RatePath(dataDirname, dataFilename);
      // for debugging purpose
      rateP.dbgDumpFields();
      
      ReturnPath returnP = rateP.getReturnCompounded();
   
      returnP.estimatePath();
      returnP.dbgDumpFields();
      
      // get expected return rate and volatility
      double expectedReturnRate = returnP.get_expectedReturnRate();
      double volatility         = returnP.get_volatility();
      
      // Now prepare for MC runs.
      // initialize basic task information with the computed values in ReturnPath    
      initAllTasks = new ToInitAllTasks(returnP, nTimeStepsMC, pathStartValue);
      String slaveClassName = "MonteCarlo.PriceStock";
      //
      // Now create the tasks.
      initTasks(nRunsMC);
      //
    } catch( DemoException demoEx ) {
      dbgPrintln(demoEx.toString());
      System.exit(-1);
    }
  }
    
  //------------------------------------------------------------------------
  /**
    * Generates the parameters for the given Monte Carlo simulation.
    *
    * @param nRunsMC the number of tasks, and hence Monte Carlo paths to
    *        produce.
    */
  private void initTasks(int nRunsMC) writes TaskR, TaskR:[?] {
        
      // initialize task array
      tasks = new ToTask<TaskR:[i]>[nRunsMC]<TaskR:[i]>#i;
	
      // for each task, parallel init
      foreach (int i in 0, nRunsMC) {
        String header = "MC run "+String.valueOf(i);
        ToTask<TaskR:[i]> task = 
	    new ToTask<TaskR:[i]>(header, (long)i*11);
        tasks[i] = task;
      }
   }

  // to process each element in result array in parallel per every iteration,
  // an effect should be specified to show it's partitionable
  public void runParallel() {
	 	 
      results = new ToResult<ResultR:[i]>[nRunsMC]<ResultR:[i]>#i;
           
      foreach (int iRun in 0, nRunsMC) {
	  // [iRun] notation indicates separate region for each PriceStock object
     	  // each PriceStock object should be declared as a local object for each iteration
	  PriceStock<ResultR:[iRun]> ps = new PriceStock<ResultR:[iRun]>();
	  ps.setInitAllTasks(initAllTasks);
	  // read the corresponding task and copy its value to PriceStock
	  ps.setTask(tasks[iRun]);
	  
	  // ****************************************************
	  // main Monte Carlo computation
	  ps.run();
	  // ****************************************************
	  
	  results[iRun] = ps.getResult();
      }
  }
  
  public void processParallel() {
      //
      // Process the results.
    try {
      processResults();
    } catch( DemoException demoEx ) {
      dbgPrintln(demoEx.toString());
      System.exit(-1);
    }
  }
 
  /**
    * Method for doing something with the Monte Carlo simulations.
    * It's probably not mathematically correct, but shall take an average over
    * all the simulated rate paths.
    *
    * @exception DemoException thrown if there is a problem with reading in
    *            any values.
    */

 RatePath<reductionR> avgMCrate;
 //RatePath avgMCrate;
 ReentrantLock lock = new ReentrantLock();
 RatePath<reductionR:[i]>[]<reductionR:[i]>#i localAvgMCrate;

    commutative void sumReduction(final int index, double localAvgExpectedReturnRateMC, 
				  double localAvgVolatilityMC) 
	reads Root, reductionR:[index] writes reductionR {
	lock.lock();
	avgExpectedReturnRateMC += localAvgExpectedReturnRateMC;
	avgVolatilityMC += localAvgVolatilityMC;
	avgMCrate.inc_pathValue2(localAvgMCrate[index].get_pathValue());
	lock.unlock();
 }

  private void processResults() throws DemoException {	  
	avgExpectedReturnRateMC = 0.0;
	avgVolatilityMC = 0.0;

	double runAvgExpectedReturnRateMC = 0.0;
    double runAvgVolatilityMC = 0.0;
    //ToResult returnMC;
    //if( nRunsMC != results.size() ) {
    if (nRunsMC != results.length) {
    	errPrintln("Fatal: TaskRunner managed to finish with no all the results gathered in!");
      System.exit(-1);
    }
    //
    // Create an instance of a RatePath, for accumulating the results of the
    // Monte Carlo simulations.
    avgMCrate = new RatePath<reductionR>(nTimeStepsMC, "MC", 19990109, 19991231, dTime);
      
    // parallelize the reduction using local and tiling
    int tileSize = 100;
    localAvgMCrate = new RatePath<reductionR:[i]>[nRunsMC/tileSize]<reductionR:[i]>#i;  

    foreach (int p in 0, (nRunsMC/tileSize)) {

    	int start = p * tileSize;
    	int end = (p+1) * tileSize;
    	double localAvgExpectedReturnRateMC = 0.0;
        double localAvgVolatilityMC = 0.0;

	localAvgMCrate[p] = new RatePath<reductionR:[p]>(nTimeStepsMC, "MC", 19990109, 19991231, dTime);

    	for (int i=start;i<end;i++) {
	    ToResult<ResultR:[?]> returnMC = results[i];
	    localAvgMCrate[p].inc_pathValue(returnMC.get_pathValue());

    	    // reductions (sum)
    	    localAvgExpectedReturnRateMC += returnMC.get_expectedReturnRate();
    	    localAvgVolatilityMC         += returnMC.get_volatility();
       	}
    	
    	// update global sum
	sumReduction(p, localAvgExpectedReturnRateMC, localAvgVolatilityMC);

    }
    
    // ********************************************************************
    // final result
    avgMCrate.inc_pathValue((double)1.0/((double)nRunsMC));
	avgExpectedReturnRateMC /= nRunsMC;
	avgVolatilityMC /= nRunsMC;
    // ********************************************************************
    
	JGFavgExpectedReturnRateMC = avgExpectedReturnRateMC;

//    dbgPrintln("Average over "+nRunsMC+": expectedReturnRate="+
//    avgExpectedReturnRateMC+" volatility="+avgVolatilityMC + JGFavgExpectedReturnRateMC);
  }
  //
  //------------------------------------------------------------------------
  // Accessor methods for class AppDemo.
  // Generated by 'makeJavaAccessor.pl' script.  HWY.  20th January 1999.
  //------------------------------------------------------------------------
  /**
    * Accessor method for private instance variable <code>dataDirname</code>.
    *
    * @return Value of instance variable <code>dataDirname</code>.
    */
  public String get_dataDirname() {
    return(this.dataDirname);
  }

  /**
    * Set method for private instance variable <code>dataDirname</code>.
    *
    * @param dataDirname the value to set for the instance variable <code>dataDirname</code>.
    */
  public void set_dataDirname(String dataDirname) {
    this.dataDirname = dataDirname;
  }

  /**
    * Accessor method for private instance variable <code>dataFilename</code>.
    *
    * @return Value of instance variable <code>dataFilename</code>.
    */
  public String get_dataFilename() {
    return(this.dataFilename);
  }

  /**
    * Set method for private instance variable <code>dataFilename</code>.
    *
    * @param dataFilename the value to set for the instance variable <code>dataFilename</code>.
    */
  public void set_dataFilename(String dataFilename) {
    this.dataFilename = dataFilename;
  }

  /**
    * Accessor method for private instance variable <code>nTimeStepsMC</code>.
    *
    * @return Value of instance variable <code>nTimeStepsMC</code>.
    */
  public int get_nTimeStepsMC() {
    return(this.nTimeStepsMC);
  }

  /**
    * Set method for private instance variable <code>nTimeStepsMC</code>.
    *
    * @param nTimeStepsMC the value to set for the instance variable <code>nTimeStepsMC</code>.
    */
  public void set_nTimeStepsMC(int nTimeStepsMC) {
    this.nTimeStepsMC = nTimeStepsMC;
  }

  /**
    * Accessor method for private instance variable <code>nRunsMC</code>.
    *
    * @return Value of instance variable <code>nRunsMC</code>.
    */
  public int get_nRunsMC() {
    return(this.nRunsMC);
  }

  /**
    * Set method for private instance variable <code>nRunsMC</code>.
    *
    * @param nRunsMC the value to set for the instance variable <code>nRunsMC</code>.
    */
  public void set_nRunsMC(int nRunsMC) {
    this.nRunsMC = nRunsMC;
  }
  
  /**
    * Accessor method for private instance variable <code>tasks</code>.
    *
    * @return Value of instance variable <code>tasks</code>.
    */
  public ToTask<TaskR:[i]>[]<TaskR:[i]>#i get_tasks() reads TaskR {
  	return(this.tasks);
  }
  /**
    * Set method for private instance variable <code>tasks</code>.
    *
    * @param tasks the value to set for the instance variable <code>tasks</code>.
    */
  public void set_tasks(ToTask<TaskR:[i]>[]<TaskR:[i]>#i tasks) writes TaskR {
	this.tasks = tasks;
  }

  /**
    * Accessor method for private instance variable <code>results</code>.
    *
    * @return Value of instance variable <code>results</code>.
    */
  public ToResult<ResultR:[i]>[]<ResultR:[i]>#i get_results() reads Root,ResultR {
  	return(this.results);
  }

  /**
    * Set method for private instance variable <code>results</code>.
    *
    * @param results the value to set for the instance variable <code>results</code>.
    */
  public void set_results(ToResult<ResultR:[i]>[]<ResultR:[i]>#i results) writes ResultR {
  	this.results = results;
  }
 
  //------------------------------------------------------------------------
}
