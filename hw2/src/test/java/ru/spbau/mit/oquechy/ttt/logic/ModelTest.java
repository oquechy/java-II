package ru.spbau.mit.oquechy.ttt.logic;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.spbau.mit.oquechy.ttt.Controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.*;
import static ru.spbau.mit.oquechy.ttt.logic.Sign.N;
import static ru.spbau.mit.oquechy.ttt.logic.Sign.O;
import static ru.spbau.mit.oquechy.ttt.logic.Sign.X;

class ModelTest {

    @Mock private Controller controller;
    private Model model;

    private final static Sign[][] EMPTY = {
            {N, N, N},
            {N, N, N},
            {N, N, N}
    };

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        model = new Model(controller);
    }

    @Test
    void constructor() {
        assertFieldEqualsTo(EMPTY);
    }

    //   ...    O..    O..    ?..    O..
    //   .X. -> .X. -> .?. -> .X. -> .X.
    //   ...    ...    ...    ...    .X.
    @Test
    void checkMove() {

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

        model.checkMove(positions[0]);
        assertFieldEqualsTo(expectedF1);
        model.checkMove(positions[1]);
        assertFieldEqualsTo(expectedF2);
        model.checkMove(positions[2]);
        assertFieldEqualsTo(expectedF2);
        model.checkMove(positions[3]);
        assertFieldEqualsTo(expectedF2);
        model.checkMove(positions[4]);
        assertFieldEqualsTo(expectedF3);

        InOrder order = inOrder(controller);
        order.verify(controller).writeSign(1, 1, X);
        order.verify(controller).writeSign(0, 0, O);
        order.verify(controller).writeSign(2, 1, X);
        verifyNoMoreInteractions(controller);
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

        model.checkMove(positions[0]);
        model.checkMove(positions[1]);
        model.checkMove(positions[2]);
        model.checkMove(positions[3]);
        model.checkMove(positions[4]);
        model.checkMove(positions[5]);

        InOrder order = inOrder(controller);
        order.verify(controller).writeSign(1, 1, X);
        order.verify(controller).writeSign(0, 0, O);
        order.verify(controller).writeSign(0, 2, X);
        order.verify(controller).writeSign(1, 0, O);
        order.verify(controller).writeSign(2, 1, X);
        order.verify(controller).writeSign(2, 0, O);
        order.verify(controller).writeWinner(O);
        verifyNoMoreInteractions(controller);
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

        model.checkMove(positions[0]);
        model.checkMove(positions[1]);

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


        model.checkMove(positions[0]);
        model.checkMove(positions[1]);
        model.checkMove(positions[2]);
        model.checkMove(positions[3]);
        model.checkMove(positions[4]);
        model.checkMove(positions[5]);
        model.checkMove(positions[6]);

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
        model.checkMove(positions[0]);
        assertThat(model.getMoveCounter(), is(1));
        model.checkMove(positions[1]);
        assertThat(model.getMoveCounter(), is(2));
        model.checkMove(positions[2]);
        assertThat(model.getMoveCounter(), is(3));
        model.checkMove(positions[3]);
        assertThat(model.getMoveCounter(), is(4));
        model.checkMove(positions[4]);
        assertThat(model.getMoveCounter(), is(4));
        model.checkMove(positions[5]);
        assertThat(model.getMoveCounter(), is(4));
        model.checkMove(positions[6]);
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
