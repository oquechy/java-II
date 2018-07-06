package ru.spbau.mit.oquechy.ttt.bot;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.mit.oquechy.ttt.logic.Model;
import ru.spbau.mit.oquechy.ttt.logic.Position;
import ru.spbau.mit.oquechy.ttt.logic.Sign;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Chooses new move for nought to reach the best result
 * of all possible worst cases.
 */
public class BruteForceBot implements Bot {
    private final static int MOVES = Model.ROW * Model.ROW;

    private final static int INCORRECT = -2;
    private final static int LOSS = -1;
    private final static int DRAW = 0;
    private final static int WIN = 1;

    @NotNull
    private Sign myType = Sign.O;

    final private Model model;
    @NotNull
    final private Sign[][] field = new Sign[Model.ROW][Model.ROW];

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
    @NotNull
    @Override
    public Position newMove() {
        copyField();

        int movePriority = INCORRECT;
        int move = 0;

        for (int i = 0; i < MOVES; i++) {
            @NotNull Position position = new Position(i / Model.ROW, i % Model.ROW);
            int p = getPriority(position);
            if (p > movePriority) {
                movePriority = p;
                move = i;
            }
        }

        return new Position(move / Model.ROW, move % Model.ROW);
    }

    private void copyField() {
        for (int i = 0; i < Model.ROW; i++) {
            for (int j = 0; j < Model.ROW; j++) {
                field[i][j] = model.getSign(i, j);
            }
        }
    }

    private int getPriority(@NotNull Position position) {
        if (isIncorrectMove(position)) {
            return INCORRECT;
        }

        doMove(position, myType);

        @Nullable Sign result = Model.getResult(field);
        if (result != null) {
            undoMove(position);
            return result == myType ? WIN : result == Sign.N ? DRAW : LOSS;
        }

        int worstMovePriority = WIN;                     // choosing a move to have the best result
        for (int i = 0; i < MOVES; i++) {                // for all possible opponent's moves

            @NotNull Position move = new Position(i / Model.ROW, i % Model.ROW);

            if (isIncorrectMove(move)) {
                continue;
            }

            doMove(move, myType.flip());
            result = Model.getResult(field);

            if (result != null) {
                undoMove(move);
                int p = result == myType ? WIN : result == Sign.N ? DRAW : LOSS;
                worstMovePriority = min(worstMovePriority, p);            // updating lower bound
            } else {                                                      // in case opponent's move is last
                int bestMovePriority = LOSS;
                for (int j = 0; j < MOVES; j++) {
                    @NotNull Position p = new Position(j / Model.ROW, j % Model.ROW);
                    int priority = getPriority(p);
                    bestMovePriority = max(bestMovePriority, priority);
                }

                undoMove(move);
                worstMovePriority = min(worstMovePriority, bestMovePriority); // updating lower bound
            }                                                                 // with best strategy
        }

        undoMove(position);

        return worstMovePriority;
    }

    private boolean isIncorrectMove(Position move) {
        return !(move.onField() && !isBusy(move));
    }

    private void undoMove(Position move) {
        if (move.onField()) {
            field[move.getX()][move.getY()] = Sign.N;
        }
    }

    private void doMove(Position move, Sign sign) {
        field[move.getX()][move.getY()] = sign;
    }

    private boolean isBusy(@NotNull Position move) {
        return model.isBusy(move) || field[move.getX()][move.getY()] != Sign.N;
    }
}
