package DPJRuntime.Framework;

import extra166y.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * <p>class Pipeline</p>
 * 
 * <p>An algorithm template for parallel pipeline computations.  A
 * Pipeline consists of an ordered collection of pipeline stages.
 * Each stage contains an operation called a filter and a buffer of
 * data elements.  The data elements pass through the stages in order;
 * as a data element passes through a stage, the filter for that stage
 * is applied to the element.  Parallelism occurs because different
 * filters in different stages can operate on different elements at
 * the same time.</p>
 *
 * <p>We will probably need some mechanisms to control the rate of flow
 * but let's keep this simple for now.  </p>
 *
 * @author Vikram Adve
 * @author Rob Bocchino
 *
 * @param<T>  The type of a data element flowing through the pipeline
 * @param<TR> The first region argument of the type T
 * @param<PR> Region of the pipeline
 * @param<E>  Effect bound for all filters.  Filters added to the pipeline
 *            may write TR:*, write PR:*, and do effect E, but do no
 *            other effects.
 **/
public class Pipeline<type T<region TR>, region PR,
    effect E | effect E # writes TR:*, PR:* effect E> {

    // PUBLIC INTERFACES TO BE IMPLEMENTED BY THE USER

    /**
     * <p>interface Filter</p> 
     *
     * A Filter represents an operation performed by a pipeline stage.
     *
     * @param<T>  The type of an element operated on by the filter
     * @param<TR> The first region argument of type T
     * @param<FR> The region of the filter
     * @param<E>  Bound on filter operation effects, other than writing
     *            R and FR
     **/
    public interface Filter<type T<region TR>,
	region FR, effect E> {

	/**
	 * Operate on an item.  The user implements this method.  The
	 * implementation may write R and FR, and do effect E.
	 *
	 * The method region parameter R captures the actual region
	 * (unspecified here) of the item coming in.  It ensures that
	 * the returned item was created by some invocation of op
	 * (either this one, or in a previous stage).  That ensures
	 * that multiple data elements are never shared by different
	 * stages of the pipeline, so they can be updated in parallel
	 * by the different stages.
	 *
	 * @param <R>  Region for parameterizing the incoming and
	 *             outgoing items (which may be the same or 
	 *             different)
	 * @param item Item to operate on
	 * @return     Either item or different object with the same
	 *             region
	 */
	public <region R>T<R> op(T<R> item) writes R, FR effect E;
    }

    /**
     * <p>interface FilterFactory</p>
     *
     * A FilterFactory creates a single filter.
     *
     * @param<T>  Type of element operated on by the filter
     * @param<TR> First region argument of T
     * @param<E>  Effect bound for the filter
     **/
    public interface FilterFactory<type T<region TR>, effect E> {
        /** 
	 * Create and return a fresh filter instance.  The region
	 * parameter R ensures freshness.
	 */
        public <region R>Filter<T,R, effect E> createFilter() effect E;
    }

    // PUBLICLY VISIBLE METHODS

    /**
     * Create an empty pipeline.
     */
    public Pipeline() writes DPJRuntime.RuntimeState.Global {
        stages = new ArrayList<PipelineStage>();
	// Ensure one task per filter
        DPJRuntime.RuntimeState.dpjForeachCutoff = 1;
    }

    /**
     * Create a new pipeline stage with a new filter, add it to the
     * tail of the pipeline, and return the filter.
     *
     * Notice that the region of the returned filter is PR:[?].
     * That's because the region of the filter in stage i is PR:[i].
     * Since we're exposing these types to the user, we have to be
     * careful.  If we gave out references to Filter with PR as the
     * region, then the parameter FR in the Filter interface would be
     * bound to the same user-visible region for all the different
     * stages.  Because Filter.op allows a write effect on FR, that
     * could cause a race.
     *
     * Also notice we don't need to do this for T<TR> (i.e., we don't
     * need to make the type here T<TR:[?]>) because Filter.op doesn't
     * allow any effect on TR.  The effect is on R, which captures the
     * actual region in the type of the item going in.
     * 
     * @param factory   Factory for creating a new filter
     * @return          The created filter.  
     */
    public Filter<T,PR:[?],effect E> 
	appendStageWithFilter(FilterFactory<T, effect E> factory) 
	writes PR:[?] effect E 
    {
	final int idx = stages.size();
	Filter<T,PR:[idx],effect E> filter = 
	    factory.<region PR:[idx]>createFilter();
        PipelineStage stage = new PipelineStage(idx, filter);
	stages.add(stage);
        return filter;
    }

    /**
     * Launch tasks for all pipeline stages.
     * This method returns only when all stages have exited.
     */
    public void launchAllStages() writes TR:[?], PR:[?] effect E 
    {
        int pipeLength = stages.size();

	// Pipeline not set up!
        assert(pipeLength > 0);

	// Fork one task per pipeline stage
	// FIXME:  The order of execution may be suboptimal.
        foreach (int i in 0, pipeLength) {
            stages.get(i).run();
        }
    }

    // PRIVATE IMPLEMENTATION OF DPJPIPELINE

    /**
     * Pipeline stages 
     */
    private final ArrayList<PipelineStage> stages;

    /**
     * Debug switch
     */
    private static final boolean DEBUG_PIPE = true;

    /**
     * Buffer for input and output at a pipeline stage
     *
     * @param<R> Region of the buffer
     */
    private class ItemBuffer<region R> {
	/**
	 * Queue is thread-safe
	 */
        final ConcurrentLinkedQueue<BufferElement> itemQueue;
        ItemBuffer() pure { 
	    itemQueue = 
		new ConcurrentLinkedQueue<BufferElement>(); 
	}
        boolean isEmpty() reads R { return itemQueue.isEmpty(); }
        BufferElement peek() reads R { return itemQueue.peek(); }
        BufferElement poll() writes R { return itemQueue.poll(); }
        boolean add(BufferElement elt) writes R { 
	    return itemQueue.add(elt); 
	}
    }

    /**
     * Wrapper class for elements in the buffer, to represent two
     * kinds of values flowing through the pipeline: (1) actual data
     * items; and (2) sentinel values (start and end of stream).
     */
    private class BufferElement {
	/**
	 * The type of an item in a BufferElement is T<TR:[?]>, to
	 * represent the fact that (1) items are only ever created by
	 * Filter.op, and (2) every newly created object is assigned a
	 * region TR:[i], where the i is unique to that creation.
	 * This careful management of regions isn't strictly
	 * necessary, because it's not checked by the DPJ effect
	 * system, and these types are never seen by the user.
	 * However, it does help document what's going on.  Further,
	 * if we ever wanted to give out the types of these data
	 * elements to the user, then the regions would really be
	 * necessary (as they are for the filter regions, q.v.).
	 */
	private final T<TR:[?]> item;
	public BufferElement(T<TR:[?]> item) pure { this.item = item; }
	public BufferElement() pure { this.item = null; }
	public T<TR:[?]> getItem() pure { return item; }
	public String toString() pure {
	    return "BufferElement: " + item.toString();
	}
    }

    /**
     * Sentinel value for start of stream
     */
    private final BufferElement STREAM_START =
	new BufferElement() {
	    public String toString() pure {
		return "BufferElement: START OF STREAM";
	    }
	};

    /**
     * Sentinel value for end of stream
     */
    private final BufferElement STREAM_END =
	new BufferElement() {
	    public String toString() pure {
		return "BufferElement: END OF STREAM";
	    }
	};

    /**
     * A pipeline stage.  Stage i has its mutable data in region
     * PR:[i].  As far as the DPJ effect system is concerned, all
     * those regions are PR:[?].
     */
    private class PipelineStage {

	/**
	 * The filter operation for this stage
	 */
        final Filter<T,PR:[?],effect E> filter;

	/**
	 * Buffer for the output of this stage
	 */
        final ItemBuffer<PR:[?]> outBuf;

	/**
	 * The index of this stage
	 */
	final int idx;

	/**
	 * Create a new pipeline stage
	 *
	 * @param filter Filter for the stage
	 */
        PipelineStage(int idx, Filter<T,PR:[?],effect E> filter) pure {
	    this.idx = idx;
            this.filter = filter;
            outBuf = new ItemBuffer<PR:[?]>();
        }

	private void debugPrint(String s) pure {
	    if (DEBUG_PIPE) {
		System.out.println("Stage " + idx + ": " + s);
	    }
	}

        /**
         * Execute the stage until STREAM_END sentinel either appears
         * in the input, or is produced by the current stage.
         */
        public void run() writes TR:[?], PR:[?] effect E {
	    PipelineStage previousStage =
		(idx == 0) ? null : stages.get(idx-1);
	    ItemBuffer<PR:[?]> inBuf = (previousStage == null) ?
		null : previousStage.outBuf;
	    while (true) {
		BufferElement elt = STREAM_START;
                if (inBuf != null) {
                    debugPrint("Looking for input buffer element");
                    while ((elt = inBuf.poll()) == null)
			// Spin until input buffer has an entry
                        ;                  
                    debugPrint("Found " + elt);
                }
		if (elt != STREAM_END) {
		    debugPrint("Input element: "+elt);
		    // Every newly created item gets a new index
		    // region, unspecified here.  As described above,
		    // these index regions aren't really necessary,
		    // because the user never sees these types.  If
		    // the user did see them, they would prevent
		    // aliases that could cause races in the
		    // user-defined Filter.op method.
		    T<TR:[?]> item = filter.op(elt.getItem());
		    debugPrint("Item produced by filter: " +item);
		    // Null produced by filter means end of stream
		    elt = (item == null) ? 
			STREAM_END : new BufferElement(item);
		}
		outBuf.add(elt);
		if (elt == STREAM_END) break;
	    }
        }

    }

}
