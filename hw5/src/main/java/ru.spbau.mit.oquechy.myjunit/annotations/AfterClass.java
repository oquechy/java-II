package ru.spbau.mit.oquechy.myjunit.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates a method which should be run after all tests
 * by {@link ru.spbau.mit.oquechy.myjunit.MyJunitInvoker}.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AfterClass {
}
