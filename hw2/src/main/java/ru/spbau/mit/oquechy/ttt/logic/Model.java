package ru.spbau.mit.oquechy.ttt.logic;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.mit.oquechy.ttt.Controller;

/**
 * Game logic
 */
public class Model {

    /**
     * Width and height of the field
     */
    public final static int SIZE = 3;

    private Controller controller;
    private int moveCounter = 0;

    /**
     * Creates empty field for a new game
     * @param controller of the application
     */
    public Model(Controller controller) {
        this.controller = controller;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                field[i][j] = Sign.N;
            }
        }
    }

    /**
     * Verifies new move got from user or bot.
     * If move is correct, asks controller to draw it
     * and runs check for end of game.
     * @param i index of new move from [0..8]
     * @return {@code true} if move is correct and
     * {@code false} otherwise
     */
    public boolean checkMove(int i) {
        int y = i / SIZE;
        int x = i % SIZE;
        if (field[y][x] == Sign.N) {
            field[y][x] = currentSign;
            controller.writeSign(y, x, currentSign);
            checkWin();
            currentSign = currentSign.flip();
            moveCounter++;
            return true;
        } else {
            return false;
        }
    }

    private void checkWin() {
        @Nullable Sign result = getResult(field);

        if (result != null) {
            controller.writeWinner(result);
        }
    }

    /**
     * Checks for end of game on a given field.
     * @param field filed to be checked
     * @return {@code Sign} of the winner or null
     * if game is still continue
     */
    public static Sign getResult(Sign[][] field) {
        // rows
        for (int i = 0; i < SIZE; i++) {
            boolean win = field[i][0] != Sign.N;
            for (int j = 1; j < SIZE; j++) {
                win &= field[i][j] == field[i][0];
            }

            if (win) {
                return field[i][0];
            }
        }

        // cols
        for (int i = 0; i < SIZE; i++) {
            boolean win = field[0][i] != Sign.N;
            for (int j = 1; j < SIZE; j++) {
                win &= field[j][i] == field[0][i];
            }

            if (win) {
                return field[0][i];
            }
        }

        // diagonals
        boolean win = field[1][1] != Sign.N;
        for (int i = 1; i < SIZE; i++) {
            win &= field[i][i] == field[0][0];
        }

        if (win) {
            return field[1][1];
        }

        win = field[1][1] != Sign.N;
        for (int i = 1; i < SIZE; i++) {
            win &= field[i][SIZE - i - 1] == field[0][SIZE - 1];
        }

        if (win) {
            return field[1][1];
        } else if (filled(field)) {
            return Sign.N;
        }

        return null;
    }

    private static boolean filled(Sign[][] field) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (field[i][j] == Sign.N) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Checks whether the given cell is filled with X or O.
     * @param i index of the cell from [0..8]
     * @return {@code true} if cell is busy
     * and {@code false} otherwise
     */
    public boolean isBusy(int i) {
        return field[i / SIZE][i % SIZE] != Sign.N;
    }

    /**
     * Returns current sign of specified field position.
     * @param y first coordinate
     * @param x second coordinate
     */
    public Sign getSign(int y, int x) {
        return field[y][x];
    }

    @NotNull
    private Sign currentSign = Sign.X;

    @NotNull
    private Sign[][] field = new Sign[SIZE][SIZE];

    /**
     * Returns total number of moves in the round.
     */
    public int getMoveCounter() {
        return moveCounter;
    }
}
