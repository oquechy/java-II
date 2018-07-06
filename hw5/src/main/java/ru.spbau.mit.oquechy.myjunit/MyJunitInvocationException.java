package ru.spbau.mit.oquechy.myjunit;

/**
 * Exception which is thrown then {@link MyJunitInvoker} meets incorrect
 * declaration of test method, setup method, tear down method or test class constructor.
 */
public class MyJunitInvocationException extends Throwable {
    /**
     * Binds a message to the exception.
     *
     * @param message message to user
     */
    public MyJunitInvocationException(String message) {
        super(message);
    }
}
