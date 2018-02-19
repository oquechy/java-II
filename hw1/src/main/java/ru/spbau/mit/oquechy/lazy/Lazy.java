package ru.spbau.mit.oquechy.lazy;

/**
 * Interface for lazy computations. Result of {@code get} should be
 * computed once, no matter how many times it was called.
 * @param <T> type of the result of the computation.
 */
public interface Lazy<T> {
    /**
     * Method to get the result of the computation. It should be the same
     * object/value every time.
     * @return result of computation.
     */
    T get();
}
