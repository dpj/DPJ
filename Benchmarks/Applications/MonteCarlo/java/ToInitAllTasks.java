
import EDU.oswego.cs.dl.util.concurrent.*;

public class ToInitAllTasks implements java.io.Serializable {
    private String header;
    private String name;
    private int startDate;
    private int endDate;
    private double dTime;
    private int returnDefinition;
    private double expectedReturnRate;
    private double volatility;
    private int nTimeSteps;
    private double pathStartValue;
    
    public ToInitAllTasks(String header, String name, int startDate, int endDate, double dTime, int returnDefinition, double expectedReturnRate, double volatility, double pathStartValue) {
        super();
        this.header = header;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.dTime = dTime;
        this.returnDefinition = returnDefinition;
        this.expectedReturnRate = expectedReturnRate;
        this.volatility = volatility;
        this.nTimeSteps = nTimeSteps;
        this.pathStartValue = pathStartValue;
    }
    
    public ToInitAllTasks(ReturnPath obj, int nTimeSteps, double pathStartValue) throws DemoException {
        super();
        this.name = obj.get_name();
        this.startDate = obj.get_startDate();
        this.endDate = obj.get_endDate();
        this.dTime = obj.get_dTime();
        this.returnDefinition = obj.get_returnDefinition();
        this.expectedReturnRate = obj.get_expectedReturnRate();
        this.volatility = obj.get_volatility();
        this.nTimeSteps = nTimeSteps;
        this.pathStartValue = pathStartValue;
    }
    
    public String get_header() {
        return (this.header);
    }
    
    public void set_header(String header) {
        this.header = header;
    }
    
    public String get_name() {
        return (this.name);
    }
    
    public void set_name(String name) {
        this.name = name;
    }
    
    public int get_startDate() {
        return (this.startDate);
    }
    
    public void set_startDate(int startDate) {
        this.startDate = startDate;
    }
    
    public int get_endDate() {
        return (this.endDate);
    }
    
    public void set_endDate(int endDate) {
        this.endDate = endDate;
    }
    
    public double get_dTime() {
        return (this.dTime);
    }
    
    public void set_dTime(double dTime) {
        this.dTime = dTime;
    }
    
    public int get_returnDefinition() {
        return (this.returnDefinition);
    }
    
    public void set_returnDefinition(int returnDefinition) {
        this.returnDefinition = returnDefinition;
    }
    
    public double get_expectedReturnRate() {
        return (this.expectedReturnRate);
    }
    
    public void set_expectedReturnRate(double expectedReturnRate) {
        this.expectedReturnRate = expectedReturnRate;
    }
    
    public double get_volatility() {
        return (this.volatility);
    }
    
    public void set_volatility(double volatility) {
        this.volatility = volatility;
    }
    
    public int get_nTimeSteps() {
        return (this.nTimeSteps);
    }
    
    public void set_nTimeSteps(int nTimeSteps) {
        this.nTimeSteps = nTimeSteps;
    }
    
    public double get_pathStartValue() {
        return (this.pathStartValue);
    }
    
    public void set_pathStartValue(double pathStartValue) {
        this.pathStartValue = pathStartValue;
    }
}
