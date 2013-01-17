package DPJRuntime;
import java.util.*;

/**
 * DPJ version of {@code java.util.HashSet}.  This implementation
 * extends {@code HashSet}, and every method calls the method that it
 * overrides.  This provides a way to get {@code HashSet}
 * functionality with DPJ region and effect annotations.
 *
 * @author Rob Bocchino
 */
public class SequentialHashSet<type E, region R> extends HashSet<E>
    implements SequentialSet<E,R> {
    @Override
    public boolean add(E e) writes R { 
	return super.add(e); 
    }

    @Override 
    public boolean addAll(Collection<? extends E> c) writes R {
	return super.addAll(c);
    }

    @Override
    public void clear() writes R {
	super.clear();
    }

    @Override
    public boolean contains(Object o) reads R {
	return super.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) reads R {
	return super.containsAll(c);
    }

    @Override
    public boolean equals(Object o) reads R {
	return super.equals(o);
    }

    @Override
    public int hashCode() reads R {
	return super.hashCode();
    }

    @Override
    public boolean isEmpty() reads R {
	return super.isEmpty();
    }

    @Override
    public Iterator<E> iterator() reads R {
	return super.iterator();
    }

    @Override
    public boolean remove(Object o) writes R {
	return super.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) writes R {
	return super.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) writes R {
	return super.retainAll(c);
    }

    @Override
    public int size() reads R {
	return super.size();
    }

    @Override
    public Object[] toArray() reads R {
	return super.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) reads R {
	return super.toArray(a);
    }
}
