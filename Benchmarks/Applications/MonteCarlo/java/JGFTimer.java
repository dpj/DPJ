
import EDU.oswego.cs.dl.util.concurrent.*;

public class JGFTimer {
    public String name;
    public String opname;
    public double time;
    public double opcount;
    public long calls;
    public int size = -1;
    private long start_time;
    private boolean on;
    
    public JGFTimer(String name, String opname) {
        super();
        this.name = name;
        this.opname = opname;
        reset();
    }
    
    public JGFTimer(String name, String opname, int size) {
        super();
        this.name = name;
        this.opname = opname;
        this.size = size;
        reset();
    }
    
    public JGFTimer(String name) {
        this(name, "");
    }
    
    public void start() {
        if (on) System.out.println("Warning timer " + name + " was already turned on");
        on = true;
        start_time = System.currentTimeMillis();
    }
    
    public void stop() {
        time += (double)(System.currentTimeMillis() - start_time) / 1000.0;
        if (!on) System.out.println("Warning timer " + name + " wasn\'t turned on");
        calls++;
        on = false;
    }
    
    public void addops(double count) {
        opcount += count;
    }
    
    public void reset() {
        time = 0.0;
        calls = 0;
        opcount = 0;
        on = false;
    }
    
    public double perf() {
        return opcount / time;
    }
    
    public void longprint() {
        System.out.println("Timer            Calls         Time(s)       Performance(" + opname + "/s)");
        System.out.println(name + "           " + calls + "           " + time + "        " + this.perf());
    }
    
    public void print() {
        if (opname.equals("")) {
            System.out.println(name + "   " + time + " (s)");
        } else {
            switch (size) {
            case 0: 
                System.out.println(name + ":SizeA" + "\t" + time + " (s) \t " + (float)this.perf() + "\t" + " (" + opname + "/s)");
                break;
            
            case 1: 
                System.out.println(name + ":SizeB" + "\t" + time + " (s) \t " + (float)this.perf() + "\t" + " (" + opname + "/s)");
                break;
            
            case 2: 
                System.out.println(name + ":SizeC" + "\t" + time + " (s) \t " + (float)this.perf() + "\t" + " (" + opname + "/s)");
                break;
            
            default: 
                System.out.println(name + "\t" + time + " (s) \t " + (float)this.perf() + "\t" + " (" + opname + "/s)");
                break;
            
            }
        }
    }
    
    public void printperf() {
        String name;
        name = this.name;
        while (name.length() < 40) name = name + " ";
        System.out.println(name + "\t" + (float)this.perf() + "\t" + " (" + opname + "/s)");
    }
}
