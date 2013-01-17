package DPJRuntime.Framework;

import extra166y.*;
import DPJRuntime.Framework.ArrayOps.*;

/**
 * <p>class DisjointArray</p>
 * 
 * <p>A linear array supporting safe parallel updates to its members.
 * Supports several generic parallel operations that can be
 * specialized via hook methods.  Details TBA.</p>
 **/
public final class DisjointArray<type T<region TR>, region AR> 
    implements DisjointContainer {

    private final extra166y.ParallelArray<T> array in AR;
	
    /**
     * Constructor for use by create*() methods or subclasses 
     * to create a new DPJParallelArray, initially using the supplied
     * array.  To create a DisjointArray for now, use {@link #create},
     * followed by the {@link #DisjointGenerator()} to populate it, or use
     * {@link #withIndexedMapping} to create a new instance from an old one.
     * 
     * @param array: the underlying ParallelArray instance
     */
    private DisjointArray(extra166y.ParallelArray<T> array) pure {
        this.array = array;
        if (array == null)
            throw new NullPointerException();
    }

    /** Hack to get around the fact that we can't write C<R>.class in
     * the client.  Also, it would be better to make T<TR> a method param
     * and eliminate the extra class, but such isn't supported in the
     * compiler yet!  */

    public static final class Creator<type T<region TR>, region AR> {
        public DisjointArray<T,AR> create(int size, 
					   Class<T<Root>> fakeEltType) pure{
	    Class<T> actualEltType = (Class<T>) fakeEltType;
	    extra166y.ParallelArray<T> array = extra166y.ParallelArray.
    		<T>create(size, actualEltType,
                          extra166y.ParallelArray.defaultExecutor());
	    return new DisjointArray<T,AR>(array);
	}
    }
    
    /**
     * Parallel methods for constructing, mapping and transforming
     * all elements in the array.
     */
    /** Parallel construction (non-indexed) of all elements.
     * 
     * @param op: Operation to generate each new object of the array.
     **/
    public <effect E | effect E # writes AR effect E>
	void replaceWithGeneratedValue(DisjointGenerator<T, effect E> generator)
	writes AR effect E {
	extra166y.Ops.Generator<T> wrapper = new DisjointGeneratorWrapper<T,effect E>(generator);
	array.replaceWithGeneratedValue(wrapper);
    }
    // where
    private class DisjointGeneratorWrapper<type T, effect E> 
    implements extra166y.Ops.Generator<T> {
        private DisjointGenerator<T, effect E> generator;
        public DisjointGeneratorWrapper(DisjointGenerator<T, effect E> generator) { 
	    this.generator = generator; 
	}
        public T op() { return generator.op(); }
    }
    
    /** Parallel unindexed mapping operation on all elements.
     * 
     * @param op: Op. to generate a new object for each object in old array.
     * 
     * @return  : New DPJParallelArray<T2,AR> with objects produced by mapping.
    */
    public <type T2<region R>, effect E | effect E # reads AR writes TR:* effect E> 
	DisjointArray<T2,AR> 
	withMapping(DisjointObjectToObject<T, T2, effect E> op)
	reads AR effect E {
	return new DisjointArray<T2,AR>
	    (array.withMapping(new DisjointObjectToObjectWrapper<T,T2,effect E>(op)).all());
    }
    // where
    private class DisjointObjectToObjectWrapper<type T1, T2<region R>, effect E>
    implements extra166y.Ops.Op<T1, T2> {
        private DisjointObjectToObject<T1, T2, effect E> theOp;
        public DisjointObjectToObjectWrapper(DisjointObjectToObject<T1, T2,effect E> op) { theOp = op; }
        public final T2 op(T1 obj) { return theOp.<region R>op(obj); }
    }
    
    /** Parallel indexed mapping operation on all elements.
     * 
     * @param op: Op. to generate a new object for each object in old array.
     * 
     * @return  : New DPJParallelArray<T,AR> with objects produced by mapping.
    */
    public <type T2<region R>, effect E | effect E # reads AR writes TR:* effect E> 
	DisjointArray<T2,AR> 
	withIndexedMapping(DisjointIntAndObjectToObject<T, T2, effect E> mapper) 
	reads AR effect E {
        return new DisjointArray<T2,AR>(array.withIndexedMapping
					(new DisjointIntAndObjectToObjectWrapper<T,T2,effect E>(mapper)).
					all());
    }
    // where
    private class DisjointIntAndObjectToObjectWrapper<type T1, T2, effect E>
	implements extra166y.Ops.IntAndObjectToObject<T1,T2> {
        private DisjointIntAndObjectToObject<T1,T2,effect E> mapper;
        public DisjointIntAndObjectToObjectWrapper
	    (DisjointIntAndObjectToObject<T1,T2,effect E> mapper) 
            { this.mapper = mapper; }
        public final T2 op(int idx, T1 obj) { return mapper.op(idx, obj); }
    }

    /** Parallel reduction of elements into a single value.
     * 
     * @param op: Op. to reduce all elements into a single element.
     *		  Op must be associative or results may be nondeterministic.
     * 
     * @return  : result of reduction
     */
    public <effect E | effect E # reads AR effect E> 
	T reduce(Reducer<T, effect E> op, T base)
    reads AR effect E {
	return array.reduce(new ReducerWrapper<effect E>(op), base);
    }
    // where
    private class ReducerWrapper<effect E> implements extra166y.Ops.Reducer<T> {
        private Reducer<T, effect E> theReducer;
        public ReducerWrapper(Reducer<T, effect E> reducer) { theReducer = reducer; }
        public final T op(T a, T b) { return theReducer.op(a, b); }
    }
    
    public int size() pure { return array.size(); }

    public T get(int i) reads AR { return array.get(i); }
}
