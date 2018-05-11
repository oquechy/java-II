package ru.spbau.mit.oquechy.myjunit.examples;

import org.jetbrains.annotations.NotNull;
import ru.spbau.mit.oquechy.myjunit.annotations.*;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unused"})
public class Valid {
    @NotNull
    private List<String> INVOKED = new LinkedList<>();

    private final static int ENABLED_TESTS_COUNT = 4;
    private final static int BEFORE_COUNT = 2;
    private final static int AFTER_COUNT = 2;
    private final static int AFTER_ALL_COUNT = 1;
    private final static int BEFORE_ALL_COUNT = 2;

    private final static String NOT_A_TEST = "Not A Test!";
    private final static String IGNORED_1 = "Ignored#1";
    private final static String IGNORED_2 = "Ignored#2";
    private final static String BEFORE_1 = "Before#1";
    private final static String BEFORE_2 = "Before#2";
    private final static String BEFORE_CLASS_2 = "BeforeClass#2";
    private final static String BEFORE_CLASS_1 = "BeforeClass#1";
    private final static String AFTER_CLASS_1 = "AfterClass#1";
    private final static String AFTER_2 = "After#2";
    private final static String AFTER_1 = "After#1";
    private final static String TEST = "Test";
    private final static String TEST_MISSED_EX = "Test(expected = IllegalArgumentException.class)";
    private final static String TEST_WRONG_EX = "Test(expected = ClassNotFoundException.class)";
    private final static String TEST_EX = "Test(expected = NullPointerException.class)";

    @BeforeClass
    public void bc1() {
        System.out.println(BEFORE_CLASS_1);
        INVOKED.add(BEFORE_CLASS_1);
    }

    @BeforeClass
    private void bc2() {
        System.out.println(BEFORE_CLASS_2);
        INVOKED.add(BEFORE_CLASS_2);
    }

    @Before
    public void b1() {
        System.out.println(BEFORE_1);
        INVOKED.add(BEFORE_1);
    }

    @Before
    private void b2() {
        System.out.println(BEFORE_2);
        INVOKED.add(BEFORE_2);
    }

    @Test(expected = NullPointerException.class)
    public void te() {
        System.out.println(TEST_EX);
        INVOKED.add(TEST_EX);
        throw new NullPointerException();
    }

    @Test(expected = IllegalArgumentException.class)
    public void tne() {
        System.out.println(TEST_MISSED_EX);
        INVOKED.add(TEST_MISSED_EX);
    }

    @Test(expected = ClassNotFoundException.class)
    public void tea() {
        System.out.println(TEST_WRONG_EX);
        INVOKED.add(TEST_WRONG_EX);
        throw new NullPointerException();
    }

    @Test
    private void t() {
        System.out.println(TEST);
        INVOKED.add(TEST);
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
        System.out.println(AFTER_1);
        INVOKED.add(AFTER_1);
    }

    @After
    private void a2() {
        System.out.println(AFTER_2);
        INVOKED.add(AFTER_2);
    }

    @AfterClass
    public void ac() {
        System.out.println(AFTER_CLASS_1);
        INVOKED.add(AFTER_CLASS_1);

        validate();
    }

    private void validate() {
        assertThat(INVOKED.subList(0, 2), containsInAnyOrder(BEFORE_CLASS_1, BEFORE_CLASS_2));
        assertThat(INVOKED.get(INVOKED.size() - 1), is(AFTER_CLASS_1));
        INVOKED = INVOKED.subList(2, INVOKED.size() - 1);

        int wrappedTest = 1 + BEFORE_COUNT + AFTER_COUNT;
        assertThat(INVOKED.size(), is(ENABLED_TESTS_COUNT * wrappedTest));

        for (int i = 0; i < INVOKED.size(); i += wrappedTest) {
            assertThat(INVOKED.subList(i, i + BEFORE_COUNT), containsInAnyOrder(BEFORE_1, BEFORE_2));
            assertThat(INVOKED.subList(i + wrappedTest - AFTER_COUNT, i + wrappedTest),
                    containsInAnyOrder(AFTER_1, AFTER_2));
        }

        assertThat(INVOKED, hasItems(TEST, TEST_EX, TEST_MISSED_EX, TEST_WRONG_EX));
        INVOKED.clear();
    }

    public void notATest() {
        System.out.println(NOT_A_TEST);
        INVOKED.add(NOT_A_TEST);
    }
}
