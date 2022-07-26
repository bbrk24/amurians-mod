package org.bbrk24.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

/**
 * This class extends {@code ArrayList} with some of the FP functionality built into other
 * languages like Swift and JavaScript.
 */
public class FunctionalList<E> extends ArrayList<E> {
    public FunctionalList() {
        super();
    }

    public FunctionalList(int capacity) {
        super(capacity);
    }

    public FunctionalList(Collection<? extends E> collection) {
        super(collection);
    }

    /**
     * Create a new list containing the contents of both input lists, in order.
     */
    public static <T> FunctionalList<T> concat(
        Collection<? extends T> first,
        Collection<? extends T> second
    ) {
        FunctionalList<T> retval = new FunctionalList<>(first);
        retval.addAll(second);
        return retval;
    }

    /**
     * Perform a filter operation on a collection that is not of type {@code FunctionalList}.
     */
    public static <T> FunctionalList<T> filtering(
        Iterable<? extends T> collection,
        Predicate<? super T> pred
    ) {
        FunctionalList<T> retval = new FunctionalList<>();
        for (T el : collection) {
            if (pred.test(el)) {
                retval.add(el);
            }
        }
        return retval;
    }

    /** Whether at least some of the elements of the list satisfy the predicate. */
    public boolean some(Predicate<? super E> pred) {
        for (E el : this) {
            if (pred.test(el)) {
                return true;
            }
        }
        return false;
    }

    /** The elements from this list that satisfy the given predicate. */
    public FunctionalList<E> filter(Predicate<? super E> pred) {
        FunctionalList<E> retval = new FunctionalList<>();
        for (E el : this) {
            if (pred.test(el)) {
                retval.add(el);
            }
        }
        return retval;
    }
}
