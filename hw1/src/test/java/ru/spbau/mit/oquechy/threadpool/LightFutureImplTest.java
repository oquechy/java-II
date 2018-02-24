package ru.spbau.mit.oquechy.threadpool;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

public class LightFutureImplTest {

    private int cnt;
    @NotNull
    private final Supplier<Integer> COUNTER = () -> {
        synchronized (LightFutureImplTest.this) {
            return cnt++;
        }
    };

    @NotNull
    private final Supplier<?> THROWER = () -> {throw new IllegalArgumentException();};

    private ThreadPoolImpl threadPool;
    @NotNull
    private Supplier<String> HEAVY = () -> {
        String s = "s";
        for (int i = 0; i < 20; i++) {
            s += s;
        }
        return s;
    };

    @Before
    public void SetUp() {
        threadPool = new ThreadPoolImpl(10);
        cnt = 0;
    }
    
     @Test
    public void getConstant() throws LightExecutionException, InterruptedException {
        @NotNull LinkedList<LightFuture<Integer>> tasks = new LinkedList<>();

        for (int i = 0; i < 40; i++) {
            int j = i;
            tasks.add(threadPool.assignTask(() -> (j)));
        }

        for (int i = 0; i < tasks.size(); i++) {
            LightFuture<Integer> task = tasks.get(i);
            assertThat(task.get(), is(i));
        }
    }

    @Test
    public void getWithComputation() throws LightExecutionException, InterruptedException {

        @NotNull LinkedList<LightFuture<Integer>> tasks = new LinkedList<>();

        for (int i = 0; i < 100; i++) {
            tasks.add(threadPool.assignTask(COUNTER));
        }

        @NotNull boolean[] met = new boolean[tasks.size()];

        for (@NotNull LightFuture<Integer> task : tasks) {
            met[task.get()] = true;
        }

        for (int i = 0; i < met.length; i++) {
            boolean b = met[i];
            if (!b)
                System.err.println(i);
            assertThat(b, is(true));
        }
    }

    @Test
    public void isReady() throws LightExecutionException, InterruptedException {
        @NotNull LinkedList<LightFuture<?>> tasks = new LinkedList<>();
        @NotNull Object[] sync = new Object[40];
        for (int i = 0; i < sync.length; i++) {
            sync[i] = new Object();
        }

        for (int i = 0; i < 40; i++) {
            int j = i;
            @NotNull LightFuture<?> task = threadPool.assignTask(() -> {
                while (tasks.size() <= j) {
                    synchronized (sync[j]) {
                       if (tasks.size() <= j) {
                           try {
                               sync[j].wait();
                           } catch (InterruptedException ignored) { }
                       }
                    }
                }

                assertThat(tasks.get(j).isReady(), is(false));
                return true;
            });
            tasks.add(task);
            synchronized (sync[j]) {
                sync[j].notify();
            }
        }

        for (@NotNull LightFuture<?> task : tasks) {
            task.get();
            assertThat(task.isReady(), is(true));
        }
    }
    
    @Test
    public void getExceptionWhileRunningSupplier() throws InterruptedException, LightExecutionException {
        @NotNull LightFuture<?> task = threadPool.assignTask(THROWER);
        try {
            task.get();
            assertTrue("exception should be thrown", false);
        } catch (LightExecutionException e) {
            assertThat(e.getCause(), instanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void thenApplyOnce() throws LightExecutionException, InterruptedException {
        @NotNull LinkedList<LightFuture<?>> afterTasks = new LinkedList<>();

        for (int i = 0; i < 40; i++) {
            int j = i;
            @NotNull LightFuture<Integer> task = threadPool.assignTask(() -> j);
            afterTasks.add(task.thenApply((t) -> -t));
        }

        for (int i = 0; i < 40; i++) {
            LightFuture<?> afterTask = afterTasks.get(i);
            assertThat(afterTask.get(), is(-i));
        }
    }

    @Test
    public void thenApplyManyTimes() throws LightExecutionException, InterruptedException {
        @NotNull LinkedList<LightFuture<?>> afterTasks = new LinkedList<>();
        @NotNull LightFuture<Integer> task = threadPool.assignTask(() -> 10);

        for (int i = 0; i < 40; i++) {
            int j = i;
            afterTasks.add(task.thenApply((t) -> t * j));
        }

        for (int i = 0; i < 40; i++) {
            LightFuture<?> afterTask = afterTasks.get(i);
            assertThat(afterTask.get(), is(i * 10));
        }
    }

    @Test
    public void thenApplyAfterPrevious() throws LightExecutionException, InterruptedException {
        @NotNull LinkedList<LightFuture<?>> afterTasks = new LinkedList<>();

        for (int i = 0; i < 40; i++) {
            @NotNull LightFuture<String> task = threadPool.assignTask(HEAVY);
            afterTasks.add(task.thenApply((t) -> task.isReady()));
        }

        for (int i = 0; i < 40; i++) {
            assertThat(afterTasks.get(i).get(), is(true));
        }
    }

    @Test
    public void getMultiThread() throws InterruptedException {
        @NotNull LightFuture<Integer> task = threadPool.assignTask(COUNTER);
        @NotNull Thread[] threads = new Thread[4000];

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                try {
                    assertThat(task.get(), is(0));
                } catch (@NotNull InterruptedException | LightExecutionException e) {
                    assertTrue("exception shouldn't be thrown", false);
                }
            });
        }

        startAndJoin(threads);
    }

    @Test
    public void thenApplyMultiThread() throws InterruptedException {
        @NotNull LightFuture<Integer> task = threadPool.assignTask(COUNTER);
        @NotNull Thread[] threads = new Thread[4000];

        for (int i = 0; i < threads.length; i++) {
            int j = i;
            threads[i] = new Thread(() -> {
                try {
                    assertThat(task.thenApply((t) -> (t - j)).get(), is (-j));
                } catch (@NotNull InterruptedException | LightExecutionException e) {
                    assertTrue("exception shouldn't be thrown", false);
                }
            });
        }

        startAndJoin(threads);
    }

    @Test
    public void isReadyMultiThread() throws InterruptedException {
        @NotNull LightFuture<String> task = threadPool.assignTask(HEAVY);
        @NotNull Thread[] threads = new Thread[4000];

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(task::isReady);
        }

        startAndJoin(threads);
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