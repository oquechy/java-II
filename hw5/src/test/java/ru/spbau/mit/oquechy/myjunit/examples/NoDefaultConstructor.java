package ru.spbau.mit.oquechy.myjunit.examples;

import ru.spbau.mit.oquechy.myjunit.annotations.Test;

@SuppressWarnings("unused")
public class NoDefaultConstructor {
    public NoDefaultConstructor(Object o) { }

    @Test
    public void test() { }
}
