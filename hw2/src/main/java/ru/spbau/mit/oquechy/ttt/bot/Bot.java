package ru.spbau.mit.oquechy.ttt.bot;

import ru.spbau.mit.oquechy.ttt.logic.Position;

/**
 * Simulates opponent's moves in single player mode.
 */
public interface Bot {
    /**
     * Should return index of new move from [0..8].
     */
    Position newMove();
}
