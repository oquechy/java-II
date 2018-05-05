package ru.spbau.mit.oquechy.ttt.logic;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Game logic
 */
public class Model {

    /**
     * Width and height of the field
     */
    public final static int ROW = 3;

    private int moveCounter = 0;
    @NotNull
    private Sign currentSign = Sign.X;
    @NotNull
    private Sign[][] field = new Sign[ROW][ROW];

    /**
     * Creates empty field for a new game
     */
    public Model() {
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < ROW; j++) {
                field[i][j] = Sign.N;
            }
        }
    }

    /**
     * Checks for end of game on a given field.
     *
     * @param field filed to be checked
     * @return {@code Sign} of the winner or null
     * if game is still continue
     */
    public static Sign getResult(Sign[][] field) {
        // rows
        for (int i = 0; i < ROW; i++) {
            boolean win = field[i][0] != Sign.N;
            for (int j = 1; j < ROW; j++) {
                win &= field[i][j] == field[i][0];
            }

            if (win) {
                return field[i][0];
            }
        }

        // cols
        for (int i = 0; i < ROW; i++) {
            boolean win = field[0][i] != Sign.N;
            for (int j = 1; j < ROW; j++) {
                win &= field[j][i] == field[0][i];
            }

            if (win) {
                return field[0][i];
            }
        }

        // diagonals
        boolean win = field[1][1] != Sign.N;
        for (int i = 1; i < ROW; i++) {
            win &= field[i][i] == field[0][0];
        }

        if (win) {
            return field[1][1];
        }

        win = field[1][1] != Sign.N;
        for (int i = 1; i < ROW; i++) {
            win &= field[i][ROW - i - 1] == field[0][ROW - 1];
        }

        if (win) {
            return field[1][1];
        } else if (filled(field)) {
            return Sign.N;
        }

        return null;
    }

    private static boolean filled(Sign[][] field) {
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < ROW; j++) {
                if (field[i][j] == Sign.N) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Verifies new move got from user or bot.
     * If move is correct, asks controller to draw it
     * and runs check for end of game.
     *
     * @param position index of new move from [0..8]
     * @return {@code true} if move is correct and
     * {@code false} otherwise
     */
    public boolean checkAndSetMove(Position position) {
        int row = position.getX();
        int col = position.getY();
        if (field[row][col] == Sign.N) {
            field[row][col] = currentSign;
            currentSign = currentSign.flip();
            moveCounter++;
            return true;
        } else {
            return false;
        }
    }

    public boolean checkWin() {
        @Nullable Sign result = getResult(field);
        return result != null;
    }

    /**
     * Returns the winner or null if game wasn't finished.
     */
    public Sign getResult() {
        return getResult(field);
    }

    /**
     * Checks whether the given cell is filled with X or O.
     *
     * @param position index of the cell from [0..8]
     * @return {@code true} if cell is busy
     * and {@code false} otherwise
     */
    public boolean isBusy(Position position) {
        return field[position.getX()][position.getY()] != Sign.N;
    }

    /**
     * Returns current sign of specified field position.
     *
     * @param y first coordinate
     * @param x second coordinate
     */
    public Sign getSign(int y, int x) {
        return field[y][x];
    }

    /**
     * Returns current sign of specified field position.
     *
     * @param position requested position
     */
    public Sign getSign(Position position) {
        return getSign(position.getX(), position.getY());
    }

    /**
     * Returns total number of moves in the round.
     */
    public int getMoveCounter() {
        return moveCounter;
    }
}
