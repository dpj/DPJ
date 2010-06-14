
import EDU.oswego.cs.dl.util.concurrent.*;

public class Universal {
    private static boolean UNIVERSAL_DEBUG;
    private boolean DEBUG;
    private String prompt;
    
    public Universal() {
        super();
        this.DEBUG = true;
        this.UNIVERSAL_DEBUG = true;
        this.prompt = "Universal> ";
    }
    
    public boolean get_DEBUG() {
        return (this.DEBUG);
    }
    
    public void set_DEBUG(boolean DEBUG) {
        this.DEBUG = DEBUG;
    }
    
    public boolean get_UNIVERSAL_DEBUG() {
        return (this.UNIVERSAL_DEBUG);
    }
    
    public void set_UNIVERSAL_DEBUG(boolean UNIVERSAL_DEBUG) {
        this.UNIVERSAL_DEBUG = UNIVERSAL_DEBUG;
    }
    
    public String get_prompt() {
        return (this.prompt);
    }
    
    public void set_prompt(String prompt) {
        this.prompt = prompt;
    }
    
    public void dbgPrintln(String s) {
        if (DEBUG || UNIVERSAL_DEBUG) {
            System.out.println("DBG " + prompt + s);
        }
    }
    
    public void dbgPrint(String s) {
        if (DEBUG || UNIVERSAL_DEBUG) {
            System.out.print("DBG " + prompt + s);
        }
    }
    
    public void errPrintln(String s) {
        System.err.println(prompt + s);
    }
    
    public void errPrint(String s) {
        System.err.print(prompt + s);
    }
}
