package DPJRuntime;

/**
 * This class provides various utility methods for DPJ.
 *
 * @author Robert L. Bocchino Jr.
 */
public class Utils {

    /**
     * Quickly computes the floor of the base 2 log of {@code x}.
     *
     * @param x Input
     * @return  Floor of base 2 log of {@code x}
     */
    public static int log2(int x) pure {
	if (x <= 0) throw new ArithmeticException();
	int result = 0;
	while (x > 1) {
	    x >>= 1;
	    ++result;
	}
	return result;
    }

    /**
     * Swaps the values at indices {@code i} and {@code j}
     * of array {@code A}.
     *
     * @param i First index of values to swap
     * @param j Second index of values to swap
     * @param <T> Element type of array {@code A}
     * @param <R> Region of array {@code A}
     */
    public static <type T, region R> void swap(Array<T,R> A, 
					       int i, int j) 
        writes R 
    {
	T tmp = A[j];
	A[j] = A[i];
	A[i] = tmp;
    }

    /**
     * Randomly permutes array {@code A}
     *
     * @param A Array to permute
     * @param <T> Element type of array {@code A}
     * @param <R> Region of array {@code A}
     */
    public static <type T, region R> void permute(Array<T,R> A) {
	for (int i = 0; i < A.length; ++i) {
	    int j = (int) (Math.random() * A.length);
	    int k = (int) (Math.random() * A.length);
	    Utils.swap(A, j, k);
	}
    }

    /**
     * Randomly permutes integer array {@code A}
     *
     * @param A Integer array to permute
     * @param <R> Region of array {@code A}
     */
    public static <region R> void permuteInt(ArrayInt<R> A) {
	for (int i = 0; i < A.length; ++i) {
	    int j = (int) (Math.random() * A.length);
	    int k = (int) (Math.random() * A.length);
	    int tmp = A[j];
	    A[j] = A[i];
	    A[i] = tmp;
	}
    }

}

