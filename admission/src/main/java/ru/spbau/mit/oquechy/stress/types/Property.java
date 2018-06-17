package ru.spbau.mit.oquechy.stress.types;

public enum Property {
    N,
    M,
    D{
        @Override
        public String toString() {
            return "∆";
        }
    }
}
