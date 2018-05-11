package ru.spbau.mit.oquechy.myjunit;

import org.junit.jupiter.api.Test;
import ru.spbau.mit.oquechy.myjunit.examples.IncompatibleAnnotations;
import ru.spbau.mit.oquechy.myjunit.examples.MethodWithParameters;
import ru.spbau.mit.oquechy.myjunit.examples.NoDefaultConstructor;
import ru.spbau.mit.oquechy.myjunit.examples.Valid;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.LinkedList;

import static java.io.File.separator;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MyJunitInvokerTest {

    private final static String PATH_TO_CLASSES = MyJunitInvokerTest.class.getResource("classes").getPath();
    private final static String PATH_TO_VALID = PATH_TO_CLASSES + separator + "valid";
    private final static String PATH_TO_BAD_INVOKE = PATH_TO_CLASSES + separator + "badinvoke";
    private final static String PATH_TO_BAD_CLASSIFY = PATH_TO_CLASSES + separator + "badclassify";

    @Test
    void validFromClassFile() throws IllegalAccessException, MyJunitInvocationException, InstantiationException,
            MyJunitClassificationException, MalformedURLException, InvocationTargetException, ClassNotFoundException {
        MyJunitInvoker.invokeTests(PATH_TO_VALID);
    }

    @Test
    void badInvokeFromClassFile() {
        assertThrows(MyJunitInvocationException.class, () -> MyJunitInvoker.invokeTests(PATH_TO_BAD_INVOKE));
    }

    @Test
    void badClassifyFromClassFile() {
        assertThrows(MyJunitClassificationException.class, () -> MyJunitInvoker.invokeTests(PATH_TO_BAD_CLASSIFY));
    }

    @Test
    void validFromClassPath() throws InvocationTargetException, IllegalAccessException,
            MyJunitInvocationException, MyJunitClassificationException, InstantiationException {
        Class<?> valid = Valid.class;
        MyJunitInvoker.invoke(valid);
    }

    @Test
    void noDefaultConstructor() {
        Class<?> invalid = NoDefaultConstructor.class;
        assertThrows(MyJunitInvocationException.class, () -> MyJunitInvoker.invoke(invalid));
    }

    @Test
    void methodWithParameters() {
        Class<?> invalid = MethodWithParameters.class;
        assertThrows(MyJunitInvocationException.class, () -> MyJunitInvoker.invoke(invalid));
    }

    @Test
    void incompatibleAnnotations() {
        Class<?> invalid = IncompatibleAnnotations.class;
        assertThrows(MyJunitClassificationException.class, () -> MyJunitInvoker.invoke(invalid));
    }
}
