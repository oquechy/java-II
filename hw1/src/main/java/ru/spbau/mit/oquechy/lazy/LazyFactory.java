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

    private static final Object mutex = new Object();

    /**
     * Returns {@link Lazy} for working in single thread.
     * @param sup computation to be called
     * @param <T> type of result of the computation
     */
    public static <T> Lazy<T> createSingleThreadLazy(Supplier<T> sup) {
        return new Lazy<T>() {
            private T result;
            @Nullable
            private Supplier<T> supplier = sup;

            @Override
            public T get() {
                if (supplier != null) {
                    result = supplier.get();
                    supplier = null;
                }

                return result;
            }
        };
    }

    /**
     * Returns {@link Lazy} for working in several threads.
     * @param sup computation to be called
     * @param <T> type of result of the computation
     */
    public static <T> Lazy<T> createMultiThreadLazy(Supplier<T> sup) {
        return new Lazy<T>() {
            private T result;
            @Nullable
            private Supplier<T> supplier = sup;

            @Override
            public T get() {
                if (supplier == null) {
                    return result;
                }

                synchronized (mutex) {
                    if (supplier != null) {
                        result = supplier.get();
                        supplier = null;
                    }
                }
                return result;
            }
        };
    }

}
