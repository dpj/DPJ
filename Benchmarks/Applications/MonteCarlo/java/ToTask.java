
import EDU.oswego.cs.dl.util.concurrent.*;

public class ToTask implements java.io.Serializable {
    private String header;
    private long randomSeed;
    
    public ToTask(String header, long randomSeed) {
        super();
        this.header = header;
        this.randomSeed = randomSeed;
    }
    
    public String get_header() {
        return (this.header);
    }
    
    public void set_header(String header) {
        this.header = header;
    }
    
    public long get_randomSeed() {
        return (this.randomSeed);
    }
    
    public void set_randomSeed(long randomSeed) {
        this.randomSeed = randomSeed;
    }
}
