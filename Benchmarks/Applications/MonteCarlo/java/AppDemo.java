
import jsr166y.forkjoin.*;
import java.util.*;
import java.awt.*;
import java.util.concurrent.locks.ReentrantLock;

public class AppDemo extends Universal {
    public static double JGFavgExpectedReturnRateMC = 0.0;
    public static boolean DEBUG = true;
    protected static String prompt = "AppDemo> ";
    public static final int Serial = 1;
    private String dataDirname;
    private String dataFilename;
    private int nTimeStepsMC = 0;
    private int nRunsMC = 0;
    private double dTime = 1.0 / 365.0;
    private boolean initialised = false;
    private int runMode;
    private Object[] tasks;
    private Object[] results;
    
    public AppDemo(String dataDirname, String dataFilename, int nTimeStepsMC, int nRunsMC) {
        super();
        this.dataDirname = dataDirname;
        this.dataFilename = dataFilename;
        this.nTimeStepsMC = nTimeStepsMC;
        this.nRunsMC = nRunsMC;
        this.initialised = false;
        set_prompt(prompt);
        set_DEBUG(DEBUG);
    }
    double pathStartValue = 100.0;
    double avgExpectedReturnRateMC = 0.0;
    double avgVolatilityMC = 0.0;
    ToInitAllTasks initAllTasks = null;
    
    public void initParallel() {
        try {
            RatePath rateP = new RatePath(dataDirname, dataFilename);
            rateP.dbgDumpFields();
            ReturnPath returnP = rateP.getReturnCompounded();
            returnP.estimatePath();
            returnP.dbgDumpFields();
            double expectedReturnRate = returnP.get_expectedReturnRate();
            double volatility = returnP.get_volatility();
            initAllTasks = new ToInitAllTasks(returnP, nTimeStepsMC, pathStartValue);
            String slaveClassName = "MonteCarlo.PriceStock";
            initTasks(nRunsMC);
        } catch (DemoException demoEx) {
            dbgPrintln(demoEx.toString());
            System.exit(-1);
        }
    }
    
    private void initTasks(int nRunsMC) {
        tasks = new Object[nRunsMC];
        
        class __dpj_S0 extends RecursiveAction {
            int __dpj_begin;
            int __dpj_length;
            int __dpj_stride;
            java.lang.Object[] tasks;
            __dpj_S0(int __dpj_begin, int __dpj_length, int __dpj_stride, java.lang.Object[] tasks) {
                this.__dpj_begin = __dpj_begin;
                this.__dpj_length = __dpj_length;
                this.__dpj_stride = __dpj_stride;
                this.tasks=tasks;
            }
            protected void compute() {
                if((__dpj_length / __dpj_stride) > DPJRuntime.RuntimeOptions.dpjForCutoff) {
                    RecursiveAction[] __dpj_splits = new RecursiveAction[DPJRuntime.RuntimeOptions.dpjForSplit];
                    for(int i=0; i<DPJRuntime.RuntimeOptions.dpjForSplit; i++)
                        __dpj_splits[i] = new __dpj_S0(__dpj_begin + (__dpj_length/DPJRuntime.RuntimeOptions.dpjForSplit)*i, (i+1==DPJRuntime.RuntimeOptions.dpjForSplit) ? (__dpj_length -  __dpj_length/DPJRuntime.RuntimeOptions.dpjForSplit*i) : (__dpj_length/DPJRuntime.RuntimeOptions.dpjForSplit), __dpj_stride, tasks);
                    RecursiveAction.forkJoin(__dpj_splits);
                }
                else {
                    for(int i = __dpj_begin; i < __dpj_begin + __dpj_length * __dpj_stride; i+=__dpj_stride)
                        {
                            String header = "MC run " + String.valueOf(i);
                            ToTask task = new ToTask(header, (long)i * 11);
                            tasks[i] = (Object)task;
                        }                    }
                }
            };
            (new __dpj_S0(0, nRunsMC, 1            , tasks)).forkJoin();

        }
        
        public void runParallel() {
            results = new Object[nRunsMC];
            
            class __dpj_S1 extends RecursiveAction {
                int __dpj_begin;
                int __dpj_length;
                int __dpj_stride;
                java.lang.Object[] tasks;
                java.lang.Object[] results;
                ToInitAllTasks initAllTasks;
                __dpj_S1(int __dpj_begin, int __dpj_length, int __dpj_stride, java.lang.Object[] tasks, java.lang.Object[] results, ToInitAllTasks initAllTasks) {
                    this.__dpj_begin = __dpj_begin;
                    this.__dpj_length = __dpj_length;
                    this.__dpj_stride = __dpj_stride;
                    this.tasks=tasks;
                    this.results=results;
                    this.initAllTasks=initAllTasks;
                }
                protected void compute() {
                    if((__dpj_length / __dpj_stride) > DPJRuntime.RuntimeOptions.dpjForCutoff) {
                        RecursiveAction[] __dpj_splits = new RecursiveAction[DPJRuntime.RuntimeOptions.dpjForSplit];
                        for(int i=0; i<DPJRuntime.RuntimeOptions.dpjForSplit; i++)
                            __dpj_splits[i] = new __dpj_S1(__dpj_begin + (__dpj_length/DPJRuntime.RuntimeOptions.dpjForSplit)*i, (i+1==DPJRuntime.RuntimeOptions.dpjForSplit) ? (__dpj_length -  __dpj_length/DPJRuntime.RuntimeOptions.dpjForSplit*i) : (__dpj_length/DPJRuntime.RuntimeOptions.dpjForSplit), __dpj_stride, tasks, results, initAllTasks);
                        RecursiveAction.forkJoin(__dpj_splits);
                    }
                    else {
                        for(int iRun = __dpj_begin; iRun < __dpj_begin + __dpj_length * __dpj_stride; iRun+=__dpj_stride)
                            {
                                PriceStock ps = new PriceStock();
                                ps.setInitAllTasks(initAllTasks);
                                ps.setTask(tasks[iRun]);
                                ps.run();
                                results[iRun] = ps.getResult();
                            }                        }
                    }
                };
                (new __dpj_S1(0, nRunsMC, 1                , tasks                , results                , initAllTasks)).forkJoin();

            }
            
            public void processParallel() {
                try {
                    processResults();
                } catch (DemoException demoEx) {
                    dbgPrintln(demoEx.toString());
                    System.exit(-1);
                }
            }
            
            private void processResults() throws DemoException {
                double[] avgExpectedReturnRateMC = new double[1];
                double[] avgVolatilityMC = new double[1];
                double runAvgExpectedReturnRateMC = 0.0;
                double runAvgVolatilityMC = 0.0;
                if (nRunsMC != results.length) {
                    errPrintln("Fatal: TaskRunner managed to finish with no all the results gathered in!");
                    System.exit(-1);
                }
                RatePath avgMCrate = new RatePath(nTimeStepsMC, "MC", 19990109, 19991231, dTime);
                int tileSize = 100;
                ReentrantLock lock = new ReentrantLock();
                
                class __dpj_S2 extends RecursiveAction {
                    int __dpj_begin;
                    int __dpj_length;
                    int __dpj_stride;
                    int tileSize;
                    java.lang.Object[] results;
                    double[] avgExpectedReturnRateMC;
                    java.util.concurrent.locks.ReentrantLock lock;
                    double[] avgVolatilityMC;
                    RatePath avgMCrate;
                    __dpj_S2(int __dpj_begin, int __dpj_length, int __dpj_stride, int tileSize, java.lang.Object[] results, double[] avgExpectedReturnRateMC, java.util.concurrent.locks.ReentrantLock lock, double[] avgVolatilityMC, RatePath avgMCrate) {
                        this.__dpj_begin = __dpj_begin;
                        this.__dpj_length = __dpj_length;
                        this.__dpj_stride = __dpj_stride;
                        this.tileSize=tileSize;
                        this.results=results;
                        this.avgExpectedReturnRateMC=avgExpectedReturnRateMC;
                        this.lock=lock;
                        this.avgVolatilityMC=avgVolatilityMC;
                        this.avgMCrate=avgMCrate;
                    }
                    protected void compute() {
                        if((__dpj_length / __dpj_stride) > DPJRuntime.RuntimeOptions.dpjForCutoff) {
                            RecursiveAction[] __dpj_splits = new RecursiveAction[DPJRuntime.RuntimeOptions.dpjForSplit];
                            for(int i=0; i<DPJRuntime.RuntimeOptions.dpjForSplit; i++)
                                __dpj_splits[i] = new __dpj_S2(__dpj_begin + (__dpj_length/DPJRuntime.RuntimeOptions.dpjForSplit)*i, (i+1==DPJRuntime.RuntimeOptions.dpjForSplit) ? (__dpj_length -  __dpj_length/DPJRuntime.RuntimeOptions.dpjForSplit*i) : (__dpj_length/DPJRuntime.RuntimeOptions.dpjForSplit), __dpj_stride, tileSize, results, avgExpectedReturnRateMC, lock, avgVolatilityMC, avgMCrate);
                            RecursiveAction.forkJoin(__dpj_splits);
                        }
                        else {
                            for(int p = __dpj_begin; p < __dpj_begin + __dpj_length * __dpj_stride; p+=__dpj_stride)
                                {
                                    int start = p * tileSize;
                                    int end = (p + 1) * tileSize;
                                    double localAvgExpectedReturnRateMC = 0.0;
                                    double localAvgVolatilityMC = 0.0;
                                    for (int i = start; i < end; i++) {
                                        ToResult returnMC = (ToResult)results[i];
                                        avgMCrate.inc_pathValue(returnMC.get_pathValue());
                                        localAvgExpectedReturnRateMC += returnMC.get_expectedReturnRate();
                                        localAvgVolatilityMC += returnMC.get_volatility();
                                    }
                                    lock.lock();
                                    avgExpectedReturnRateMC[0] += localAvgExpectedReturnRateMC;
                                    avgVolatilityMC[0] += localAvgVolatilityMC;
                                    lock.unlock();
                                }                            }
                        }
                    };
                    (new __dpj_S2(0, (nRunsMC / tileSize), 1                    , tileSize                    , results                    , avgExpectedReturnRateMC                    , lock                    , avgVolatilityMC                    , avgMCrate)).forkJoin();

                    avgMCrate.inc_pathValue((double)1.0 / ((double)nRunsMC));
                    avgExpectedReturnRateMC[0] /= nRunsMC;
                    avgVolatilityMC[0] /= nRunsMC;
                    JGFavgExpectedReturnRateMC = avgExpectedReturnRateMC[0];
                }
                
                public String get_dataDirname() {
                    return (this.dataDirname);
                }
                
                public void set_dataDirname(String dataDirname) {
                    this.dataDirname = dataDirname;
                }
                
                public String get_dataFilename() {
                    return (this.dataFilename);
                }
                
                public void set_dataFilename(String dataFilename) {
                    this.dataFilename = dataFilename;
                }
                
                public int get_nTimeStepsMC() {
                    return (this.nTimeStepsMC);
                }
                
                public void set_nTimeStepsMC(int nTimeStepsMC) {
                    this.nTimeStepsMC = nTimeStepsMC;
                }
                
                public int get_nRunsMC() {
                    return (this.nRunsMC);
                }
                
                public void set_nRunsMC(int nRunsMC) {
                    this.nRunsMC = nRunsMC;
                }
                
                public Object[] get_tasks() {
                    return (this.tasks);
                }
                
                public void set_tasks(Object[] tasks) {
                    this.tasks = tasks;
                }
                
                public Object[] get_results() {
                    return (this.results);
                }
                
                public void set_results(Object[] results) {
                    this.results = results;
                }
            }
