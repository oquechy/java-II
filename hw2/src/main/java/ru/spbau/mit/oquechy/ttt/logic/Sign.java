package ru.spbau.mit.oquechy.ttt.logic;

public enum Sign {
    N,
    X,
    O;

    public Sign flip() {
        return this == X ? O : this == O ? X : N;
    }
}
