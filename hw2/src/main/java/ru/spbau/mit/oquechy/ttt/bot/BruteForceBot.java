package ru.spbau.mit.oquechy.ttt.bot;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.mit.oquechy.ttt.logic.Model;
import ru.spbau.mit.oquechy.ttt.logic.Sign;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Chooses new move for nought to reach the best result
 * of all possible worst cases.
 */
public class BruteForceBot implements Bot {
    private final static int MOVES = Model.SIZE * Model.SIZE;

    private final static int INCORRECT = -2;
    private final static int LOSS = -1;
    private final static int DRAW = 0;
    private final static int WIN = 1;

    @NotNull
    private Sign myType = Sign.O;

    private Model model;
    @NotNull
    private Sign[][] field = new Sign[Model.SIZE][Model.SIZE];

    /**
     * Takes a model to ask it about current field state.
     */
    public BruteForceBot(Model model) {
        this.model = model;
    }

    /**
     * Emulates all possible games and chooses move for
     * the greatest result in worse case.
     * @return new move
     */
    @Override
    public int newMove() {
        copyField();

        int movePriority = INCORRECT;
        int move = 0;

        for (int i = 0, p = getPriority(i); i < MOVES; i++, p = getPriority(i)) {
            if (p > movePriority) {
                movePriority = p;
                move = i;
            }
        }

        return move;
    }

    private void copyField() {
        for (int i = 0; i < Model.SIZE; i++) {
            for (int j = 0; j < Model.SIZE; j++) {
                field[i][j] = model.getSign(i, j);
            }
        }
    }

    private int getPriority(int move) {
        if (isIncorrectMove(move)) {
            return INCORRECT;
        }

        doMove(move, myType);

        @Nullable Sign result = Model.getResult(field);
        if (result != null) {
            undoMove(move);
            return result == myType ? WIN : result == Sign.N ? DRAW : LOSS;
        }

        int worstMovePriority = WIN;                     // choosing a move to have the best result
        for (int i = 0; i < MOVES; i++) {                // for all possible opponent's moves

            if (isIncorrectMove(i)) {
                continue;
            }

            doMove(i, myType.flip());
            result = Model.getResult(field);

            if (result != null) {
                undoMove(i);
                int p = result == myType ? WIN : result == Sign.N ? DRAW : LOSS;
                worstMovePriority = min(worstMovePriority, p);            // updating lower bound
            } else {                                                      // in case opponent's move is last
                int bestMovePriority = LOSS;
                for (int j = 0; j < MOVES; j++) {
                    int p = getPriority(j);
                    bestMovePriority = max(bestMovePriority, p);
                }

                undoMove(i);
                worstMovePriority = min(worstMovePriority, bestMovePriority); // updating lower bound
            }                                                                 // with best strategy
        }

        undoMove(move);

        return worstMovePriority;
    }

    private boolean isIncorrectMove(int move) {
        return !(0 <= move && move < MOVES && !isBusy(move));
    }

    private void undoMove(int move) {
        if (0 <= move && move < MOVES) {
            field[move / Model.SIZE][move % Model.SIZE] = Sign.N;
        }
    }

    private void doMove(int move, Sign sign) {
        field[move / Model.SIZE][move % Model.SIZE] = sign;
    }

    private boolean isBusy(int move) {
        return model.isBusy(move) || field[move / Model.SIZE][move % Model.SIZE] != Sign.N;
    }
}
