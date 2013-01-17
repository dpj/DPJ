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

    protected ArrayInt<Input> input;
    protected ArrayInt<Result> result;

    public MergeSort(String name, String[] args) {
        super(name, args);
    }

    @Override
    public void initialize() {
	input = new ArrayInt<Input>(size);
	result = new ArrayInt<Result>(size);
	for (int i = 0; i < input.length; ++i) {
	    input[i] = i;
	}
	Utils.permuteInt(input);
    }

    @Override
    public void runWork() {
	sort(new ArraySliceInt<Input>(input),
	     new ArraySliceInt<Result>(result));
    }

    @Override
    public void runTest() {
	checkSorted(input,input.length);
    }

    public abstract <region R1,R2 | R1:* # R2:*>
	void sort(ArraySliceInt<R1> A, ArraySliceInt<R2> B);

    protected static <region P>void checkSorted 
	(ArrayInt<P> array, int n)  
    {
	for (int i = 0; i < n - 1; i++) {
	    if (array[i] > array[i+1]) {
		throw new Error("Unsorted at " + i + ": " + 
				array[i] + " / " + array[i+1]);
	    }
	}
    }
    
    protected static <region R1,R2,R3 | R1:* # R3:*, R2:* # R3:*>void 
	merge(ArraySliceInt<R1> A, ArraySliceInt<R2> B, 
	      ArraySliceInt<R3> out) 
	reads R1 : *, R2 : * writes R3 : * 
    {
	
	if (A.length <= MERGE_SIZE) {
	    sequentialMerge(A, B, out);
	} 
	else {
	    int aHalf = A.length >>> 1; /*l33t shifting h4x!!!*/
	    int bSplit = findSplit(A.get(aHalf), B);
	    
	    final PartitionInt<R1> A_split = 
		new PartitionInt<R1>(A, aHalf);
	    final PartitionInt<R2> B_split = 
		new PartitionInt<R2>(B, bSplit);
	    final PartitionInt<R3> out_split = 
		new PartitionInt<R3>(out, aHalf + bSplit);
	    
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
    protected static <region R1,R2,R3 | R1#R3, R2#R3>
	void sequentialMerge(ArraySliceInt<R1> A, 
			     ArraySliceInt<R2> B, 
			     ArraySliceInt<R3> out) 
	reads R1 : *, R2 : * writes R3 : * 
    {
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
	findSplit(int value, ArraySliceInt<P> B) 
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
	quickSort(ArraySliceInt<R> slice) writes R : * {
	int lo = 0;
	int hi = slice.length-1;
	// If under threshold, use insertion sort
	if (hi-lo+1l <= INSERTION_SIZE) {
	    for (int i = lo + 1; i <= hi; i++) {
		int t = slice.get(i);
		int j = i - 1;
		while (j >= lo && slice.get(j) > t) {
		    slice.put(j+1, slice.get(j));
		    --j;
		}
		slice.put(j+1, t);
	    }
	    return;
	}
	
	//  Use median-of-three(lo, mid, hi) to pick a partition. 
	//  Also swap them into relative order while we are at it.
	
	int mid = (lo + hi) >>> 1;
	
	if (slice.get(lo) > slice.get(mid)) {
	    int t = slice.get(lo); slice.put(lo, slice.get(mid)); 
	    slice.put(lo, slice.get(mid)); slice.put(mid, t);
	}
	if (slice.get(mid) > slice.get(hi)) {
	    int t = slice.get(mid); slice.put(mid, slice.get(hi)); 
	    slice.put(hi, t);
	    
	    if (slice.get(lo) > slice.get(mid)) {
		t = slice.get(lo); slice.put(lo, slice.get(mid));
		slice.put(mid, t);
	    }
	    
	}
	
	int left = lo+1;           // start one past lo since already handled lo
	int right = hi-1;          // similarly
	
	int partition = slice.get(mid);
	
	for (;;) {
	    
	    while (slice.get(right) > partition)
		--right;
	    
	    while (left < right && slice.get(left) <= partition) 
		++left;
	    
	    if (left < right) {
		int t = slice.get(left); 
		slice.put(left, slice.get(right));
		slice.put(right, t);
		--right;
	    }
	    else break;
	    
	}
	
	quickSort(slice.subslice(lo, left+1));
	quickSort(slice.subslice(left+1, hi-left));
	
    }
    
}

