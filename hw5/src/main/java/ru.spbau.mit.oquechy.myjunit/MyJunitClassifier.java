package ru.spbau.mit.oquechy.myjunit;

import lombok.Getter;
import ru.spbau.mit.oquechy.myjunit.annotations.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

class MyJunitClassifier {
    @Getter
    private ArrayList<Method> beforeEach = new ArrayList<>();
    @Getter
    private ArrayList<Method> beforeAll = new ArrayList<>();
    @Getter
    private ArrayList<Method> afterEach = new ArrayList<>();
    @Getter
    private ArrayList<Method> afterAll = new ArrayList<>();
    @Getter
    private ArrayList<Method> enabledTests = new ArrayList<>();
    @Getter
    private ArrayList<Method> disabledTests = new ArrayList<>();

    public boolean classify(Method method) throws MyJunitClassificationException {
        List<Class<?>> annotations = new ArrayList<>();
        List<Method> dst = null;
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
            return true;
        }

        return false;
    }
}
