
import EDU.oswego.cs.dl.util.concurrent.*;

public class CallAppDemo {
    
    public CallAppDemo() {
        super();
    }
    public int size;
    int[] datasizes = {10000, 60000};
    int[] input = new int[2];
    AppDemo ap = null;
    
    public void initialise() {
        input[0] = 1000;
        input[1] = datasizes[size];
        String dirName = "Data";
        String filename = "hitData";
        ap = new AppDemo(dirName, filename, (input[0]), (input[1]));
        ap.initParallel();
    }
    
    public void runiters() {
        ap.runParallel();
    }
    
    public void presults() {
        ap.processParallel();
    }
}
