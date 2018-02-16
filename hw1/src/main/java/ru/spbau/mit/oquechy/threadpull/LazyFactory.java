package ru.spbau.mit.oquechy.threadpull;

import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class LazyFactory {

    private static final Object mutex = new Object();

    public static <T> Lazy<T> createSingleThreadLazy(Supplier<T> sup) {
        return new Lazy<>() {
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

    public static <T> Lazy<T> createMultiThreadLazy(Supplier<T> sup) {
        return new Lazy<>() {
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
