package ru.spbau.mit.oquechy.myjunit;

import com.google.common.base.Stopwatch;
import ru.spbau.mit.oquechy.myjunit.annotations.Test;

import java.lang.reflect.Method;

import static java.lang.Integer.max;

/**
 * Class for decorating test results and computing statistics.
 * Methods should be called in order:
 *  - {@code Logger::start}
 *  - ...
 *  - {@code Logger::startTest}
 *  - {@code Logger::registerException} (optional)
 *  - {@code Logger::finishTest}
 *  - ...
 *  - {@code Logger::finish}
 * <p>
 * Method {@code Logger::ignore} can be called anywhere
 * between {@code Logger::start} and {@code Logger::finish}.
 */
public class Logger {

    private Stopwatch generalClock = Stopwatch.createUnstarted();
    private Stopwatch clock = Stopwatch.createUnstarted();

    private Method method;
    private boolean exceptionHandled;
    private boolean failed;

    private int passedCount = 0;
    private int failedCount = 0;
    private int ignoredCount = 0;

    /**
     * Should be called when testing is started. Sets up a timer.
     */
    public void start() {
        generalClock.start();
    }

    /**
     * Should be called before starting a new test method. Starts
     * timer for one method.
     *
     * @param method method to be measured
     */
    public void startTest(Method method) {
        this.method = method;
        exceptionHandled = false;
        failed = false;
        printStyledString("Starting test " + method.getName() + ".", ANSIStyle.YELLOW);
        clock.start();
    }

    private void printStyledString(String s, String style) {
        System.out.print(style);
        int n = 0;
        for (String line : s.split("\n")) {
            n = max(n, line.length());
        }

        for (int i = 0; i < n; i++) {
            System.out.print("=");
        }
        System.out.println();
        System.out.print(s);
        System.out.println();
        for (int i = 0; i < n; i++) {
            System.out.print("=");
        }
        System.out.println(ANSIStyle.RESET);
    }

    /**
     * Should be called after finishing the current test method.
     * Stops the timer and logs result.
     */
    public void finishTest() {
        Stopwatch reset = clock.reset();
        Class<? extends Throwable> expected = method.getAnnotation(Test.class).expected();
        if (expected != Test.None.class && !exceptionHandled) {
            printStyledString("Expected but wasn't thrown: " + expected.getName(), ANSIStyle.RED);
            failed = true;
        }
        printStyledString("Time elapsed: " + reset + ".", ANSIStyle.YELLOW);
        printStyledString(failed ? "FAILED" : "PASSED", failed ? ANSIStyle.RED : ANSIStyle.GREEN);
        failedCount += failed ? 1 : 0;
        passedCount += failed ? 0 : 1;
    }

    /**
     * Logs information about an exception.
     *
     * @param cause {@link Throwable} thrown by the method call
     */
    public void registerException(Throwable cause) {
        exceptionHandled = true;
        Class<? extends Throwable> expected = method.getAnnotation(Test.class).expected();
        Class<? extends Throwable> actual = cause.getClass();
        if (actual != expected) {
            printStyledString("Expected " + expected.getName() + " but actually was: ", ANSIStyle.RED);
            stackTrace(cause);
            failed = true;
        }
    }

    private void stackTrace(Throwable cause) {
        System.out.print(ANSIStyle.RED);
        cause.printStackTrace(System.out);
        System.out.println(ANSIStyle.RESET);
    }

    /**
     * Prints final statistic.
     */
    public void finish() {
        printStyledString("Total time: " + generalClock.stop(), ANSIStyle.YELLOW);
        printStyledString("Passed: " + passedCount + "\n" +
                "Failed: " + failedCount + "\n" +
                "Ignored: " + ignoredCount + "\n" +
                "Total: " + (passedCount + failedCount + ignoredCount), ANSIStyle.BLUE);
    }

    /**
     * Logs information about the ignored method.
     *
     * @param m method to be ignored
     */
    public void ignore(Method m) {
        String cause = m.getAnnotation(Test.class).ignore();
        printStyledString("Test " + m.getName() + " disabled. Cause: " + cause, ANSIStyle.CYAN);
        ignoredCount++;
    }

    private static class ANSIStyle {
        private static final String RESET = "\u001b[0m ";
        private static final String RED = "\u001B[31m";
        private static final String GREEN = "\u001B[32m";
        private static final String YELLOW = "\u001B[33m";
        private static final String BLUE = "\u001B[34m";
        private static final String CYAN = "\u001B[36m";
    }
}
