

/**
  * Wrapper code to invoke the Application demonstrator.
  *
  * @author H W Yau
  * @version $Revision: 1.19 $ $Date: 1999/02/16 19:10:02 $
  */

// initialize and run the application and process the results
// no particular data structure which can be shared

public class CallAppDemo {
    public int size;
    int datasizes[] = {10000,60000};
    int input[] = new int[2];
    AppDemo ap = null;

    public void initialise () {
    	// # of time steps
      input[0] = 1000;
      // data size
      input[1] = datasizes[size];

      String dirName="Data";
      String filename="hitData";
      ap = new AppDemo(dirName, filename, (input[0]),(input[1]));
      //ap.initSerial();
      ap.initParallel();
    }

    public void runiters () {
      //ap.runSerial();
    	ap.runParallel();
    }
    public void presults () {
      //ap.processSerial();
      ap.processParallel();
    }

}
