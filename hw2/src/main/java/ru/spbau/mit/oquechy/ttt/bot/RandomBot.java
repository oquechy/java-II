package ru.spbau.mit.oquechy.ttt.bot;

import org.jetbrains.annotations.NotNull;
import ru.spbau.mit.oquechy.ttt.logic.Model;

import java.util.Random;

public class RandomBot implements Bot {
    private final static int SIZE = Model.SIZE * Model.SIZE;

    private Model model;

    /**
     * Takes a model to ask it about current field state.
     */
    public RandomBot(Model model) {
        this.model = model;
    }

    /**
     * Returns the first possible move of random generated.
     */
    @Override
    public int newMove() {
        int move;
        @NotNull Random random = new Random();

        do {
            move = random.nextInt(SIZE);
        } while (model.isBusy(move));

        return move;
    }
}
