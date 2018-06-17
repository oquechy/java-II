package ru.spbau.mit.oquechy.stress.types;

public enum Architecture {
    SEPARATE_THREAD{
        @Override
        public String toString() {
            return "Separate threads";
        }
    },
    THREAD_POOL{
        @Override
        public String toString() {
            return "Thread pool and separate threads";
        }
    },
    NON_BLOCKING{
        @Override
        public String toString() {
            return "Non-blocking and thread pool";
        }
    }
}
