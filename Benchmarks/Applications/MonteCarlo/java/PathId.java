
import EDU.oswego.cs.dl.util.concurrent.*;

public class PathId extends Universal {
    public static boolean DEBUG = true;
    protected static String prompt = "PathId> ";
    private String name;
    private int startDate = 0;
    private int endDate = 0;
    private double dTime = Double.NaN;
    
    public PathId() {
        super();
        set_prompt(prompt);
        set_DEBUG(DEBUG);
    }
    
    public PathId(String name) {
        super();
        set_prompt(prompt);
        set_DEBUG(DEBUG);
        this.name = name;
    }
    
    public String get_name() throws DemoException {
        if (this.name == null) throw new DemoException("Variable name is undefined!");
        return (this.name);
    }
    
    public void set_name(String name) {
        this.name = name;
    }
    
    public int get_startDate() throws DemoException {
        if (this.startDate == 0) throw new DemoException("Variable startDate is undefined!");
        return (this.startDate);
    }
    
    public void set_startDate(int startDate) {
        this.startDate = startDate;
    }
    
    public int get_endDate() throws DemoException {
        if (this.endDate == 0) throw new DemoException("Variable endDate is undefined!");
        return (this.endDate);
    }
    
    public void set_endDate(int endDate) {
        this.endDate = endDate;
    }
    
    public double get_dTime() throws DemoException {
        if (this.dTime == Double.NaN) throw new DemoException("Variable dTime is undefined!");
        return (this.dTime);
    }
    
    public void set_dTime(double dTime) {
        this.dTime = dTime;
    }
    
    public void copyInstanceVariables(PathId obj) throws DemoException {
        this.name = obj.get_name();
        this.startDate = obj.get_startDate();
        this.endDate = obj.get_endDate();
        this.dTime = obj.get_dTime();
    }
    
    public void dbgDumpFields() {
    }
}
