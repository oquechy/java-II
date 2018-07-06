package ru.spbau.mit.oquechy.myjunit;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.mit.oquechy.myjunit.annotations.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for differentiating the methods of the test class
 * depending on annotations.
 */
public class MyJunitClassifier {
    /**
     * List of methods which should be run before each test.
     */
    @NotNull
    @Getter
    private ArrayList<Method> beforeEach = new ArrayList<>();

    /**
     * List of methods which should be run before all tests.
     */
    @NotNull
    @Getter
    private ArrayList<Method> beforeAll = new ArrayList<>();

    /**
     * List of methods which should be run after each test.
     */
    @NotNull
    @Getter
    private ArrayList<Method> afterEach = new ArrayList<>();

    /**
     * List of methods which should be run after all tests.
     */
    @NotNull
    @Getter
    private ArrayList<Method> afterAll = new ArrayList<>();

    /**
     * List of enabled test methods.
     */
    @NotNull
    @Getter
    private ArrayList<Method> enabledTests = new ArrayList<>();

    /**
     * List of disabled test methods.
     */
    @NotNull
    @Getter
    private ArrayList<Method> disabledTests = new ArrayList<>();

    /**
     * Puts a method to a corresponding list and prepares it to be invoked
     * by setting it accessible.
     *
     * @param method method to be classified
     * @return true if the method is classified as a test, setup method or tear down method
     * @throws MyJunitClassificationException if invalid combination of annotations was found
     */
    public boolean classify(@NotNull Method method) throws MyJunitClassificationException {
        @NotNull List<Class<?>> annotations = new ArrayList<>();
        @Nullable List<Method> dst = null;
        if (method.getAnnotation(Before.class) != null) {
            annotations.add(Before.class);
            dst = beforeEach;
        }
        if (method.getAnnotation(BeforeClass.class) != null) {
            annotations.add(BeforeClass.class);
            dst = beforeAll;
        }
        if (method.getAnnotation(After.class) != null) {
            annotations.add(After.class);
            dst = afterEach;
        }
        if (method.getAnnotation(AfterClass.class) != null) {
            annotations.add(AfterClass.class);
            dst = afterAll;
        }
        Test testAnnotation = method.getAnnotation(Test.class);
        if (testAnnotation != null) {
            annotations.add(Test.class);
            dst = testAnnotation.ignore().equals(Test.EMPTY) ? enabledTests : disabledTests;
        }

        if (annotations.size() > 1) {
            throw new MyJunitClassificationException(method.getName() + ": " +
                    "incompatible annotations " + annotations.get(0) + " and " + annotations.get(1));
        }

        if (dst != null) {
            dst.add(method);
            method.setAccessible(true);
            return true;
        }

        return false;
    }
}
