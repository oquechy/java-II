package ru.spbau.mit.oquechy.threadpool;

/**
 * Exception is thrown when a task inside {@link ThreadPoolImpl} ends up with an exception.
 */
public class LightExecutionException extends Exception {

    /**
     * Binds the exception which was thrown by the task to itself.
     *
     * @param throwable exception which was thrown by the task
     */
    public LightExecutionException(Throwable throwable) {
        super(throwable);
    }
}
