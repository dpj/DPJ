
import EDU.oswego.cs.dl.util.concurrent.*;

public class DemoException extends java.lang.Exception {
    public static boolean DEBUG = true;
    
    public DemoException() {
        super();
        if (DEBUG) {
            printStackTrace();
        }
    }
    
    public DemoException(String s) {
        super(s);
        if (DEBUG) {
            printStackTrace();
        }
    }
    
    public DemoException(int ierr) {
        super(String.valueOf(ierr));
        if (DEBUG) {
            printStackTrace();
        }
    }
}
