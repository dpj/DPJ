/**************************************************************************
*                                                                         *
*         Java Grande Forum Benchmark Suite - Thread Version 1.0          *
*                                                                         *
*                            produced by                                  *
*                                                                         *
*                  Java Grande Benchmarking Project                       *
*                                                                         *
*                                at                                       *
*                                                                         *
*                Edinburgh Parallel Computing Centre                      *
*                                                                         * 
*                email: epcc-javagrande@epcc.ed.ac.uk                     *
*                                                                         *
*                                                                         *
*      This version copyright (c) The University of Edinburgh, 2001.      *
*                         All rights reserved.                            *
*                                                                         *
**************************************************************************/


//import crypt.*;
import jgfutil.*; 
import DPJRuntime.*;

public class JGFCryptBenchSizeC{ 

  public static int nthreads;

  public static void main(String args[]){

      nthreads = RuntimeState.dpjNumThreads;

      JGFInstrumentor.printHeader(2,2,nthreads);

      JGFCryptBench cb = new JGFCryptBench(nthreads); 
      cb.JGFrun(2);
 
  }
}


