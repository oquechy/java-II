package ru.spbau.mit.oquechy.threadpool;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Multi thread blocking queue. Makes thread waiting for
 * element to appear in queue.
 * @param <E> type of elements
 */
public class ConcurrentQueue<E> {

    @NotNull
    private final LinkedList<E> queue = new LinkedList<>();

    /**
     * Adds new element.
     * @param e element
     */
    public void add(E e) {
        synchronized (queue) {
            queue.add(e);
            if (queue.size() == 1) {
                queue.notifyAll();
            }
        }
    }

    /**
     * Returns new element. If queue is empty, thread will wait
     * for element to be added in queue.
     * @throws InterruptedException if waiting for new element fails
     */
    public E take() throws InterruptedException {
        while (!Thread.interrupted()) {
            synchronized (queue) {
                if (queue.isEmpty()) {
                    queue.wait();
                } else {
                    return queue.remove();
                }
            }
        }

        throw new InterruptedException();
    }
}
