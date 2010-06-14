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

public class JGFCryptBenchSize extends Harness { 

  public static int nthreads;
  JGFCryptBench cb;

  public JGFCryptBenchSize(String args[])
  {
    super("JGFCryptBenchSize", args);
    //dummy nthreads
    nthreads = 1;
    cb = new JGFCryptBench(nthreads); 
  }

  public static void main(String args[]){

    JGFCryptBenchSize jgf = new JGFCryptBenchSize(args);
    jgf.run();
  }
  public void initialize()
  {
    JGFInstrumentor.printHeader(2,size,nthreads);
    cb.JGFsetsize(size);
    cb.JGFinitialise();
  }
  public void runWork()
  {
    cb.JGFkernel();
  }
  public void runCleanup()
  {
  }
  public void runTest()
  {
    cb.JGFvalidate();
  }
}
