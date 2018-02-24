package ru.spbau.mit.oquechy.threadpool;

public class LightExecutionException extends Exception {
    public LightExecutionException(Throwable throwable) {
        super(throwable);
    }
}
