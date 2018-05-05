package ru.spbau.mit.oquechy.ttt.logic;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static ru.spbau.mit.oquechy.ttt.logic.Sign.*;

class ModelTest {
    private final static Sign[][] EMPTY = {
            {N, N, N},
            {N, N, N},
            {N, N, N}
    };
    private Model model;

    @BeforeEach
    void setup() {
        model = new Model();
    }

    @Test
    void constructor() {
        assertFieldEqualsTo(EMPTY);
    }

    //   ...    O..    O..    ?..    O..
    //   .X. -> .X. -> .?. -> .X. -> .X.
    //   ...    ...    ...    ...    .X.
    @Test
    void checkAndSetMove() {

        @NotNull Sign[][] expectedF1 = {
                {N, N, N},
                {N, X, N},
                {N, N, N}
        };

        @NotNull Sign[][] expectedF2 = {
                {O, N, N},
                {N, X, N},
                {N, N, N}
        };

        @NotNull Sign[][] expectedF3 = {
                {O, N, N},
                {N, X, N},
                {N, X, N}
        };

        Position[] positions = {
                new Position(1, 1), new Position(0, 0),
                new Position(1, 1), new Position(0, 0),
                new Position(2, 1)
        };

        assertThat(model.checkAndSetMove(positions[0]), equalTo(true));
        assertFieldEqualsTo(expectedF1);
        assertThat(model.checkAndSetMove(positions[1]), equalTo(true));
        assertFieldEqualsTo(expectedF2);
        assertThat(model.checkAndSetMove(positions[2]), equalTo(false));
        assertFieldEqualsTo(expectedF2);
        assertThat(model.checkAndSetMove(positions[3]), equalTo(false));
        assertFieldEqualsTo(expectedF2);
        model.checkAndSetMove(positions[4]);
        assertFieldEqualsTo(expectedF3);
    }

    //   ...    O..    O.X    O.X    O.X    O.X
    //   .X. -> .X. -> .X. -> OX. -> OX. -> OX.
    //   ...    ...    ...    ...    .X.    OX.
    @Test
    void checkWin() {
        Position[] positions = {
                new Position(1, 1), new Position(0, 0),
                new Position(0, 2), new Position(1, 0),
                new Position(2, 1), new Position(2, 0)
        };

        assertThat(model.checkWin(), is(false));
        model.checkAndSetMove(positions[0]);
        assertThat(model.checkWin(), is(false));
        model.checkAndSetMove(positions[1]);
        assertThat(model.checkWin(), is(false));
        model.checkAndSetMove(positions[2]);
        assertThat(model.checkWin(), is(false));
        model.checkAndSetMove(positions[3]);
        assertThat(model.checkWin(), is(false));
        model.checkAndSetMove(positions[4]);
        assertThat(model.checkWin(), is(false));
        model.checkAndSetMove(positions[5]);
        assertThat(model.checkWin(), is(true));
    }

    @Test
    void getResult() {
        @NotNull Sign[][] draw = {
                {O, X, O},
                {O, X, X},
                {X, O, O}
        };

        @NotNull Sign[][] continuous = {
                {O, N, N},
                {N, X, X},
                {N, O, N}
        };

        @NotNull Sign[][] nought = {
                {X, O, O},
                {N, X, O},
                {X, X, O}
        };

        @NotNull Sign[][] cross = {
                {X, O, O},
                {X, X, X},
                {O, X, O}
        };

        assertThat(Model.getResult(draw), is(N));
        assertThat(Model.getResult(continuous), is(nullValue()));
        assertThat(Model.getResult(nought), is(O));
        assertThat(Model.getResult(cross), is(X));
    }

    @Test
    void isBusy() {
        Position[] positions = {
                new Position(0, 0), new Position(2, 2),
                new Position(0, 0), new Position(1, 1),
                new Position(2, 2)
        };

        model.checkAndSetMove(positions[0]);
        model.checkAndSetMove(positions[1]);

        assertThat(model.isBusy(positions[2]), is(true));
        assertThat(model.isBusy(positions[3]), is(false));
        assertThat(model.isBusy(positions[4]), is(true));
    }

    @Test
    void getSign() {
        @NotNull Sign[][] field = {
                {X, O, X},
                {N, X, O},
                {N, O, X}
        };

        Position[] positions = {
                new Position(0, 0), new Position(0, 1),
                new Position(0, 2), new Position(1, 2),
                new Position(1, 1), new Position(2, 1),
                new Position(2, 2)
        };


        model.checkAndSetMove(positions[0]);
        model.checkAndSetMove(positions[1]);
        model.checkAndSetMove(positions[2]);
        model.checkAndSetMove(positions[3]);
        model.checkAndSetMove(positions[4]);
        model.checkAndSetMove(positions[5]);
        model.checkAndSetMove(positions[6]);

        for (int i = 0; i < Model.ROW; i++) {
            for (int j = 0; j < Model.ROW; j++) {
                assertThat(model.getSign(i, j), equalTo(field[i][j]));
            }
        }
    }

    @Test
    void getMoveCounter() {
        Position[] positions = {
                new Position(0, 0), new Position(0, 1),
                new Position(0, 2), new Position(1, 0),
                new Position(1, 0), new Position(0, 2),
                new Position(1, 1)
        };

        assertThat(model.getMoveCounter(), is(0));
        model.checkAndSetMove(positions[0]);
        assertThat(model.getMoveCounter(), is(1));
        model.checkAndSetMove(positions[1]);
        assertThat(model.getMoveCounter(), is(2));
        model.checkAndSetMove(positions[2]);
        assertThat(model.getMoveCounter(), is(3));
        model.checkAndSetMove(positions[3]);
        assertThat(model.getMoveCounter(), is(4));
        model.checkAndSetMove(positions[4]);
        assertThat(model.getMoveCounter(), is(4));
        model.checkAndSetMove(positions[5]);
        assertThat(model.getMoveCounter(), is(4));
        model.checkAndSetMove(positions[6]);
        assertThat(model.getMoveCounter(), is(5));
    }

    private void assertFieldEqualsTo(Sign[][] expectedF) {
        for (int i = 0; i < Model.ROW; i++) {
            for (int j = 0; j < Model.ROW; j++) {
                assertThat(model.getSign(i, j), is(expectedF[i][j]));
            }
        }
    }
}
