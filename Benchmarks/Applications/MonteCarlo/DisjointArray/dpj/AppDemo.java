import java.util.*;
import java.awt.*;
import jsr166y.*;
import extra166y.*;
import DPJRuntime.Framework.*;
import DPJRuntime.Framework.ArrayOps.*;
import java.util.concurrent.atomic.*;

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

    region Cont; // ...for the containers
    region Tasks; // ...for the tasks
    region Results; // ...for the results
    private DisjointArray<ToTask<Tasks>,Cont> tasks;
    private DisjointArray<ResultWrapper<Results>,Cont> results;

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

    PriceStock psMC;
    double pathStartValue = 100.0;
    double avgExpectedReturnRateMC = 0.0;
    double avgVolatilityMC = 0.0;


    ToInitAllTasks initAllTasks = null;

    //public void initSerial() { 
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

    public void runParallel() {
	results = tasks.<ResultWrapper<Results>,reads Root,Tasks>
	    withMapping(new TaskToResultWrapper());
    }
  
    public static <region R1,R2>void incPathValue(double[]<R1> operand, double[]<R2> result) 
	reads R1 writes R2 {
	for (int i = 0; i < operand.length; ++i) {
	    result[i] += operand[i];
	}
    }

    final class ResultWrapper<region R> {
	double avgReturnRate in this;
	double avgVolatility in this;
	double[]<this> pathValue in this;
	ToResult<R> result;	
	ResultWrapper(ToResult<R> result) reads R writes this {
	    avgReturnRate = result.get_expectedReturnRate();
	    avgVolatility = result.get_volatility();
	    this.pathValue = new double[result.get_pathValue().length]<this>;
	    incPathValue(result.get_pathValue(), this.pathValue);
	    //this.pathValue = result.get_pathValue();
	    this.result = result;
	}
    }
  
    final class TaskToResultWrapper
	implements DisjointObjectToObject<ToTask<Tasks>,
		   ResultWrapper<Results>,reads Root, Tasks> {
	public <region R>ResultWrapper<R> op(final ToTask<Tasks> task) 
	    reads Root, Tasks writes R {
	    PriceStock<R> ps = new PriceStock<R>();
	    ps.setInitAllTasks(initAllTasks);
	    ps.setTask(task);
	    ps.run();
	    return new ResultWrapper<R>(ps.getResult());
	}
    }

    public void processParallel() {
	//
	// Process the results.
	System.out.println("processParallel");
	try {
	    processResults();
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
    private void initTasks(int nRunsMC) {
	tasks = new DisjointArray.Creator<ToTask<Tasks>,
	    Cont>().create(nRunsMC, ToTask.class);
	tasks = tasks.<pure>withIndexedMapping(new InitTask());
    }
  
    final class InitTask implements DisjointIntAndObjectToObject<ToTask<Tasks>,
				    ToTask<Tasks>, pure> {
	public <region R>ToTask<R> op(int index, final ToTask<Tasks> unused) pure {
	    String header="MC run "+String.valueOf(index);
	    ToTask<R> task = new ToTask<R>(header, (long)(index*11));
	    return task;
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

    private class ResultReducer implements Reducer<ResultWrapper<Results>,pure> {
	public ResultWrapper<Results> op(final ResultWrapper<Results> A,
					 final ResultWrapper<Results> B) writes A, B {
	    A.avgReturnRate += B.avgReturnRate;
	    A.avgVolatility += B.avgVolatility;
	    for (int i = 0; i < A.pathValue.length; ++i) {
		A.pathValue[i] += B.pathValue[i];
	    }
	    return A;
	}
    }

    private void processResults() throws DemoException {
	ResultWrapper<Results> first = results.get(0);

	if (nRunsMC != results.size()) {
	    errPrintln("Fatal: TaskRunner managed to finish with no all the results gathered in!");
	    System.exit(-1);
	}

	// Create an instance of a RatePath, for accumulating the results of the
	// Monte Carlo simulations.
	RatePath<Results> avgMCrate = 
	    new RatePath<Results>(nTimeStepsMC, "MC", 19990109, 19991231, dTime);

	ResultWrapper<Results> result = 
	    results.<pure>reduce(new ResultReducer(), first);
	double avgExpectedReturnRateMC = result.avgReturnRate;
	double avgVolatilityMC = result.avgVolatility;

	// ********************************************************************
	// final result
	avgMCrate.inc_pathValue(result.pathValue);
	//avgMCrate.set_pathValue(result.pathValue);
	avgMCrate.inc_pathValue((double)1.0/((double)nRunsMC));
	avgExpectedReturnRateMC /= nRunsMC;
	avgVolatilityMC         /= nRunsMC;
	// ********************************************************************
	
	JGFavgExpectedReturnRateMC = avgExpectedReturnRateMC;
	
	dbgPrintln("Average over "+nRunsMC+": expectedReturnRate="+
		   avgExpectedReturnRateMC+" volatility="+avgVolatilityMC + JGFavgExpectedReturnRateMC);
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
  
  //------------------------------------------------------------------------

    // Hack to get around the fact that Java doesn't let us say C<R>.class
    private class ParallelArrayMaker<type T<region TR>> {
	<region R>ParallelArray<T<R>> create(int n, Class<T> cls) {
	    Class<T<R>> cls1 = (Class<T<R>>) cls;
	    return ParallelArray.<T<R>>create(n, cls1, ParallelArray.defaultExecutor());
	}
    }


}

