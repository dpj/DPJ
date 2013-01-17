package DPJRuntime;
import java.util.*;

/**
 * This interface is identical to the {@code java.util.Set} interface,
 * with the addition of DPJ region and effect annotations.
 *
 * @author Rob Bocchino
 */
public interface SequentialSet<type E, region R> extends Set<E> {
    @Override
    boolean add(E e) writes R;

    @Override 
    boolean addAll(Collection<? extends E> c) writes R;

    @Override
    void clear() writes R;

    @Override
    boolean contains(Object o) reads R;

    @Override
    boolean containsAll(Collection<?> c) reads R;

    @Override
    boolean equals(Object o) reads R;

    @Override
    int hashCode() reads R;

    @Override
    boolean isEmpty() reads R;

    @Override
    Iterator<E> iterator() reads R;

    @Override
    boolean remove(Object o) writes R;

    @Override
    boolean removeAll(Collection<?> c) writes R;

    @Override
    boolean retainAll(Collection<?> c) writes R;

    @Override
    int size() reads R;

    @Override
    Object[] toArray() reads R;

    @Override
    <T> T[] toArray(T[] a) reads R;
}
