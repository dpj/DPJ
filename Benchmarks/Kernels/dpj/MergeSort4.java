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
    public  <region P1,P2 | P1:* # P2:*>
	void sort(DPJArrayInt<P1> A, DPJArrayInt<P2> B) 
	writes P1 : *, P2 : * {
      if (A.length <= QUICK_SIZE) {
	  quickSort(A);
      } else {
	  
	  int q = A.length/4;

	  int[]<Local> idxs = new int[3]<Local>;
	  idxs[0] = q;
	  idxs[1] = 2*q;
	  idxs[2] = 3*q;

	  final DPJPartitionInt<P1> A_quarters = 
	      new DPJPartitionInt<P1>(A, idxs);
	  final DPJPartitionInt<P2> B_quarters = 
	      new DPJPartitionInt<P2>(B, idxs);
	  final DPJPartitionInt<P2> B_halves = 
	      new DPJPartitionInt<P2>(B, 2*q);
	  
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

