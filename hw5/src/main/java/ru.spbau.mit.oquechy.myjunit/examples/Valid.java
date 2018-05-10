package ru.spbau.mit.oquechy.myjunit.examples;

import ru.spbau.mit.oquechy.myjunit.annotations.*;

import java.util.HashSet;
import java.util.Set;


@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unused"})
public class Valid {
    public final static Set<String> INVOKED = new HashSet<>();
    public final static String NOT_A_TEST = "Not A Test!";
    public final static int ENABLED_TESTS_COUNT = 11;
    public final static String IGNORED_1 = "Ignored#1";
    public final static String IGNORED_2 = "Ignored#2";

    @BeforeClass
    public void bc1() {
        System.out.println("BeforeClass#1");
        INVOKED.add("BeforeClass#1");
    }

    @BeforeClass
    private void bc2() {
        System.out.println("BeforeClass#2");
        INVOKED.add("BeforeClass#2");
    }

    @Before
    public void b1() {
        System.out.println("Before#1");
        INVOKED.add("Before#1");
    }

    @Before
    private void b2() {
        System.out.println("Before#2");
        INVOKED.add("Before#2");
    }

    @Test(expected = NullPointerException.class)
    public void te() {
        System.out.println("Test(expected = NullPointerException.class)");
        INVOKED.add("Test(expected = NullPointerException.class)");
//        throw new NullPointerException();
    }

    @Test(expected = IllegalArgumentException.class)
    public void tne() {
        System.out.println("Test(expected = IllegalArgumentException.class)");
        INVOKED.add("Test(expected = IllegalArgumentException.class)");
    }

    @Test
    private void t() {
        System.out.println("Test");
        INVOKED.add("Test");
    }

    @Test(ignore = "deprecated")
    public void ti() {
        System.out.println(IGNORED_1);
        INVOKED.add(IGNORED_1);
    }

    @Test(ignore = "deprecated", expected = NullPointerException.class)
    private void tie() {
        System.out.println(IGNORED_2);
        INVOKED.add(IGNORED_2);
    }

    @After
    public void a1() {
        System.out.println("After#1");
        INVOKED.add("After#1");
    }

    @After
    private void a2() {
        System.out.println("After#2");
        INVOKED.add("After#2");
    }

    @AfterClass
    public void ac1() {
        System.out.println("AfterClass#1");
        INVOKED.add("AfterClass#1");
    }

    @AfterClass
    private void ac2() {
        System.out.println("AfterClass#2");
        INVOKED.add("AfterClass#2");
    }

    public void notATest() {
        System.out.println(NOT_A_TEST);
        INVOKED.add(NOT_A_TEST);
    }
}
