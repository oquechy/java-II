package ru.spbau.mit.oquechy.threadpool;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Implementation of thread pool with constant number of threads.
 * It stores {@link LightFuture} object, so it can be called
 * thenApply method to add new task in the pool.
 */
public class ThreadPoolImpl {

    @NotNull
    private ConcurrentQueue<LightFutureImpl<?, ?>> queue = new ConcurrentQueue<>();
    private Thread[] threads;

    /**
     * Starts threadsCount threads with access to queue of tasks.
     * @param threadsCount number of threads to be ran
     */
    public ThreadPoolImpl(int threadsCount) {
        threads = new Thread[threadsCount];
        for (int i = 0; i < threadsCount; i++) {
            threads[i] = new Thread(() -> {
                try {
                    while (!Thread.interrupted()) {
                        LightFutureImpl<?, ?> lightFuture = queue.take();
                        lightFuture.run();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (LightExecutionException ignored) { }
            });
            threads[i].start();
        }
    }

    /**
     * Interrupts all threads and stops the work.
     * @throws InterruptedException then waiting for other threads to join
     * with main one was interrupted
     */
    public void shutdown() throws InterruptedException {
        for (@NotNull Thread thread : threads) {
            thread.interrupt();
        }

        for (@NotNull Thread thread : threads) {
            thread.join();
        }
    }

    /**
     * Adds new task to pool.
     * @param supplier function to compute
     * @param <T> return value of the supplier
     * @return {@link LightFuture} object to track progress of task
     */
    @NotNull
    public <T> LightFuture<T> assignTask(Supplier<T> supplier) {
        @NotNull LightFutureImpl<T, ?> lightFuture = new LightFutureImpl<>(supplier);
        queue.add(lightFuture);
        return lightFuture;
    }

    /**
     * Multi thread storage for supplier.
     * @param <T> return type of the supplier.
     * @param <U> return type of previous task if applicable.
     */
    private class LightFutureImpl<T, U> implements LightFuture<T> {
        private T result;
        private volatile boolean isReady = false;
        @NotNull
        private final Object syncSupplier = new Object();
        private final Object syncAfterTasks = new Object();
        private Supplier<T> supplier;
        private Function<U, T> function;
        @Nullable
        private volatile LightExecutionException exception = null;
        private final Queue<LightFutureImpl<?, T>> afterTasks = new LinkedList<>();

        /**
         * Doesn't call supplier.
         * @param supplier to be stored
         */
        private LightFutureImpl(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        /**
         * Runs computation. Notifies any threads waiting for result.
         * Adds related tasks to thread pool.
         * @throws LightExecutionException if supplier throws an exception
         * while running
         */
        private void run() throws LightExecutionException {
            try {
                synchronized (syncSupplier) {
                    result = supplier.get();
                    isReady = true;
                    syncSupplier.notify();
                }
            } catch (RuntimeException e) {
                synchronized (syncSupplier) {
                    exception = new LightExecutionException(e);
                    syncSupplier.notify();
                    // once became notnull forever notnull
                    throw exception;
                }
            }

            synchronized (syncAfterTasks) {
                while (!afterTasks.isEmpty()) {
                    LightFutureImpl<?, T> afterTask = afterTasks.remove();
                    afterTask.initSupplier(result);
                    queue.add(afterTask);
                }
            }
        }

        private void initSupplier(U result) {
            supplier = () -> function.apply(result);
        }

        private LightFutureImpl(Function<U, T> mapping) {
            function = mapping;
        }

        /**
         * Returns true then computation is over.
         */
        @Override
        public boolean isReady() {
            return isReady;
        }

        /**
         * Waits for computation to end and returns result of
         * computation every time the same.
         * @return return value of the supplier
         */
        @Override
        public T get() throws InterruptedException, LightExecutionException {
            if (exception != null) {
                // once became notnull forever notnull
                throw exception;
            }

            if (isReady) {
                return result;
            }

            synchronized (syncSupplier) {
                while (!isReady && exception == null) {
                    syncSupplier.wait();
                }
            }

            if (exception != null) {
                // once became notnull forever notnull
                throw exception;
            }

            return result;
        }

        /**
         * Applies mapping to result of the supplier to get a new supplier.
         * Doesn't wait for the result. Adds new supplier
         * in to-do-list of this thread pool.
         */
        @NotNull
        @Override
        public <V> LightFuture<V> thenApply(@NotNull Function<T, V> mapping) {
            if (isReady) {
                @NotNull LightFutureImpl<V, T> lightFuture = new LightFutureImpl<>(() -> mapping.apply(result));
                queue.add(lightFuture);
                return lightFuture;
            }

            @NotNull LightFutureImpl<V, T> lightFuture = new LightFutureImpl<>(mapping);
            synchronized (syncAfterTasks) {
                afterTasks.add(lightFuture);
            }
            return lightFuture;
        }
    }
}
