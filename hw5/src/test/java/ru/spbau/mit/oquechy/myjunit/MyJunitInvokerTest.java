package ru.spbau.mit.oquechy.myjunit;

import org.junit.jupiter.api.Test;
import ru.spbau.mit.oquechy.myjunit.examples.Valid;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;

import static java.io.File.separator;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MyJunitInvokerTest {

    private String PATH_TO_CLASS = getClass().getResource("classes").getPath();
    private String PATH_TO_JAR = getClass().getResource("test.jar").getPath();

    @Test
    void validFromPath() throws ClassNotFoundException, InvocationTargetException,
            MyJunitClassificationException, InstantiationException, MyJunitInvocationException, IllegalAccessException,
            MalformedURLException {
        MyJunitInvoker.invoke(PATH_TO_CLASS);
        assertCorrectTests();
        Valid.INVOKED.clear();
    }

    @Test
    void validFromJar() throws ClassNotFoundException, InvocationTargetException,
            MyJunitClassificationException, InstantiationException, MyJunitInvocationException, IllegalAccessException,
            MalformedURLException {
        MyJunitInvoker.invoke(PATH_TO_JAR);
        assertCorrectTests();
        Valid.INVOKED.clear();
    }

    @Test
    void validAnnotationsByClass() throws InvocationTargetException, IllegalAccessException,
            MyJunitInvocationException, MyJunitClassificationException, InstantiationException {
        Class<?> valid = Valid.class;
        MyJunitInvoker.invoke(valid);
        assertCorrectTests();
        Valid.INVOKED.clear();
    }

    private void assertCorrectTests() {
        assertThat(Valid.INVOKED, hasSize(Valid.ENABLED_TESTS_COUNT));
        assertThat(Valid.INVOKED, not(hasItem(Valid.NOT_A_TEST)));
        assertThat(Valid.INVOKED, not(hasItem(Valid.IGNORED_1)));
        assertThat(Valid.INVOKED, not(hasItem(Valid.IGNORED_2)));
    }

    @Test
    void noDefaultConstructor() throws ClassNotFoundException {
        Class<?> invalid = Class.forName("ru.spbau.mit.oquechy.myjunit.examples.NoDefaultConstructor");
        assertThrows(MyJunitInvocationException.class, () -> MyJunitInvoker.invoke(invalid));
    }

    @Test
    void methodWithParameters() throws ClassNotFoundException {
        Class<?> invalid = Class.forName("ru.spbau.mit.oquechy.myjunit.examples.MethodWithParameters");
        assertThrows(MyJunitInvocationException.class, () -> MyJunitInvoker.invoke(invalid));
    }

    @Test
    void incompatibleAnnotations() throws ClassNotFoundException {
        Class<?> invalid = Class.forName("ru.spbau.mit.oquechy.myjunit.examples.IncompatibleAnnotations");
        assertThrows(MyJunitClassificationException.class, () -> MyJunitInvoker.invoke(invalid));
    }
}
