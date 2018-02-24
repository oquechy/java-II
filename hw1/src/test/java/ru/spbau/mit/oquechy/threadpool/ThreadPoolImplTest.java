package ru.spbau.mit.oquechy.threadpool;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.function.Supplier;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.junit.Assert.*;

public class ThreadPoolImplTest {
    @NotNull
    private static final Supplier<Integer> RANDOM_SUPPLIER = () -> (new Random().nextInt());

    private static final int CONSTANT = 0b101010;
    @NotNull
    private static final Supplier<Integer> CONSTANT_SUPPLIER = () -> CONSTANT;

    @Test
    public void shutdownWithoutTasks() throws InterruptedException {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        int threadsOnStart = threadGroup.activeCount();
        @NotNull ThreadPoolImpl threadPool = new ThreadPoolImpl(1000);
        threadPool.shutdown();

        assertThat(threadGroup.activeCount(), is(threadsOnStart));
    }

    @Test
    public void shutdownWithTasks() throws InterruptedException {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        int threadsOnStart = threadGroup.activeCount();
        @NotNull ThreadPoolImpl threadPool = new ThreadPoolImpl(1000);

        for (int i = 0; i < 400000; i++) {
            threadPool.assignTask(RANDOM_SUPPLIER);
        }

        threadPool.shutdown();
        assertThat(threadGroup.activeCount(), is(threadsOnStart));
    }

    @Test
    public void assignTaskJustOne() throws LightExecutionException, InterruptedException {
        @NotNull ThreadPoolImpl threadPool = new ThreadPoolImpl(3);
        @NotNull LightFuture<Integer> constantTask = threadPool.assignTask(CONSTANT_SUPPLIER);
        assertThat(constantTask.get(), is(CONSTANT));
    }

    @Test
    public void assignTaskSeveralTasks() throws LightExecutionException, InterruptedException {
        @NotNull ThreadPoolImpl threadPool = new ThreadPoolImpl(3);
        @NotNull LinkedList<LightFuture<Integer>> lightFutures = new LinkedList<>();

        for (int i = 0; i < 100; i++) {
            int j = i;
            lightFutures.add(threadPool.assignTask(() -> j));
        }

        for (int i = 0; i < lightFutures.size(); i++) {
            LightFuture<Integer> lightFuture = lightFutures.get(i);
            assertThat(lightFuture.get(), is(i));
        }
    }

    @Test
    public void threadsCount() throws InterruptedException, LightExecutionException {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        int n = 100;
        int threadsOnStart = threadGroup.activeCount();
        @NotNull ThreadPoolImpl threadPool = new ThreadPoolImpl(n);
        assertThat(threadGroup.activeCount(), greaterThanOrEqualTo(threadsOnStart + n));

        @NotNull HashSet<Long> threadIDs = new HashSet<>();
        @NotNull Object[] syncStart = new Object[n - 1];
        for (int i = 0; i < syncStart.length; i++) {
            syncStart[i] = new Object();
        }

        @NotNull Object[] syncFinish = new Object[n - 1];
        for (int i = 0; i < syncFinish.length; i++) {
            syncFinish[i] = new Object();
        }

        @NotNull boolean[] isReady = new boolean[n - 1];

        @NotNull LinkedList<LightFuture<?>> tasks = new LinkedList<>();

        for (int i = 0; i < n - 1; i++) {
            int j = i;
            tasks.add(threadPool.assignTask(() -> {
                isReady[j] = true;
                try {
                    synchronized (syncStart[j]) {
                        syncStart[j].notify();
                    }
                    synchronized (syncFinish[0]) {
                        addId(threadIDs);
                    }
                    synchronized (syncFinish[j]) {
                        syncFinish[j].wait();
                    }
                } catch (InterruptedException ignored) {
                }
                return true;
            }));
        }

        tasks.add(threadPool.assignTask(() -> {
            synchronized (syncFinish[0]) {
                addId(threadIDs);
            }
            // shouldn't be replaced with foreach in order to make synchronization not on local variable
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < syncFinish.length; i++) {
                while (!isReady[i]) {
                    synchronized (syncStart[i]) {
                        if (!isReady[i]) {
                            try {
                                syncStart[i].wait();
                            } catch (InterruptedException ignored) {
                            }
                        }
                    }
                }

                synchronized (syncFinish[i]) {
                    syncFinish[i].notify();
                }
            }
            return true;
        }));

        for (@NotNull LightFuture<?> task : tasks) {
            task.get();
        }

        assertThat(threadIDs, hasSize(greaterThanOrEqualTo(n)));
    }

    private void addId(@NotNull HashSet<Long> threadIDs) {
        threadIDs.add(Thread.currentThread().getId());
    }
}