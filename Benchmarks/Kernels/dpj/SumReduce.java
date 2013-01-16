/**
 * Parallel sum reduction
 * @author Robert L. Bocchino Jr.
 * October 2008
 */

import DPJRuntime.*;

public class SumReduce extends Harness {

    public SumReduce(String[] args) {
	super("SumReduce", args, 2, 3);
	if (args.length == 3)
	    seqLength = Integer.parseInt(args[2]);
    }

    public <region R>int reduce(ArraySliceInt<R> A, 
				int seqLength) 
	reads R 
    {
	if (A.length == 0) return 0;
	if (A.length == 1) return A.get(0);
	int result = 0;
	if (A.length > seqLength) {
	    int tmp1, tmp2;
	    cobegin {
		tmp1 = reduce(A.subslice(0, A.length/2), 
			      seqLength);
		tmp2 = reduce(A.subslice(A.length/2, 
					 A.length - A.length/2), 
			      seqLength);
	    }
	    result = tmp1 + tmp2;
	} else {
	    for (int i = 0; i < A.length; ++i)
		result += A.get(i);
	}
	return result;
    }

    @Override
    public void initialize() {
	A = new ArrayInt(size);
	for (int i = 0; i < A.length; ++i) {
	    A[i] = i;	    
	}
    }

    @Override
    public void runTest() {
	int myResult = 0;
	for (int i = 0; i < A.length; ++i)
	    myResult += A[i];
	assert(result == myResult);
    }

    @Override
    public void runWork() {
	result = reduce(new ArraySliceInt<*>(A), 
			seqLength);
    }

    @Override
    public void usage() {
	System.err.println("Usage:  java " + progName + ".java [mode] [size] [seqsize]");
	System.err.println("mode = TEST, IDEAL, TIME");
	System.err.println("size = problem size (int)");
	System.err.println("seqsize = sequential problem size (int)");
    }
    private int seqLength = 1;
    private ArrayInt A;
    private int result = 0;
    public static void main(String[] args) {
	SumReduce sr = new SumReduce(args);
	sr.run();
    }
}
