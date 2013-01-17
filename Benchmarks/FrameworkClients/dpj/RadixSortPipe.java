import DPJRuntime.Framework.*;
import DPJRuntime.Framework.Pipeline.*;
import java.lang.Math;
import java.util.ArrayList;

public class RadixSortPipe {
    public static final int MAX_RADIX = 2048;
    public static final int SIZE = 16;
    public static final int NUMREPS = 10;

    region ArrayRegion;
    region PipeRegion;

    private static class IntArray<region R> {
        public final int data[]<R> in R;

        IntArray() {
            // For now, use the same 16 "random" numbers used by the
            // StreamIt RadixSort benchmark (taken from Knuth).
            assert(SIZE <= MAX_RADIX);
            data = new int[SIZE]<R>;
            data[0]  = 503; data[1]  =  87; data[2]  = 512;
            data[3]  = 061; data[4]  = 908; data[5]  = 170;
            data[6]  = 897; data[7]  = 275; data[8]  = 653;
            data[9]  = 426; data[10] = 154; data[11] = 509;
            data[12] = 612; data[13] = 677; data[14] = 765;
            data[15] = 703;
        }

	public String toString() reads R {
	    StringBuffer sb = new StringBuffer();
	    for (int d : data) {
		sb.append(d + " ");
	    }
	    return sb.toString();
	}
    }

    private static final class IntSourceFilter<region FR>
        implements Filter<IntArray<ArrayRegion>,FR,pure> {
        private int rep in FR = 0;
        public <region R>IntArray<R> op(IntArray<R> item)
            reads R writes FR {
            if (rep++ < NUMREPS) {		
		IntArray<R> intArray = new IntArray<R>();
                return intArray;
	    } else {
		// Signal end of stream
		return null;
	    }
        }
    }

    /**
     * Factory for creating an IntSourceFilter
     */
    private static final class IntSourceFactory
        implements Pipeline.FilterFactory<IntArray<ArrayRegion>,pure> {
        public <region R> Pipeline.Filter<IntArray<ArrayRegion>,R,pure> 
	    createFilter() pure {
            return new IntSourceFilter<R>();
        }
    }

    private static class SortFilter<region FR>
        implements Filter<IntArray<ArrayRegion>, FR, pure> {
        final int radix;
        SortFilter(int radix) { this.radix = radix; }
	// Arrays to hold intermediate results of sorting;
	final int[]<FR> left = new int[SIZE]<FR>; 
	final int[]<FR> right = new int[SIZE]<FR>;
	/**
	 * Stable sort on radix
	 */
        public <region R>IntArray<R> op(IntArray<R> item)
            writes R, FR {
	    // Copy 0 and 1 items into two temporary arrays left and
	    // right, maintaining the original order in each group
	    int leftSize = 0, rightSize = 0;
	    for (int i : item.data) {
		if ((i & radix) == 0) {
		    left[leftSize++] = i;
		} else {
		    right[rightSize++] = i;
		}
	    }
	    // Copy back all left items, followed by all right items
	    int idx = 0;
	    for (int i = 0; i < leftSize; ++i) {
		item.data[idx++] = left[i];
	    }
	    for (int i = 0; i < rightSize; ++i) {
		item.data[idx++] = right[i];
	    }
            return item;
        }
    }

    private static final class SortFilterFactory
        implements Pipeline.FilterFactory<IntArray<ArrayRegion>, pure> {
        final int radix;
        SortFilterFactory(int radix) { this.radix = radix; }
        public <region R>Pipeline.Filter<IntArray<ArrayRegion>, R, pure>
            createFilter() pure {
            return new SortFilter<R>(radix);
        }
    }

    public static void addSortStages(Pipeline<IntArray<ArrayRegion>, 
				     PipeRegion, pure> pipeline) {
        for (int radix=1; radix < MAX_RADIX; radix <<= 1)
            pipeline.appendStageWithFilter(new SortFilterFactory(radix));
    }

    private static synchronized void print(String s) pure {
	System.out.println(s);
    }

    public static void main() {

        // Create the pipeline and populate it with the source stage
        // and log_2(MAX_RADIX) stages
        Pipeline<IntArray<ArrayRegion>, PipeRegion, pure> pipeline = 
	    new Pipeline<IntArray<ArrayRegion>, 
	    PipeRegion, pure>();
        pipeline.appendStageWithFilter(new IntSourceFactory());
        RadixSortPipe.addSortStages(pipeline);

        // Process the data
        pipeline.launchAllStages();
    }
}
