package ru.spbau.mit.oquechy.threadpool;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.LinkedList;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

public class ConcurrentQueueTest {

    @NotNull
    private final static Object[] OBJECTS = {"Lisa", 0b101010, new Object(), '!',
            false, new IllegalArgumentException(), -1e18, 0x1};

    @Test
    public void add() throws InterruptedException {
        @NotNull ConcurrentQueue<Object> queue = new ConcurrentQueue<>();

        @NotNull Thread[] threads = new Thread[OBJECTS.length];
        for (int i = 0; i < threads.length; i++) {
            int j = i;
            threads[i] = new Thread(() -> queue.add(OBJECTS[j]));
        }

        startAndJoin(threads);
    }

    @Test
    public void take() throws InterruptedException {
        @NotNull ConcurrentQueue<Object> queue = new ConcurrentQueue<>();
        @NotNull Object sync = new Object();

        for (Object object : OBJECTS) {
            queue.add(object);
        }

        @NotNull Thread[] threads = new Thread[OBJECTS.length];
        @NotNull LinkedList<Object> taken = new LinkedList<>();
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                try {
                    Object object = queue.take();
                    synchronized (sync) {
                        taken.add(object);
                    }
                } catch (InterruptedException ignored) { }
            });
        }

        startAndJoin(threads);

        assertThat(taken, hasSize(OBJECTS.length));
        assertThat(taken, containsInAnyOrder(OBJECTS));
    }

    private void startAndJoin(@NotNull Thread[] threads) throws InterruptedException {
        for (@NotNull Thread thread : threads) {
            thread.start();
        }

        for (@NotNull Thread thread : threads) {
            thread.join();
        }
    }
}