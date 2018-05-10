package ru.spbau.mit.oquechy.myjunit.annotations;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AfterClass {
}
