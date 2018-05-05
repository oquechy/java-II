package ru.spbau.mit.oquechy.ttt.logic;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Data class for storing cell coordinates.
 */
@Data
public class Position {
    final private int x;
    final private int y;

    /**
     * Returns true if stored position is located on the filed.
     */
    public boolean onField() {
        return 0 <= x && x < Model.ROW && 0 <= y && y < Model.ROW;
    }
}
