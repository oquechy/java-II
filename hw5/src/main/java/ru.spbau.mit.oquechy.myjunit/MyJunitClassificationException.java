package ru.spbau.mit.oquechy.myjunit;

/**
 * Exception which is thrown then {@link MyJunitClassifier} meets incorrect
 * combination of annotations.
 */
public class MyJunitClassificationException extends Exception {
    /**
     * Binds a message to the exception.
     *
     * @param message message to user
     */
    public MyJunitClassificationException(String message) {
        super(message);
    }
}
