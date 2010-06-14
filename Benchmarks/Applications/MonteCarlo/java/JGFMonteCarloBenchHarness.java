
import EDU.oswego.cs.dl.util.concurrent.*;

public class JGFMonteCarloBenchHarness extends Harness {
    private static String[] args;
    
    public JGFMonteCarloBenchHarness(String[] args) {
        super("JGFMonteCarloBench", args);
    }
    
    @Override()
    public void runTest() {
    }
    
    @Override()
    public void runWork() {
        JGFMonteCarloBench mc = new JGFMonteCarloBench();
        mc.JGFrun(size);
    }
    
    public static void __dpj_run() {
        JGFMonteCarloBenchHarness mc = new JGFMonteCarloBenchHarness(args);
        mc.run();
    }
    
    static class __dpj_MainClass extends FJTask {
        
        __dpj_MainClass() {
            super();
        }
        
        public void run() {
            try {
                __dpj_run();
            } catch (Throwable e) {
                System.out.println("DPJVM: Uncaught Runtime Error");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        JGFMonteCarloBenchHarness.args = DPJRuntime.RuntimeOptions.initialize(args);
        new FJTaskRunnerGroup(DPJRuntime.RuntimeOptions.dpjTasks).invoke(new __dpj_MainClass());
    }
}
