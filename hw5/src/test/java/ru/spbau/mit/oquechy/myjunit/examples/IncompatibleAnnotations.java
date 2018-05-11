package ru.spbau.mit.oquechy.myjunit.examples;

import ru.spbau.mit.oquechy.myjunit.annotations.*;

@SuppressWarnings("unused")
public class IncompatibleAnnotations {

    @Before
    @Test
    public void setUp() { }

    @BeforeClass
    @After
    public void beforeAll() { }

    @AfterClass
    @Test(expected = IllegalAccessException.class)
    public void tearDown() { }
}
