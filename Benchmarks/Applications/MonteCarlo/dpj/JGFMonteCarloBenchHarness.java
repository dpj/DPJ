
public class JGFMonteCarloBenchHarness extends Harness {
	
	public JGFMonteCarloBenchHarness(String[] args) {
		super("JGFMonteCarloBench", args);
	}
		
	@Override
	public void runTest() {
		
	}

	@Override
	public void runWork() {
	  JGFMonteCarloBench mc = new JGFMonteCarloBench();
	  mc.JGFrun(size);
	}
	
	 public static void main(String[] args) {
		JGFMonteCarloBenchHarness mc = new JGFMonteCarloBenchHarness(args);
		mc.run();
	}

}
