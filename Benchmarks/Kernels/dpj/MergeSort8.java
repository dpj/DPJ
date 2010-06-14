import DPJRuntime.*;
import java.util.Random;

/**
 * 8-way split version of merge sort
 */

public class MergeSort8 extends MergeSort {

    public MergeSort8(String[] args) {
        super("MergeSort8",args);
    }

    @Override
    public <region P1,P2 | P1:* # P2:*>
	void sort(DPJArrayInt<P1> A, DPJArrayInt<P2> B) {
	sort(A, B, true);
    }

    public <region P1,P2 | P1:* # P2:*>
	boolean sort(DPJArrayInt<P1> A, DPJArrayInt<P2> B, 
		     boolean parity) 
	writes P1:*, P2:* {
	if (A.length <= QUICK_SIZE) {
	    if(parity)
		quickSort(A);
	    else
		quickSort(B);
	    return parity;
	} else {
	    
	    int q = A.length/8;
	    
	    int[]<Local> idxs = new int[7]<Local>;
	    idxs[0] = q;
	    idxs[1] = 2*q;
	    idxs[2] = 3*q;
	    idxs[3] = 4*q;
	    idxs[4] = 5*q;
	    idxs[5] = 6*q;
	    idxs[6] = 7*q;
	    
	    int[]<Local> quart_idxs = new int[3]<Local>;
	    quart_idxs[0] = 2*q;
	    quart_idxs[1] = 4*q;
	    quart_idxs[2] = 6*q;
	    
	    final DPJPartitionInt<P1> A_eighths = 
		new DPJPartitionInt<P1>(A, idxs);
	    final DPJPartitionInt<P2> B_eighths = 
		new DPJPartitionInt<P2>(B, idxs);
	    final DPJPartitionInt<P1> A_quarters = 
		new DPJPartitionInt<P1>(A, quart_idxs);
	    final DPJPartitionInt<P1> A_halves = 
		new DPJPartitionInt<P1>(A, 4*q);
	    final DPJPartitionInt<P2> B_quarters = 
		new DPJPartitionInt<P2>(B, quart_idxs);
	    final DPJPartitionInt<P2> B_halves = 
		new DPJPartitionInt<P2>(B, 4*q);
	    
	    boolean subparity;
	    
	    cobegin {
		subparity = 
		    sort(A_eighths.get(0), 
			   B_eighths.get(0),
			   parity);
		sort(A_eighths.get(1), 
		       B_eighths.get(1),
		       parity);
		sort(A_eighths.get(2), 
		       B_eighths.get(2),
		       parity);
		sort(A_eighths.get(3), 
		       B_eighths.get(3),
		       parity);
		sort(A_eighths.get(4), 
		       B_eighths.get(4),
		       parity);
		sort(A_eighths.get(5), 
		       B_eighths.get(5),
		       parity);
		sort(A_eighths.get(6), 
		       B_eighths.get(6),
		       parity);
		sort(A_eighths.get(7), 
		       B_eighths.get(7),
		       parity);
	    }

	    if (subparity) {
		sort_quarters(A_eighths, B_quarters);
		sort_halves(A_quarters, B_halves);
		merge(A_halves.get(0), A_halves.get(1), B);
	    } else {
		sort_quarters(B_eighths, A_quarters);
		sort_halves(B_quarters, A_halves);
		merge(B_halves.get(0), B_halves.get(1), A);
	    }

	    return !subparity;
	}
    }

    private static <region P1,P2 | P1:* # P2:*>void 
	sort_quarters(final DPJPartitionInt<P1> A_eighths,
			final DPJPartitionInt<P2> B_quarters) 
	reads P1:* writes P2:* {
	cobegin {
	    merge(A_eighths.get(0), 
		  A_eighths.get(1), 
		  B_quarters.get(0));
	    merge(A_eighths.get(2), 
		  A_eighths.get(3), 
		  B_quarters.get(1));
	    merge(A_eighths.get(4), 
		  A_eighths.get(5), 
		  B_quarters.get(2));
	    merge(A_eighths.get(6), 
		  A_eighths.get(7), 
		  B_quarters.get(3));
	}
    }
  
    private static <region P1,P2 | P1:*#P2:*>void
	sort_halves(final DPJPartitionInt<P1> quarters,
		      final DPJPartitionInt<P2> halves) 
	reads P1:* writes P2:* {
	cobegin {
	    merge(quarters.get(0),
		  quarters.get(1),
		  halves.get(0));
	    merge(quarters.get(2), 
		  quarters.get(3), 
		  halves.get(1));
	}
    }

    public static void main(String[] args) {
        MergeSort8 ms = new MergeSort8(args);
        ms.run();
    }

}

