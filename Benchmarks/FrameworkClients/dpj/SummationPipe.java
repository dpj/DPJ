import DPJRuntime.Framework.*;
import DPJRuntime.Framework.Pipeline.*;

/**
 * <p>class SummationPipe</p>
 * 
 * <p>A simple exercise for Pipeline.</p> Create a stream of
 * integers and compute its sum.  The creating and summing are both
 * sequential, but they happen in pipelined parallel fashion.
 **/
public class SummationPipe {

    region SourceRegion;
    region PipeRegion;
    static final int COUNT = 16;

    private static class Integer<region R> {
        public final int i;
        Integer(int v) pure { i = v; }
        public String toString() pure { 
	    return "Integer(i = " + java.lang.Integer.toString(i) + ")"; 
	}
    }

    /**
     * Filter for creating a stream of integers
     */
    private static final class IntSourceFilter<region FR>
        implements Filter<Integer<SourceRegion>,FR,pure> {
        private int i in FR = 0;
        public <region R>Integer<R> op(Integer<R> item)
            reads FR {
            if (i < COUNT) {
		// Generate the next integer in sequence
                return new Integer<R>(i++); 
	    } else {                        
		// Signal end of stream
                return null;
	    }
        }
    }

    /**
     * Filter for summing a stream of integers
     */
    private static final class IntSumFilter<region FR>
        implements Filter<Integer<SourceRegion>,FR,pure> {
        public int sum in FR = 0;
	public int count in FR = 0;
        public <region R>Integer<R> op(Integer<R> item)
            reads R writes FR {
            sum += item.i;
	    if (++count == COUNT) {
		System.out.printf("sum = %d\n", sum);
	    }
            return item;
        }
    }

    /**
     * Factory class to create and return an IntSourceFilter
     */
    private static final class IntSourceFactory
        implements FilterFactory<Integer<SourceRegion>,pure> {
        public <region R>Filter<Integer<SourceRegion>,R,pure> 
	    createFilter() pure {
            return new IntSourceFilter<R>();
        }
    }

    /**
     * Factory class to create and return an IntSumFilter
     */
    private static final class SummationFactory
        implements FilterFactory<Integer<SourceRegion>,pure> {
        public <region R>Filter<Integer<SourceRegion>,R,pure> 
	    createFilter() pure {
            return new IntSumFilter<R>();
        }
    }

    public static void main(String[] args) {
        Pipeline<Integer<SourceRegion>, PipeRegion, pure> pipeline = 
	    new Pipeline<Integer<SourceRegion>, 
	    PipeRegion, pure>();
	pipeline.appendStageWithFilter(new IntSourceFactory());
        pipeline.appendStageWithFilter(new SummationFactory());
	// Run the pipeline.  This blocks until all pipe stages exit.
        pipeline.launchAllStages();
    }
}
