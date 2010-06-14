import DPJRuntime.*;
import java.util.Random;

/**
 * Sample sort program adapted from a demo in
 * <A href="http://supertech.lcs.mit.edu/cilk/"> Cilk</A> and
 * <A href="http://www.cs.utexas.edu/users/hood/"> Hood</A>.
 *
 * There are two versions of MergeSort here: One that splits the array
 * into four pieces at each recursive step, and one that splits the
 * array into eight pieces.  This abstract class represents the common
 * elements of both versions.
 * 
 **/

public abstract class MergeSort extends Harness {

    region Input, Result;

    protected int[]<Input> input;
    protected int[]<Result> result;

    public MergeSort(String name, String[] args) {
        super(name, args);
    }

    @Override
    public void initialize() {
	input = new int[size]<Input>;
	result = new int[size]<Result>;
	for (int i = 0; i < input.length; ++i) {
	    input[i] = i;
	}
	DPJUtils.permuteInt(input);
    }

    @Override
    public void runWork() {
	sort(new DPJArrayInt<Input>(input),
	     new DPJArrayInt<Result>(result));
    }

    @Override
    public void runTest() {
	checkSorted(input,input.length);
    }

    public abstract <region P1,P2 | P1:* # P2:*>
	void sort(DPJArrayInt<P1> A, DPJArrayInt<P2> B);

    protected static <region P>void checkSorted (int[]<P> anArray, int n)  {
	for (int i = 0; i < n - 1; i++) {
	    if (anArray[i] > anArray[i+1]) {
		throw new Error("Unsorted at " + i + ": " + 
				anArray[i] + " / " + anArray[i+1]);
	    }
	}
    }
    
    protected static <region P1,P2,P3 | P1:* # P3:*, P2:* # P3:*>void 
	merge(DPJArrayInt<P1> A, DPJArrayInt<P2> B, DPJArrayInt<P3> out) 
	reads P1 : *, P2 : * writes P3 : * {
	
	if (A.length <= MERGE_SIZE) {
	    sequentialMerge(A, B, out);
	} else {
	    int aHalf = A.length >>> 1; /*l33t shifting h4x!!!*/
	    int bSplit = findSplit(A.get(aHalf), B);
	    
	    final DPJPartitionInt<P1> A_split = 
		new DPJPartitionInt<P1>(A, aHalf);
	    final DPJPartitionInt<P2> B_split = 
		new DPJPartitionInt<P2>(B, bSplit);
	    final DPJPartitionInt<P3> out_split = 
		new DPJPartitionInt<P3>(out, aHalf + bSplit);
	    
	    cobegin {
		merge(A_split.get(0),
		      B_split.get(0), 
		      out_split.get(0));
		merge(A_split.get(1),
		      B_split.get(1), 
		      out_split.get(1));
	    }
	}
    }

    /** A standard sequential merge **/
    protected static <region P1,P2,P3 | P1#P3, P2#P3>
	void sequentialMerge(DPJArrayInt<P1> A, 
			     DPJArrayInt<P2> B, 
			     DPJArrayInt<P3> out) 
	reads P1 : *, P2 : * writes P3 : * {
	int a = 0;
	int aFence = A.length;
	int b = 0;
	int bFence = B.length;
	int k = 0;
	
	while (a < aFence && b < bFence) {
	    if (A.get(a) < B.get(b)) 
		out.put(k++, A.get(a++));
	    else 
		out.put(k++, B.get(b++));
	}
	
	while (a < aFence) out.put(k++, A.get(a++));
	while (b < bFence) out.put(k++, B.get(b++));
    }
    
    protected static <region P>int 
	findSplit(int value, DPJArrayInt<P> B) 
	reads P {
	int low = 0;
	int high = B.length;
	while (low < high) {
	    int middle = low + ((high - low) >>> 1);
	    if (value <= B.get(middle))
		high = middle;
	    else
		low = middle + 1;
	}
	return high;
    }

    
  
    /* Threshold values */
    
    // Cutoff for when to do sequential versus parallel merges 
    public static final int MERGE_SIZE = 2048;
    
    // Cutoff for when to do sequential quicksort versus parallel mergesort
    public static final int QUICK_SIZE = 2048;
    
    // Cutoff for when to use insertion-sort instead of quicksort
    public static final int INSERTION_SIZE = 2000;
    
    
    
    /** A standard sequential quicksort **/
    protected static <region R>void
	quickSort(DPJArrayInt<R> array) writes R : * {
	int lo = 0;
	int hi = array.length-1;
	// If under threshold, use insertion sort
	DPJArrayInt<R> arr = array;
	if (hi-lo+1l <= INSERTION_SIZE) {
	    for (int i = lo + 1; i <= hi; i++) {
		int t = arr.get(i);
		int j = i - 1;
		while (j >= lo && arr.get(j) > t) {
		    arr.put(j+1, arr.get(j));
		    --j;
		}
		arr.put(j+1, t);
	    }
	    return;
	}
	
	//  Use median-of-three(lo, mid, hi) to pick a partition. 
	//  Also swap them into relative order while we are at it.
	
	int mid = (lo + hi) >>> 1;
	
	if (arr.get(lo) > arr.get(mid)) {
	    int t = arr.get(lo); arr.put(lo, arr.get(mid)); 
	    arr.put(lo, arr.get(mid)); arr.put(mid, t);
	}
	if (arr.get(mid) > arr.get(hi)) {
	    int t = arr.get(mid); arr.put(mid, arr.get(hi)); 
	    arr.put(hi, t);
	    
	    if (arr.get(lo) > arr.get(mid)) {
		t = arr.get(lo); arr.put(lo, arr.get(mid));
		arr.put(mid, t);
	    }
	    
	}
	
	int left = lo+1;           // start one past lo since already handled lo
	int right = hi-1;          // similarly
	
	int partition = arr.get(mid);
	
	for (;;) {
	    
	    while (arr.get(right) > partition)
		--right;
	    
	    while (left < right && arr.get(left) <= partition) 
		++left;
	    
	    if (left < right) {
		int t = arr.get(left); 
		arr.put(left, arr.get(right));
		arr.put(right, t);
		--right;
	    }
	    else break;
	    
	}
	
	quickSort(array.subarray(lo, left+1));
	quickSort(array.subarray(left+1, hi-left));
	
    }
    
}

