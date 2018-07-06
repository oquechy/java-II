package ru.spbau.mit.oquechy.ttt.logic;

import org.jetbrains.annotations.NotNull;

/**
 * Possible filling of cell of the game field.
 */
public enum Sign {
    N,
    X,
    O;

    /**
     * Returns the opponent's sign.
     */
    @NotNull
    public Sign flip() {
        return this == X ? O : this == O ? X : N;
    }
}
