package ru.spbau.mit.oquechy.ttt.bot;

import ru.spbau.mit.oquechy.ttt.logic.Model;

import java.util.Random;

public class RandomBot implements Bot {
    private final static int SIZE = Model.SIZE * Model.SIZE;

    private Model model;

    public RandomBot(Model model) {
        this.model = model;
    }

    @Override
    public int newMove() {
        int move;
        Random random = new Random();

        do {
            move = random.nextInt(SIZE);
            if (model == null) System.err.println("model is null");
        } while (model.isBusy(move));

        return move;
    }
}
