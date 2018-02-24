package ru.spbau.mit.oquechy.threadpool;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * An interface for encapsulating multi thread tasks.
 * @param <T> return value of the task
 */
public interface LightFuture<T> {
    /**
     * Returns {@code true} then task is finished.
     */
    boolean isReady();

    /**
     * Returns result of work.
     * @throws InterruptedException if waiting for the result to compute fails
     * @throws LightExecutionException if computation ends up with an exception
     */
    T get() throws InterruptedException, LightExecutionException;

    /**
     * Applies mapping to result of the task to get a new task.
     * @param mapping function to get new task from the result
     *                of the previous
     * @param <U> return value of new task
     * @return new task
     * @throws InterruptedException if waiting for the result to compute fails
     * @throws LightExecutionException if computation ends up with an exception
     */
    @NotNull
    <U> LightFuture<U> thenApply(Function<T, U> mapping) throws InterruptedException, LightExecutionException;
}
