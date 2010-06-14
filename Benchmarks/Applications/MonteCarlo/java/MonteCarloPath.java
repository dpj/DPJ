
import jsr166y.forkjoin.*;
import java.util.*;
import java.io.*;

public class MonteCarloPath extends PathId {
    public static boolean DEBUG = true;
    protected static String prompt = "MonteCarloPath> ";
    public static int DATUMFIELD = RatePath.DATUMFIELD;
    private double[] fluctuations;
    private double[] pathValue;
    private int returnDefinition = 0;
    private double expectedReturnRate = Double.NaN;
    private double volatility = Double.NaN;
    private int nTimeSteps = 0;
    private double pathStartValue = Double.NaN;
    
    public MonteCarloPath() {
        super();
        set_prompt(prompt);
        set_DEBUG(DEBUG);
    }
    
    public MonteCarloPath(ReturnPath returnPath, int nTimeSteps) throws DemoException {
        super();
        copyInstanceVariables(returnPath);
        this.nTimeSteps = nTimeSteps;
        this.pathValue = new double[nTimeSteps];
        this.fluctuations = new double[nTimeSteps];
        set_prompt(prompt);
        set_DEBUG(DEBUG);
    }
    
    public MonteCarloPath(PathId pathId, int returnDefinition, double expectedReturnRate, double volatility, int nTimeSteps) throws DemoException {
        super();
        copyInstanceVariables(pathId);
        this.returnDefinition = returnDefinition;
        this.expectedReturnRate = expectedReturnRate;
        this.volatility = volatility;
        this.nTimeSteps = nTimeSteps;
        this.pathValue = new double[nTimeSteps];
        this.fluctuations = new double[nTimeSteps];
        set_prompt(prompt);
        set_DEBUG(DEBUG);
    }
    
    public MonteCarloPath(String name, int startDate, int endDate, double dTime, int returnDefinition, double expectedReturnRate, double volatility, int nTimeSteps) {
        super();
        set_name(name);
        set_startDate(startDate);
        set_endDate(endDate);
        set_dTime(dTime);
        this.returnDefinition = returnDefinition;
        this.expectedReturnRate = expectedReturnRate;
        this.volatility = volatility;
        this.nTimeSteps = nTimeSteps;
        this.pathValue = new double[nTimeSteps];
        this.fluctuations = new double[nTimeSteps];
        set_prompt(prompt);
        set_DEBUG(DEBUG);
    }
    
    public double[] get_fluctuations() throws DemoException {
        if (this.fluctuations == null) throw new DemoException("Variable fluctuations is undefined!");
        return (this.fluctuations);
    }
    
    public void set_fluctuations(double[] fluctuations) {
        this.fluctuations = fluctuations;
    }
    
    public double[] get_pathValue() throws DemoException {
        if (this.pathValue == null) throw new DemoException("Variable pathValue is undefined!");
        return (this.pathValue);
    }
    
    public void set_pathValue(double[] pathValue) {
        this.pathValue = pathValue;
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
    
    public int get_nTimeSteps() throws DemoException {
        if (this.nTimeSteps == 0) throw new DemoException("Variable nTimeSteps is undefined!");
        return (this.nTimeSteps);
    }
    
    public void set_nTimeSteps(int nTimeSteps) {
        this.nTimeSteps = nTimeSteps;
    }
    
    public double get_pathStartValue() throws DemoException {
        if (this.pathStartValue == Double.NaN) throw new DemoException("Variable pathStartValue is undefined!");
        return (this.pathStartValue);
    }
    
    public void set_pathStartValue(double pathStartValue) {
        this.pathStartValue = pathStartValue;
    }
    
    private void copyInstanceVariables(ReturnPath obj) throws DemoException {
        set_name(obj.get_name());
        set_startDate(obj.get_startDate());
        set_endDate(obj.get_endDate());
        set_dTime(obj.get_dTime());
        this.returnDefinition = obj.get_returnDefinition();
        this.expectedReturnRate = obj.get_expectedReturnRate();
        this.volatility = obj.get_volatility();
    }
    
    public void writeFile(String dirName, String filename) throws DemoException {
        try {
            java.io.File ratesFile = new File(dirName, filename);
            if (ratesFile.exists() && !ratesFile.canWrite()) throw new DemoException("Cannot write to specified filename!");
            java.io.PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(ratesFile)));
            for (int i = 0; i < nTimeSteps; i++) {
                out.print("19990101,");
                for (int j = 1; j < DATUMFIELD; j++) {
                    out.print("0.0000,");
                }
                out.print(pathValue[i] + ",");
                out.println("0.0000,0.0000");
            }
            out.close();
        } catch (java.io.IOException ioex) {
            throw new DemoException(ioex.toString());
        }
    }
    
    public RatePath getRatePath() throws DemoException {
        return (new RatePath(this));
    }
    
    public void computeFluctuationsGaussian(long randomSeed) throws DemoException {
        if (nTimeSteps > fluctuations.length) throw new DemoException("Number of timesteps requested is greater than the allocated array!");
        Random rnd;
        if (randomSeed == -1) {
            rnd = new Random();
        } else {
            rnd = new Random(randomSeed);
        }
        double mean = (expectedReturnRate - 0.5 * volatility * volatility) * get_dTime();
        double sd = volatility * Math.sqrt(get_dTime());
        double gauss;
        double meanGauss = 0.0;
        double variance = 0.0;
        for (int i = 0; i < nTimeSteps; i++) {
            gauss = rnd.nextGaussian();
            meanGauss += gauss;
            variance += (gauss * gauss);
            fluctuations[i] = mean + sd * gauss;
        }
        meanGauss /= (double)nTimeSteps;
        variance /= (double)nTimeSteps;
    }
    
    public void computeFluctuationsGaussian() throws DemoException {
        computeFluctuationsGaussian((long)-1);
    }
    
    public void computePathValue(double startValue) throws DemoException {
        pathValue[0] = startValue;
        if (returnDefinition == ReturnPath.COMPOUNDED || returnDefinition == ReturnPath.NONCOMPOUNDED) {
            for (int i = 1; i < nTimeSteps; i++) {
                pathValue[i] = pathValue[i - 1] * Math.exp(fluctuations[i]);
            }
        } else {
            throw new DemoException("Unknown or undefined update method.");
        }
    }
}
