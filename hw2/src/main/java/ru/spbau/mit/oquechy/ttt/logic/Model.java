package ru.spbau.mit.oquechy.ttt.logic;

import ru.spbau.mit.oquechy.ttt.Controller;

public class Model {

    public final static int SIZE = 3;

    private Controller controller;
    private int moveCounter = 0;

    public Model(Controller controller) {
        this.controller = controller;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                field[i][j] = Sign.N;
            }
        }
    }

    public boolean checkMove(int i) {
        int y = i / SIZE;
        int x = i % SIZE;
        if (field[y][x] == Sign.N) {
            field[y][x] = currentSign;
            controller.writeSign(y, x, currentSign);
            checkWin();
            currentSign = currentSign.flip();
            return true;
        } else {
            return false;
        }
    }

    private void checkWin() {
        Sign result = getResult(field);

        if (result != null) {
            controller.writeWinner(result);
        }
    }

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

    public boolean isBusy(int i) {
        return field[i / SIZE][i % SIZE] != Sign.N;
    }

    public Sign getSign(int y, int x) {
        return field[y][x];
    }

    private Sign currentSign = Sign.X;

    private Sign[][] field = new Sign[SIZE][SIZE];

    public int getMoveCounter() {
        return ++moveCounter;
    }
}
