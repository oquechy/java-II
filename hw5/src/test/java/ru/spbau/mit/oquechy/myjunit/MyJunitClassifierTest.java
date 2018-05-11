package ru.spbau.mit.oquechy.myjunit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.spbau.mit.oquechy.myjunit.examples.IncompatibleAnnotations;
import ru.spbau.mit.oquechy.myjunit.examples.Valid;

import java.lang.reflect.Method;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

class MyJunitClassifierTest {

    private MyJunitClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new MyJunitClassifier();
    }

    @Test
    void classifyValidClass() throws MyJunitClassificationException, NoSuchMethodException {
        Class<Valid> validClass = Valid.class;
        for (Method method : validClass.getDeclaredMethods()) {
            if (!method.getName().equals("notATest") && !method.getName().equals("validate")) {
                assertThat(classifier.classify(method), is(true));
            } else {
                assertThat(classifier.classify(method), is(false));
            }
        }

        assertThat(classifier.getBeforeAll(), containsInAnyOrder(
                validClass.getDeclaredMethod("bc1"),
                validClass.getDeclaredMethod("bc2")
        ));

        assertThat(classifier.getBeforeEach(), containsInAnyOrder(
                validClass.getDeclaredMethod("b1"),
                validClass.getDeclaredMethod("b2")
        ));

        assertThat(classifier.getEnabledTests(), containsInAnyOrder(
                validClass.getDeclaredMethod("t"),
                validClass.getDeclaredMethod("te"),
                validClass.getDeclaredMethod("tea"),
                validClass.getDeclaredMethod("tne")
        ));

        assertThat(classifier.getDisabledTests(), containsInAnyOrder(
                validClass.getDeclaredMethod("ti"),
                validClass.getDeclaredMethod("tie")
        ));
    }

    @Test
    void incompatibleAnnotations() {
        Class<IncompatibleAnnotations> invalidClass = IncompatibleAnnotations.class;
        for (Method method : invalidClass.getDeclaredMethods()) {
            assertThrows(MyJunitClassificationException.class, () -> classifier.classify(method));
        }
    }
}