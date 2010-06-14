
import jsr166y.forkjoin.*;
import java.io.*;
import java.util.*;

public class RatePath extends PathId {
    public static boolean DEBUG = true;
    protected static String prompt = "RatePath> ";
    public static int DATUMFIELD = 4;
    public static final int MINIMUMDATE = 19000101;
    public static final double EPSILON = 10.0 * Double.MIN_VALUE;
    private double[] pathValue;
    private int[] pathDate;
    private int nAcceptedPathValue = 0;
    
    public RatePath(String filename) throws DemoException {
        super();
        set_prompt(prompt);
        set_DEBUG(DEBUG);
        readRatesFile(null, filename);
    }
    
    public RatePath(String dirName, String filename) throws DemoException {
        super();
        set_prompt(prompt);
        set_DEBUG(DEBUG);
        readRatesFile(dirName, filename);
    }
    
    public RatePath(double[] pathValue, String name, int startDate, int endDate, double dTime) {
        super();
        set_name(name);
        set_startDate(startDate);
        set_endDate(endDate);
        set_dTime(dTime);
        set_prompt(prompt);
        set_DEBUG(DEBUG);
        this.pathValue = pathValue;
        this.nAcceptedPathValue = pathValue.length;
    }
    
    public RatePath(MonteCarloPath mc) throws DemoException {
        super();
        set_name(mc.get_name());
        set_startDate(mc.get_startDate());
        set_endDate(mc.get_endDate());
        set_dTime(mc.get_dTime());
        pathValue = mc.get_pathValue();
        nAcceptedPathValue = mc.get_nTimeSteps();
        pathDate = new int[nAcceptedPathValue];
    }
    
    public RatePath(int pathValueLength, String name, int startDate, int endDate, double dTime) {
        super();
        set_name(name);
        set_startDate(startDate);
        set_endDate(endDate);
        set_dTime(dTime);
        set_prompt(prompt);
        set_DEBUG(DEBUG);
        this.pathValue = new double[pathValueLength];
        this.nAcceptedPathValue = pathValue.length;
    }
    
    public void inc_pathValue(double[] operandPath) {
        for (int i = 0; i < pathValue.length; i++) {
            pathValue[i] += operandPath[i];
        }
    }
    
    public void inc_pathValue(double scale) throws DemoException {
        if (pathValue == null) throw new DemoException("Variable pathValue is undefined!");
        for (int i = 0; i < pathValue.length; i++) {
            pathValue[i] *= scale;
        }
    }
    
    public double[] get_pathValue() {
        return (this.pathValue);
    }
    
    public void set_pathValue(double[] pathValue) {
        this.pathValue = pathValue;
    }
    
    public int[] get_pathDate() throws DemoException {
        if (this.pathDate == null) throw new DemoException("Variable pathDate is undefined!");
        return (this.pathDate);
    }
    
    public void set_pathDate(int[] pathDate) {
        this.pathDate = pathDate;
    }
    
    public double getEndPathValue() {
        return (getPathValue(pathValue.length - 1));
    }
    
    public double getPathValue(int index) {
        return (pathValue[index]);
    }
    
    public ReturnPath getReturnCompounded() throws DemoException {
        if (pathValue == null || nAcceptedPathValue == 0) {
            throw new DemoException("The Rate Path has not been defined!");
        }
        double[] returnPathValue = new double[nAcceptedPathValue];
        returnPathValue[0] = 0.0;
        try {
            for (int i = 1; i < nAcceptedPathValue; i++) {
                returnPathValue[i] = Math.log(pathValue[i] / pathValue[i - 1]);
            }
        } catch (ArithmeticException aex) {
            throw new DemoException("Error in getReturnLogarithm:" + aex.toString());
        }
        ReturnPath rPath = new ReturnPath(returnPathValue, nAcceptedPathValue, ReturnPath.COMPOUNDED);
        rPath.copyInstanceVariables(this);
        rPath.estimatePath();
        return (rPath);
    }
    
    public ReturnPath getReturnNonCompounded() throws DemoException {
        if (pathValue == null || nAcceptedPathValue == 0) {
            throw new DemoException("The Rate Path has not been defined!");
        }
        double[] returnPathValue = new double[nAcceptedPathValue];
        returnPathValue[0] = 0.0;
        try {
            for (int i = 1; i < nAcceptedPathValue; i++) {
                returnPathValue[i] = (pathValue[i] - pathValue[i - 1]) / pathValue[i];
            }
        } catch (ArithmeticException aex) {
            throw new DemoException("Error in getReturnPercentage:" + aex.toString());
        }
        ReturnPath rPath = new ReturnPath(returnPathValue, nAcceptedPathValue, ReturnPath.NONCOMPOUNDED);
        rPath.copyInstanceVariables(this);
        rPath.estimatePath();
        return (rPath);
    }
    
    private void readRatesFile(String dirName, String filename) throws DemoException {
        java.io.File ratesFile = new File(dirName, filename);
        java.io.BufferedReader in;
        if (!ratesFile.canRead()) {
            throw new DemoException("Cannot read the file " + ratesFile.toString());
        }
        try {
            in = new BufferedReader(new FileReader(ratesFile));
        } catch (FileNotFoundException fnfex) {
            throw new DemoException(fnfex.toString());
        }
        int iLine = 0;
        int initNlines = 100;
        int nLines = 0;
        String aLine;
        java.util.Vector allLines = new Vector(initNlines);
        try {
            while ((aLine = in.readLine()) != null) {
                iLine++;
                allLines.addElement(aLine);
            }
        } catch (IOException ioex) {
            throw new DemoException("Problem reading data from the file " + ioex.toString());
        }
        nLines = iLine;
        this.pathValue = new double[nLines];
        this.pathDate = new int[nLines];
        nAcceptedPathValue = 0;
        iLine = 0;
        for (java.util.Enumeration enum_ = allLines.elements(); enum_.hasMoreElements(); ) {
            aLine = (String)enum_.nextElement();
            String[] field = Utilities.splitString(",", aLine);
            int aDate = Integer.parseInt("19" + field[0]);
            double aPathValue = Double.valueOf(field[DATUMFIELD]).doubleValue();
            if ((aDate <= MINIMUMDATE) || (Math.abs(aPathValue) < EPSILON)) {
                dbgPrintln("Skipped erroneous data in " + filename + " indexed by date=" + field[0] + ".");
            } else {
                pathDate[iLine] = aDate;
                pathValue[iLine] = aPathValue;
                iLine++;
            }
        }
        nAcceptedPathValue = iLine;
        set_name(ratesFile.getName());
        set_startDate(pathDate[0]);
        set_endDate(pathDate[nAcceptedPathValue - 1]);
        set_dTime((double)(1.0 / 365.0));
    }
}
