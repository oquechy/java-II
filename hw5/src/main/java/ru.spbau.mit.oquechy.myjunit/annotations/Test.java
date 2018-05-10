package ru.spbau.mit.oquechy.myjunit.annotations;

import ru.spbau.mit.oquechy.myjunit.MyJunitInvoker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that annotated method is a test and should
 * be run by {@link MyJunitInvoker}.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Test {
    String EMPTY = "<‽>";

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
    class None extends Throwable { }
}

