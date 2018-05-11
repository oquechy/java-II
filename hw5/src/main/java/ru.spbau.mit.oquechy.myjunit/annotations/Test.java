package ru.spbau.mit.oquechy.myjunit.annotations;

import ru.spbau.mit.oquechy.myjunit.MyJunitInvoker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that annotated method is a test and should
 * be run by {@link MyJunitInvoker}.
 * <p>
 * Test will be disabled if {@code ignore} contains not default value {@code Test.EMPTY}.
 * <p>
 * If the test method should throw an exception, you can check it by
 * setting {@code expected} to class of the desired exception.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Test {
    String EMPTY = "<#EMPTY#>";

    /**
     * Optionally specify a Throwable to cause a test method to succeed if
     * and only if an exception of the specified class is thrown by the method.
     */
    Class<? extends Throwable> expected() default None.class;

    /**
     * Annotated test method won't be invoked if {@code ignore} is not empty.
     */
    String ignore() default EMPTY;

    /**
     * Default empty exception.
     */
    class None extends Throwable {
    }
}

