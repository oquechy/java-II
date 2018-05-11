package ru.spbau.mit.oquechy.myjunit;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import static java.io.File.separator;

/**
 * Class for loading testing classes and invoking methods annotated as tests
 * via MyJunit annotations.
 */
public class MyJunitInvoker {

    private final static String USAGE = "Pass the path to the directory with .class files.";

    /**
     * Console application for invoking tests. The tests should be methods of the
     * class with default constructor. Each test method shouldn't take any arguments.
     * Use annotations from the package ru.spbau.mit.oquechy.myjunit.annotations to
     * indicate test methods.
     * <p>
     * The only parameter is the path to the directory with .class files.
     * Application will load described classes from the directory and then run them.
     * <p>
     * Tests are invoked sequentially.
     *
     * @param args path to classes
     * @throws MyJunitInvocationException     if invalid test declaration found
     * @throws MyJunitClassificationException if incorrect combination of annotations was met in the test class
     * @throws InstantiationException         if test class instance creating failed
     * @throws ClassNotFoundException         if test class loading failed
     * @throws InvocationTargetException      if invocation of constructor, setup or tear down method
     *                                        ends up with exception
     * @throws MalformedURLException          if path is incorrect
     * @throws IllegalAccessException         if private method was called
     */
    public static void main(String[] args) throws IllegalAccessException, MyJunitInvocationException,
            MyJunitClassificationException, InstantiationException, ClassNotFoundException,
            InvocationTargetException, MalformedURLException {
        if (args.length != 1) {
            System.err.println(USAGE);
            return;
        }

        invokeTests(args[0]);
    }

    /**
     * Invokes tests from the specified location. The tests should be methods of the
     * class with default constructor. Each test method shouldn't take any arguments.
     * Use annotations from the package ru.spbau.mit.oquechy.myjunit.annotations to
     * indicate test methods.
     * <p>
     * The only string parameter is the path to the directory with .class files.
     * These classes will be loaded and invoked.
     * <p>
     * Tests are invoked sequentially.
     *
     * @param path path to classes
     * @throws MyJunitInvocationException     if invalid test declaration found
     * @throws MyJunitClassificationException if incorrect combination of annotations was met in the test class
     * @throws InstantiationException         if test class instance creating failed
     * @throws ClassNotFoundException         if test class loading failed
     * @throws InvocationTargetException      if invocation of constructor, setup or tear down method
     *                                        ends up with exception
     * @throws MalformedURLException          if path is incorrect
     * @throws IllegalAccessException         if private method was called
     */
    public static void invokeTests(String path) throws MyJunitInvocationException, MyJunitClassificationException,
            InstantiationException, IllegalAccessException, InvocationTargetException,
            MalformedURLException, ClassNotFoundException {

        @NotNull ArrayList<Class<?>> classes = loadClasses(path);

        for (@NotNull Class<?> c : classes) {
            invoke(c);
        }
    }

    /**
     * Invokes tests methods of the specified class. The class should have a
     * default constructor. Each method annotated as test shouldn't take any arguments.
     *
     * @param c test class to be instantiated and invoked
     * @throws MyJunitInvocationException     if invalid test declaration found
     * @throws MyJunitClassificationException if incorrect combination of annotations was met in the test class
     * @throws InstantiationException         if test class instance creating failed
     * @throws InvocationTargetException      if invocation of constructor, setup or tear down method
     *                                        ends up with exception
     * @throws IllegalAccessException         if private method was called
     */
    public static void invoke(@NotNull Class<?> c) throws MyJunitInvocationException, MyJunitClassificationException,
            InstantiationException, InvocationTargetException, IllegalAccessException {
        @NotNull MyJunitClassifier classifier = validate(c);
        @NotNull Object instance = getInstance(c);
        @NotNull Logger logger = new Logger();
        run(instance, classifier, logger);
    }

    @NotNull
    private static ArrayList<Class<?>> loadClasses(String path) throws MalformedURLException, ClassNotFoundException {
        path = ensureSlash(path);

        @NotNull File classPath = new File(path);
        @NotNull URL classesURL = new URL("file:" + path);
        @NotNull ClassLoader classLoader = new URLClassLoader(new URL[]{classesURL});

        @NotNull ArrayList<Class<?>> classes = new ArrayList<>();
        for (@NotNull File file : FileUtils.listFiles(classPath, new String[]{"class"}, true)) {
            @NotNull String className = Utils.getClassName(classPath, file);
            Class<?> testClass = classLoader.loadClass(className);
            classes.add(testClass);
        }
        return classes;
    }

    @NotNull
    private static String ensureSlash(String path) {
        return path.endsWith(separator) ? path : path + separator;
    }

    private static Object getInstance(Class<?> testClass)
            throws IllegalAccessException, InvocationTargetException, InstantiationException,
            MyJunitInvocationException {

        try {
            Constructor<?> constructor = testClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (NoSuchMethodException e) {
            throw new MyJunitInvocationException(testClass.getName() + " should have default constructor.");
        }
    }

    @NotNull
    private static MyJunitClassifier validate(Class<?> testClass)
            throws MyJunitInvocationException, MyJunitClassificationException {
        @NotNull MyJunitClassifier classifier = new MyJunitClassifier();
        for (@NotNull Method method : testClass.getDeclaredMethods()) {
            boolean isMyJunitMethod = classifier.classify(method);

            if (isMyJunitMethod && method.getParameterCount() != 0) {
                throw new MyJunitInvocationException(method.getName() + " shouldn't have parameters.");
            }
        }
        return classifier;
    }

    private static void run(Object testClass, MyJunitClassifier classifier, Logger logger)
            throws InvocationTargetException, IllegalAccessException {

        logger.start();

        for (@NotNull Method m : classifier.getBeforeAll()) {
            m.invoke(testClass);
        }

        ArrayList<Method> beforeEach = classifier.getBeforeEach();
        ArrayList<Method> afterEach = classifier.getAfterEach();

        for (@NotNull Method m : classifier.getEnabledTests()) {
            for (@NotNull Method before : beforeEach) {
                before.invoke(testClass);
            }

            try {
                logger.startTest(m);
                m.invoke(testClass);
            } catch (Throwable t) {
                logger.registerException(t.getCause());
            } finally {
                logger.finishTest();
            }

            for (@NotNull Method method : afterEach) {
                method.invoke(testClass);
            }
        }

        for (@NotNull Method m : classifier.getAfterAll()) {
            m.invoke(testClass);
        }

        for (@NotNull Method m : classifier.getDisabledTests()) {
            logger.ignore(m);
        }

        logger.finish();

    }
}
