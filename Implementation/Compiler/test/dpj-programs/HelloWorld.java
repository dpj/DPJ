/**
 * A "Hello, world" program which tests desugaring in javac.
 * 
 * @author Jeff Overbey
 */
public class HelloWorld {
    public static void main(final String[] args) {
	new Thread() {
	    public void run() {
		System.out.println("Hello, DPJ!");
	    }
	}.run();
    }
}
