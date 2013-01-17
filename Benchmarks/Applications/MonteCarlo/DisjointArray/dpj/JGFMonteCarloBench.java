 

import java.io.*;

// note for DPJ conversion:
// no particular data structure shared


public class JGFMonteCarloBench extends CallAppDemo {

  public void JGFsetsize(int size){
    this.size = size;
  }

  public void JGFinitialise(){
      initialise();
  }

  public void JGFapplication(){ 
    JGFInstrumentor.startTimer("Section3:MonteCarlo:Run");  
    runiters();
    JGFInstrumentor.stopTimer("Section3:MonteCarlo:Run");  
    presults();
  } 


  public void JGFvalidate(){
   double refval[] = {-0.0333976656762814,-0.03215796752868655};
   double dev = Math.abs(AppDemo.JGFavgExpectedReturnRateMC - refval[size]);
   if (dev > 1.0e-12 ){
     System.out.println("Validation failed");
     System.out.println(" expectedReturnRate= " + AppDemo.JGFavgExpectedReturnRateMC + "  " + dev + "  " + size);
    }
  }

  public void JGFtidyup(){    
    System.gc();
  }


  public void JGFrun(int size){
    JGFInstrumentor.addTimer("Section3:MonteCarlo:Total", "Solutions",size);
    JGFInstrumentor.addTimer("Section3:MonteCarlo:Run", "Samples",size);

    JGFsetsize(size); 

    JGFInstrumentor.startTimer("Section3:MonteCarlo:Total");

    JGFinitialise(); 
    JGFapplication(); 
    JGFvalidate(); 
    JGFtidyup(); 

    JGFInstrumentor.stopTimer("Section3:MonteCarlo:Total");

    JGFInstrumentor.addOpsToTimer("Section3:MonteCarlo:Run", (double) input[1] );
    JGFInstrumentor.addOpsToTimer("Section3:MonteCarlo:Total", 1);

    JGFInstrumentor.printTimer("Section3:MonteCarlo:Run"); 
    JGFInstrumentor.printTimer("Section3:MonteCarlo:Total"); 
  }

  // main method for the entire application
  public static void main(String[] args) {
	
	if (args.length != 1) {
		System.err.println("usage: JGFMonteCarloBench <Data Set # (0/1)>");
		System.exit(1);
	}	

	  JGFMonteCarloBench mc = new JGFMonteCarloBench();
	  mc.JGFrun(Integer.parseInt(args[0]));
  }

}
 
