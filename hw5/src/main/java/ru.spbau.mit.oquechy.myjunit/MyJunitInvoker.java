package ru.spbau.mit.oquechy.myjunit;

import org.apache.commons.io.FileUtils;
import ru.spbau.mit.oquechy.myjunit.annotations.Test;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import static java.io.File.separator;

public class MyJunitInvoker {

    private final static String USAGE = "Pass the path to test sources.";

    public static void main(String[] args)
            throws InvocationTargetException, IllegalAccessException, ClassNotFoundException, InstantiationException,
            MyJunitInvocationException, MyJunitClassificationException, MalformedURLException {
        if (args.length != 1) {
            System.err.println(USAGE);
            return;
        }

        invoke(args[0]);
    }

    public static void invoke(String path)
            throws IllegalAccessException, InvocationTargetException, ClassNotFoundException, InstantiationException,
            MyJunitInvocationException, MyJunitClassificationException, MalformedURLException {
        path = ensureSlash(path);

        File classPath = new File(path);
        URL classesURL = new URL("file:" + path);
        ClassLoader classLoader = new URLClassLoader(new URL[] {classesURL});

        for (File file : FileUtils.listFiles(classPath, new String[]{"class"}, true)) {
            String className = Utils.getClassName(classPath, file);
            Class<?> testClass = classLoader.loadClass(className);
            invoke(testClass);
        }
    }

    private static String ensureSlash(String path) {
        return path.endsWith(separator) ? path : path + separator;
    }

    public static void invoke(Class<?> testClass)
            throws IllegalAccessException, InvocationTargetException, InstantiationException,
            MyJunitInvocationException, MyJunitClassificationException {
        MyJunitClassifier classifier = new MyJunitClassifier();
        for (Method method : testClass.getDeclaredMethods()) {
            boolean isMyJunitMethod = classifier.classify(method);
            if (isMyJunitMethod && method.getParameterCount() != 0) {
                throw new MyJunitInvocationException(method.getName() + " shouldn't have parameters.");
            }
        }

        try {
            Constructor<?> constructor = testClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object instance = constructor.newInstance();
            run(instance, classifier);
        } catch (NoSuchMethodException e) {
            throw new MyJunitInvocationException(testClass.getName() + " should have default constructor.");
        }
    }

    private static void run(Object testClass, MyJunitClassifier classifier)
            throws IllegalAccessException, InvocationTargetException {
        for (Method m : classifier.getBeforeAll()) {
            m.setAccessible(true);
            m.invoke(testClass);
        }

        ArrayList<Method> beforeEach = classifier.getBeforeEach();
        beforeEach.forEach(m -> m.setAccessible(true));

        ArrayList<Method> afterEach = classifier.getAfterEach();
        afterEach.forEach(m -> m.setAccessible(true));

        for (Method m : classifier.getEnabledTests()) {
            for (Method before : beforeEach) {
                before.invoke(testClass);
            }
            m.setAccessible(true);
            try {
                m.invoke(testClass);
            } catch (Throwable t) {
                if (t.getClass() != m.getAnnotation(Test.class).expected()) {
                    throw t;
                }
            }
            for (Method method : afterEach) {
                method.invoke(testClass);
            }
        }

        for (Method m : classifier.getAfterAll()) {
            m.setAccessible(true);
            m.invoke(testClass);
        }
    }
}
