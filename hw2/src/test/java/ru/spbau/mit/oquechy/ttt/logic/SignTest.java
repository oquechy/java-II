package ru.spbau.mit.oquechy.ttt.logic;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

class SignTest {
    @Test
    void flip() {
        assertThat(Sign.O.flip(), is(Sign.X));
        assertThat(Sign.X.flip(), is(Sign.O));
        assertThat(Sign.N.flip(), is(Sign.N));
    }
}