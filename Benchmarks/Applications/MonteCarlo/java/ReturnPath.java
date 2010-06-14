
import jsr166y.forkjoin.*;
import jsr166y.forkjoin.*;

public class ReturnPath extends PathId {
    public static boolean DEBUG = true;
    protected static String prompt = "ReturnPath> ";
    public static int COMPOUNDED = 1;
    public static int NONCOMPOUNDED = 2;
    private double[] pathValue;
    private int nPathValue = 0;
    private int returnDefinition = 0;
    private double expectedReturnRate = Double.NaN;
    private double volatility = Double.NaN;
    private double volatility2 = Double.NaN;
    private double mean = Double.NaN;
    private double variance = Double.NaN;
    
    public ReturnPath() {
        super();
        set_prompt(prompt);
        set_DEBUG(DEBUG);
    }
    
    public ReturnPath(double[] pathValue, int nPathValue, int returnDefinition) {
        super();
        set_prompt(prompt);
        set_DEBUG(DEBUG);
        this.pathValue = pathValue;
        this.nPathValue = nPathValue;
        this.returnDefinition = returnDefinition;
    }
    
    public double[] get_pathValue() throws DemoException {
        if (this.pathValue == null) throw new DemoException("Variable pathValue is undefined!");
        return (this.pathValue);
    }
    
    public void set_pathValue(double[] pathValue) {
        this.pathValue = pathValue;
    }
    
    public int get_nPathValue() throws DemoException {
        if (this.nPathValue == 0) throw new DemoException("Variable nPathValue is undefined!");
        return (this.nPathValue);
    }
    
    public void set_nPathValue(int nPathValue) {
        this.nPathValue = nPathValue;
    }
    
    public int get_returnDefinition() throws DemoException {
        if (this.returnDefinition == 0) throw new DemoException("Variable returnDefinition is undefined!");
        return (this.returnDefinition);
    }
    
    public void set_returnDefinition(int returnDefinition) {
        this.returnDefinition = returnDefinition;
    }
    
    public double get_expectedReturnRate() throws DemoException {
        if (this.expectedReturnRate == Double.NaN) throw new DemoException("Variable expectedReturnRate is undefined!");
        return (this.expectedReturnRate);
    }
    
    public void set_expectedReturnRate(double expectedReturnRate) {
        this.expectedReturnRate = expectedReturnRate;
    }
    
    public double get_volatility() throws DemoException {
        if (this.volatility == Double.NaN) throw new DemoException("Variable volatility is undefined!");
        return (this.volatility);
    }
    
    public void set_volatility(double volatility) {
        this.volatility = volatility;
    }
    
    public double get_volatility2() throws DemoException {
        if (this.volatility2 == Double.NaN) throw new DemoException("Variable volatility2 is undefined!");
        return (this.volatility2);
    }
    
    public void set_volatility2(double volatility2) {
        this.volatility2 = volatility2;
    }
    
    public double get_mean() throws DemoException {
        if (this.mean == Double.NaN) throw new DemoException("Variable mean is undefined!");
        return (this.mean);
    }
    
    public void set_mean(double mean) {
        this.mean = mean;
    }
    
    public double get_variance() throws DemoException {
        if (this.variance == Double.NaN) throw new DemoException("Variable variance is undefined!");
        return (this.variance);
    }
    
    public void set_variance(double variance) {
        this.variance = variance;
    }
    
    public void computeExpectedReturnRate() throws DemoException {
        this.expectedReturnRate = mean / get_dTime() + 0.5 * volatility2;
    }
    
    public void computeVolatility() throws DemoException {
        if (this.variance == Double.NaN) throw new DemoException("Variable variance is not defined!");
        this.volatility2 = variance / get_dTime();
        this.volatility = Math.sqrt(volatility2);
    }
    
    public void computeMean() throws DemoException {
        if (this.nPathValue == 0) throw new DemoException("Variable nPathValue is undefined!");
        this.mean = 0.0;
        for (int i = 1; i < pathValue.length; i++) {
            mean += pathValue[i];
        }
        this.mean /= ((double)(nPathValue - 1.0));
    }
    
    public void computeVariance() throws DemoException {
        if (this.mean == Double.NaN || this.nPathValue == 0) throw new DemoException("Variable mean and/or nPathValue are undefined!");
        this.variance = 0.0;
        for (int i = 1; i < pathValue.length; i++) {
            variance += (pathValue[i] - mean) * (pathValue[i] - mean);
        }
        this.variance /= ((double)(nPathValue - 1.0));
    }
    
    public void estimatePath() throws DemoException {
        computeMean();
        computeVariance();
        computeExpectedReturnRate();
        computeVolatility();
    }
    
    public void dbgDumpFields() {
        super.dbgDumpFields();
    }
}
