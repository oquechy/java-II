package ru.spbau.mit.oquechy.threadpool;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Implementation of thread pool with constant number of threads.
 * It stores {@link LightFuture} object, so it can be called
 * thenApply method to add new task in the pool.
 */
public class ThreadPoolImpl {

    @NotNull
    private BlockingQueue<LightFutureImpl<?>> queue = new LinkedBlockingQueue<>();
    private Thread[] threads;

    /**
     * Starts threadsCount threads with access to queue of tasks.
     * @param threadsCount number of threads to be ran
     */
    public ThreadPoolImpl(int threadsCount) {
        threads = new Thread[threadsCount];
        for (int i = 0; i < threadsCount; i++) {
            threads[i] = new Thread(() -> {
                LightFutureImpl<?> lightFuture;
                try {
                    while (!Thread.interrupted()) {
                        lightFuture = queue.take();
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
        @NotNull LightFutureImpl<T> lightFuture = new LightFutureImpl<>(supplier);
        queue.add(lightFuture);
        return lightFuture;
    }

    /**
     * Multi thread storage for supplier.
     * @param <T> return type of the supplier.
     */
    private class LightFutureImpl<T> implements LightFuture<T> {
        private T result;
        private volatile boolean isReady = false;
        private final Object sync = new Object();
        private Supplier<T> supplier;
        @Nullable
        private volatile LightExecutionException exception = null;

        /**
         * Doesn't call supplier.
         * @param supplier to be stored
         */
        private LightFutureImpl(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        /**
         * Runs computation. Notifies any threads waiting for result.
         * @throws LightExecutionException if supplier throws an exception
         * while running
         */
        private void run() throws LightExecutionException {
            try {
                synchronized (sync) {
                    result = supplier.get();
                    isReady = true;
                    sync.notify();
                }
            } catch (RuntimeException e) {
                synchronized (sync) {
                    exception = new LightExecutionException(e);
                    sync.notify();
                    // once became notnull forever notnull
                    throw exception;
                }
            }
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

            synchronized (sync) {
                while (!isReady && exception == null) {
                    sync.wait();
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
         * Uses {@code get} for waiting for the result. Adds new supplier
         * in to-do-list of this thread pool.
         */
        @NotNull
        @Override
        public <U> LightFuture<U> thenApply(@NotNull Function<T, U> mapping)
                throws InterruptedException, LightExecutionException {
            T result = get();
            @NotNull LightFutureImpl<U> lightFuture = new LightFutureImpl<>(() -> mapping.apply(result));
            queue.add(lightFuture);
            return lightFuture;
        }
    }
}
