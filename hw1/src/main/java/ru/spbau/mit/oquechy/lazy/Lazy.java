package ru.spbau.mit.oquechy.lazy;

/**
 * Interface for lazy computations. Result of {@code get} should be
 * computed once, no matter how many times it was called.
 * @param <T> type of the result of the computation.
 */
public interface Lazy<T> {
    T get();
}
