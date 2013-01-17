package DPJRuntime.Framework;

/**
 * Interfaces and utilities declaring per-element operations used
 * within parallel methods on aggregates. This class provides type
 * names for operation signatures accepting (for now) one
 * argument and returning one result, for parameterized types.
 */
public class ArrayOps {
    public interface DisjointGenerator<type T<region R>, effect E> {
        public <region R>T<R> op() effect E;
    }

    public interface DisjointObjectToObject<type T1, T2<region R>, effect E> {
        public <region R>T2<R> op(final T1 obj) writes R effect E;
    }

    public interface DisjointIntAndObjectToObject<type T1, type T2<region R>,
						effect E> {
        public <region R>T2<R> op(int i, final T1 obj) writes R effect E;
    }

    public interface Reducer<type T<R>, effect E> {
	public T  op(final T a, final T b)
            reads R writes a, b effect E;
    }
}

