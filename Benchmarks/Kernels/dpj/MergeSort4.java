import DPJRuntime.*;
import java.util.Random;

/**
 * 4-way split version of merge sort
 */

public class MergeSort4 extends MergeSort {

    public MergeSort4(String[] args) {
        super("MergeSort4",args);
    }

    @Override
    public  <region R1,R2 | R1:* # R2:*>
	void sort(ArraySliceInt<R1> A, ArraySliceInt<R2> B) 
	writes R1 : *, R2 : * 
    {
      if (A.length <= QUICK_SIZE) {
	  quickSort(A);
      } else {
	  
	  int q = A.length/4;

	  ArrayInt<Local> idxs = new ArrayInt<Local>(3);
	  idxs[0] = q;
	  idxs[1] = 2*q;
	  idxs[2] = 3*q;

	  final PartitionInt<R1> A_quarters = 
	      new PartitionInt<R1>(A, idxs);
	  final PartitionInt<R2> B_quarters = 
	      new PartitionInt<R2>(B, idxs);
	  final PartitionInt<R2> B_halves = 
	      new PartitionInt<R2>(B, 2*q);
	  
	  cobegin {
	      sort(A_quarters.get(0), B_quarters.get(0));
	      sort(A_quarters.get(1), B_quarters.get(1));
	      sort(A_quarters.get(2), B_quarters.get(2));
	      sort(A_quarters.get(3), B_quarters.get(3));
	  }
	  
	  cobegin {
	      merge(A_quarters.get(0), 
		    A_quarters.get(1), 
		    B_halves.get(0));
	      merge(A_quarters.get(2), 
		    A_quarters.get(3), 
		    B_halves.get(1));
	  }
	  
	  merge(B_halves.get(0), 
		B_halves.get(1), 
		A);
      }
    }
  
    public static void main(String[] args) {
        MergeSort4 ms = new MergeSort4(args);
        ms.run();
    }

}

