package ru.spbau.mit.oquechy.lazy;

import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Produces two implementations of {@link Lazy}.
 *
 * SingleThreadLazy guarantees that computation will be computed
 * just once if only one thread can call its {@code get} method.
 *
 * MultiThreadLazy guarantees that computation will be computed
 * just once even if several threads can call its {@code get} method.
 *
 */
public class LazyFactory {

    /**
     * Returns {@link Lazy} for working in single thread.
     * @param supplier computation to be called
     * @param <T> type of result of the computation
     */
    public static <T> Lazy<T> createSingleThreadLazy(Supplier<T> supplier) {
        return new Lazy<T>() {
            private T result;
            @Nullable
            private Supplier<T> sup = supplier;

            @Override
            public T get() {
                if (sup != null) {
                    result = sup.get();
                    sup = null;
                }

                return result;
            }
        };
    }

    /**
     * Returns {@link Lazy} for working in several threads.
     * @param supplier computation to be called
     * @param <T> type of result of the computation
     */
    public static <T> Lazy<T> createMultiThreadLazy(Supplier<T> supplier) {
        return new Lazy<T>() {
            private T result;
            @Nullable
            volatile private Supplier<T> sup = supplier;

            @Override
            public T get() {
                if (sup == null) {
                    return result;
                }

                synchronized (this) {
                    if (sup != null) {
                        // sup may become null only in this synchronized block
                        result = sup.get();
                        sup = null;
                    }
                }
                return result;
            }
        };
    }

}
