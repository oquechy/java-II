package ru.spbau.mit.oquechy.ttt.bot;

/**
 * Simulates opponent's moves in single player mode.
 */
public interface Bot {
    /**
     * Should return index of new move from [0..8].
     */
    int newMove();
}
