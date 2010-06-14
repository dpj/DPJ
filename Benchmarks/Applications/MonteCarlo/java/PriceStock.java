
import EDU.oswego.cs.dl.util.concurrent.*;

public class PriceStock extends Universal {
    public static boolean DEBUG = true;
    protected static String prompt = "PriceStock> ";
    private MonteCarloPath mcPath;
    private String taskHeader;
    private long randomSeed = -1;
    private double pathStartValue = Double.NaN;
    private ToResult result;
    private double expectedReturnRate = Double.NaN;
    private double volatility = Double.NaN;
    private double volatility2 = Double.NaN;
    private double finalStockPrice = Double.NaN;
    private double[] pathValue;
    
    public PriceStock() {
        super();
        mcPath = new MonteCarloPath();
        set_prompt(prompt);
        set_DEBUG(DEBUG);
    }
    
    public void setInitAllTasks(Object obj) {
        ToInitAllTasks initAllTasks = (ToInitAllTasks)obj;
        mcPath.set_name(initAllTasks.get_name());
        mcPath.set_startDate(initAllTasks.get_startDate());
        mcPath.set_endDate(initAllTasks.get_endDate());
        mcPath.set_dTime(initAllTasks.get_dTime());
        mcPath.set_returnDefinition(initAllTasks.get_returnDefinition());
        mcPath.set_expectedReturnRate(initAllTasks.get_expectedReturnRate());
        mcPath.set_volatility(initAllTasks.get_volatility());
        int nTimeSteps = initAllTasks.get_nTimeSteps();
        mcPath.set_nTimeSteps(nTimeSteps);
        this.pathStartValue = initAllTasks.get_pathStartValue();
        mcPath.set_pathStartValue(pathStartValue);
        mcPath.set_pathValue(new double[nTimeSteps]);
        mcPath.set_fluctuations(new double[nTimeSteps]);
    }
    
    public void setTask(Object obj) {
        ToTask task = (ToTask)obj;
        this.taskHeader = task.get_header();
        this.randomSeed = task.get_randomSeed();
    }
    
    public void run() {
        try {
            mcPath.computeFluctuationsGaussian(randomSeed);
            mcPath.computePathValue(pathStartValue);
            RatePath rateP = new RatePath(mcPath);
            ReturnPath returnP = rateP.getReturnCompounded();
            returnP.estimatePath();
            expectedReturnRate = returnP.get_expectedReturnRate();
            volatility = returnP.get_volatility();
            volatility2 = returnP.get_volatility2();
            finalStockPrice = rateP.getEndPathValue();
            pathValue = mcPath.get_pathValue();
        } catch (DemoException demoEx) {
            errPrintln(demoEx.toString());
        }
    }
    
    public Object getResult() {
        String resultHeader = "Result of task with Header=" + taskHeader + ": randomSeed=" + randomSeed + ": pathStartValue=" + pathStartValue;
        ToResult res = new ToResult(resultHeader, expectedReturnRate, volatility, volatility2, finalStockPrice, pathValue);
        return (Object)res;
    }
}
