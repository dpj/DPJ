package DPJRuntime;

/**
 * <p>The {@code DPJPartition} class represents an array of {@link
 * DPJArray} objects that partition another {@code DPJArray} (called
 * the <i>root array</i>).  Each {@code DPJArray} in the partition is
 * a contiguous subsection of the root array, and all are mutually
 * disjoint.  The {@code DPJArray}s in the partition are called
 * <i>segments</i> of the root array.
 *
 * <p>For example, if the root array has index space {@code [0,10]},
 * then a partition might have segments with index spaces {@code
 * [0,5]} and {@code [6,10]}.
 * 
 * @author Rob Bocchino
 *
 * @param <T> The type of an element of an array in the partition
 * @param <R> The region of a cell of an array in the partition
 */
public class DPJPartition<type T,region R> {

    /**
     * The array being partitioned
     */
    private final DPJArray<T,R> A;

    /**
     * The segments of the partition
     */
    private final DPJArray<T,this:[i]>[]<this:[i]>#i segs;

    /**
     * Number of segments in the partition
     */
    public final int length;

    /**
     * Stride of the segments, for a strided partition.
     */
    private final int stride;

    /**
     * Partitions an array {@code A} into two segments at index {@code
     * idx}.  If {@code A} has {@code n} elements, then the first
     * segment consists of elements {@code 0} through {@code idx-1},
     * and the second segment consists of elements {@code idx} through
     * {@code n-1}.
     *
     * <p>Throws {@code ArrayIndexOutOfBoundsException} if
     * {@code idx} is not in {@code [0,n-1]}.
     *
     * @param A   The array to partition
     * @param idx The partition index
     */
    public DPJPartition(DPJArray<T,R> A, final int idx) 
	writes this:[?] {
	this.A = A;
	this.length = 2;
	this.stride = 0;
	segs = (DPJArray<T,this:[i]>[]<this:[i]>#i) new DPJArray[length]<this:[i]>#i;
	segs[0] = (DPJArray<T,this:[0]>) A.subarray(0, idx);
	segs[1] = (DPJArray<T,this:[1]>) A.subarray(idx, A.length - idx);
    }

    /**
     * Partitions an array {@code A} into two segments at {@code idx},
     * optionally excluding the element at {@code idx}.  If {@code A}
     * has {@code n} elements, then the first segment is always {@code
     * [0,idx-1]}.  The second segment is either {@code [idx,n-1]} (if
     * {@code exclude} is false) or {@code [idx+1,n-1]} (if {@code
     * exclude} is true).
     *
     * <p>Throws {@code ArrayIndexOutOfBoundsException} if
     * {@code idx} is not in {@code [0,n-1]}.
     *
     * @param A       The array to partition
     * @param idx     The partition index
     * @param exclude Whether to exclude the element at
     *                {@code idx} from the segments
     */
    public DPJPartition(DPJArray<T,R> A, final int idx, boolean exclude) 
	writes this:[?] {
	this.A = A;
	this.length = 2;
	this.stride = 0;
	this.segs = (DPJArray<T,this:[i]>[]<this:[i]>#i) new DPJArray[length]<this:[i]>#i;
	segs[0] = (DPJArray<T,this:[0]>) A.subarray(0, idx);
	if (exclude) {
	    segs[1] = (DPJArray<T,this:[1]>) A.subarray(idx + 1, A.length - idx - 1);
	} else {
            segs[1] = (DPJArray<T,this:[1]>) A.subarray(idx, A.length - idx);
	}
    }

    /**
     * Private constructor.  {@code strided} is a dead arg here: we
     * use it to disambiguate this constructor from the other one that
     * takes an array and a stride.
     */
    private DPJPartition(DPJArray<T,R> A, int stride, double strided) pure {
    	this.A = A;
        this.stride = stride;
	this.length = (A.length / stride) + ((A.length % stride == 0) ? 0 : 1);
	this.segs = null;
    }

    /**
     * Creates a partition using stride {@code stride}.  For example,
     * partitioning a 10-element array with stride 2 creates a
     * partition with 5 segments, each of length 2.
     *
     * @param <T> The type of the array to partition
     * @param <R> The region of the array to partition
     * @param A  The array to partition
     * @param stride The stride at which to partition
     * @return A partition of {@code A} with stride {@code stride}
     */
    public static <type T,region R>DPJPartition<T,R> 
      stridedPartition(DPJArray<T,R> A, int stride) pure {
    	return new DPJPartition<T,R>(A, stride, 0.0);
    }

    /**
     * Partitions an array {@code A} into
     * {@code idxs.length+1} segments using the indices in
     * {@code idxs} as the split points.  If {@code A} has
     * index space {@code [0,n-1]}, then the segments are
     * {@code [0,idxs[0]-1]}, {@code [idxs[0],idxs[1]-1]},
     * ..., {@code [idxs[idxs.length-1]-1,n-1]}.
     *
     * Throws {@code ArrayOutOfBoundsException} if the indices
     * are not monotonically increasing, or if any index is out of
     * bounds for {@code A}.
     *
     * @param <RI> The region of array {@code idxs}
     * @param A    The array to partition
     * @param idxs The split points    
     * @return A partition of {@code A} using {@code idxs} as the
     * split points.
     */
    public <region RI>DPJPartition(DPJArray<T,R> A, int[]<RI> idxs) 
	reads RI writes this:[?] {
	this.A = A;
	this.length = idxs.length+1;
	this.segs = (DPJArray<T,this:[i]>[]<this:[i]>#i) 
	    new DPJArray[length]<this:[i]>#i;
	this.stride = 0;
	if (length == 1)
	    segs[0] = (DPJArray<T,this:[0]>) A;
	else {
	    int i = 0, len = 0;
	    segs[0] = (DPJArray<T,this:[0]>) A.subarray(0, idxs[0]);
	    for (i = 1; i < idxs.length; ++i) {
		len = idxs[i] - idxs[i-1];
		if (len < 0) 
		    throw new ArrayIndexOutOfBoundsException();
	    	final int j = i;
		segs[j] = (DPJArray<T,this:[j]>) A.subarray(idxs[j-1], len);	    
	    }
            i = idxs[idxs.length - 1];
            len = A.length - i;
	    final int length = idxs.length;
            segs[length] = (DPJArray<T,this:[length]>) A.subarray(i, len);
	}
    }

    /**
     * Returns segment {@code idx} of the partition.
     *
     * <p> Throws {@code ArrayIndexOutOfBoundsException} if {@code
     * idx} is not a valid segment index.
     *
     * @param idx  Index of the segment to get
     * @return Segment {@code idx} of the partition
     */
    public DPJArray<T,this : [idx] : *> get(final int idx) reads this:[idx] {
	if (idx < 0 || idx > length-1) {
	    throw new ArrayIndexOutOfBoundsException();
	}
	if (segs != null)
   	    return segs[idx];
	else {
	    int start = idx * stride;
	    int segLength = (start + stride > A.length) ? (A.length - start) : stride;
	    return (DPJArray<T,this:[idx]:*>) A.subarray(start, segLength);
        }
    }

}
