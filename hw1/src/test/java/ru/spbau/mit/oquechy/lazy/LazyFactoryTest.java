package ru.spbau.mit.oquechy.lazy;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Objects;
import java.util.Random;
import java.util.function.Supplier;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class LazyFactoryTest {

    @NotNull
    private final static String[] ARRAY = {"Lisa", "Loves", "Java", "But", "Also", "Sleeping",
            "(", "And", "Playing", "With", "Cats", ")"};

    @Test
    public void testCreateMultiThreadLazy() {

        @NotNull final Supplier<Boolean> supplier = () -> true;
        @NotNull Thread[] threads = new Thread[10000];
        @NotNull Lazy<Boolean> lazy = LazyFactory.createMultiThreadLazy(supplier);

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                boolean result = lazy.get();
                assertThat(result, is(true));
            });
        }

        for (@NotNull Thread thread : threads) {
            thread.start();
        }
    }

    @Test
    public void testCreateMultiThreadLazyJustOneGet() {

        @NotNull final Supplier<Integer> supplier = new Supplier<Integer>() {
            @NotNull
            private Integer x = 0;
            @NotNull
            private final Object mutex = new Object();

            @NotNull
            @Override
            public Integer get() {
                synchronized (mutex) {
                    return x++;
                }
            }
        };

        @NotNull Thread[] threads = new Thread[10000];
        @NotNull Lazy<Integer> lazy = LazyFactory.createMultiThreadLazy(supplier);

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0, result = lazy.get(); j < 100; j++) {
                    assertThat(result, is(0));
                    result = lazy.get();
                }
            });
        }

        for (@NotNull Thread thread : threads) {
            thread.start();
        }
    }

    @Test
    public void testCreateSingleThreadLazyJustOneGet() {

        @NotNull final Supplier<Integer> supplier = new Supplier<Integer>() {
            @NotNull
            private Integer x = 0;

            @NotNull
            @Override
            public Integer get() {
                return x++;
            }
        };

        @NotNull Lazy<Integer> lazy = LazyFactory.createSingleThreadLazy(supplier);

        for (int j = 0, result = lazy.get(); j < 10000; j++) {
            assertThat(result, is(0));
            result = lazy.get();
        }
    }

    @Test
    public void testCreateSingleThreadLazyEveryTimeSameObject() {

        @NotNull final Supplier<String> peekRandom = () -> {
            int rnd = new Random().nextInt(ARRAY.length);
            return ARRAY[rnd];
        };

        @NotNull Lazy<String> lazy = LazyFactory.createSingleThreadLazy(peekRandom);

        String expected = lazy.get();

        for (int j = 0; j < 10000; j++) {
            String found = lazy.get();
            assertThat(Objects.equals(expected, found), is(true));
        }
    }

    @Test
    public void testCreateSingleThreadLazyLaziness() {

        @NotNull final Supplier<String> shouldNotBeCalled = () -> {
            throw new RuntimeException("Supplier was called");
        };

        for (int i = 0; i < 10; i++) {
            //noinspection UnusedAssignment,ResultOfMethodCallIgnored
            LazyFactory.createSingleThreadLazy(shouldNotBeCalled);
        }
    }

    @Test
    public void testCreateMultiThreadLazyEveryTimeSameObject() {

        @NotNull final Supplier<String> cyclicSupplier = new Supplier<String>() {
            private int i = 0;

            @Override
            public String get() {
                return ARRAY[i++ % ARRAY.length];
            }
        };

        @NotNull Thread[] threads = new Thread[10000];
        @NotNull Lazy<String> lazy = LazyFactory.createMultiThreadLazy(cyclicSupplier);
        String expected = ARRAY[0];

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    String found = lazy.get();
                    assertThat(Objects.equals(expected, found), is(true));
                }
            });
        }

        for (@NotNull Thread thread : threads) {
            thread.start();
        }
    }

    @Test
    public void testCreateMultiThreadLazyNoDataRaces() throws InterruptedException {

        @NotNull final Supplier<Integer> incrementer = new Supplier<Integer>() {
            private int i = 0;

            @NotNull
            @Override
            public Integer get() {
                for (int j = 0; j < 100; j++) {
                    i++;
                }
                return i;
            }
        };

        @NotNull Lazy<Integer> lazyIncrementer = LazyFactory.createMultiThreadLazy(incrementer);

        @NotNull Thread threads[] = new Thread[100];

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(lazyIncrementer::get);
        }

        for (@NotNull Thread thread : threads) {
            thread.start();
        }

        for (@NotNull Thread thread : threads) {
            thread.join();
        }

        assertThat(incrementer.get(), is(200));
    }
}