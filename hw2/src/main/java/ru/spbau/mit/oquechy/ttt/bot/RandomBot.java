package ru.spbau.mit.oquechy.ttt.bot;

import org.jetbrains.annotations.NotNull;
import ru.spbau.mit.oquechy.ttt.logic.Model;
import ru.spbau.mit.oquechy.ttt.logic.Position;

import java.util.Random;

/**
 * Implementation of {@link Bot} interface, which
 * generates random moves until suitable one is found.
 */
public class RandomBot implements Bot {
    private final static int SIZE = Model.ROW * Model.ROW;

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
    public Position newMove() {
        Position move;
        @NotNull Random random = new Random();

        do {
            move = new Position(random.nextInt(Model.ROW), random.nextInt(Model.ROW));
        } while (model.isBusy(move));

        return move;
    }
}
