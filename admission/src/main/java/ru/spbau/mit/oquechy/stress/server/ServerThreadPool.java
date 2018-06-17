package ru.spbau.mit.oquechy.stress.server;

import com.google.common.base.Stopwatch;
import ru.spbau.mit.oquechy.stress.MessageProto.Message;
import ru.spbau.mit.oquechy.stress.utils.Statistic;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ServerThreadPool extends Server {

    private ExecutorService pool = newFixedThreadPool(4);
    private ExecutorService[] writers;

    @Override
    public Statistic run(int clientsNumber, int queriesNumber) throws IOException, InterruptedException {
        sortingTime = new AtomicLong(0);
        transmittingTime = new AtomicLong(0);
        servingTime = new AtomicLong(0);

        Thread[] threads = new Thread[clientsNumber];
        writers = new ExecutorService[clientsNumber];
        for (int i = 0; i < writers.length; i++) {
            writers[i] = newSingleThreadExecutor();
        }

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            for (int i = 0; i < clientsNumber; i++) {
                Socket socket = serverSocket.accept();
                int id = i;
                threads[i] = new Thread(() -> processClient(socket, queriesNumber, id));
                threads[i].start();
            }
        }

        for (Thread thread : threads) {
            thread.join();
        }

        return new Statistic(sortingTime.get(), transmittingTime.get(), servingTime.get(),
                clientsNumber * queriesNumber);
    }

    private void processClient(Socket socket, int expectedQueries, int id) {
        try {
            for (int i = 0; i < expectedQueries; i++) {
                processQuery(socket, id);
            }
            long total = receiveStatistic(socket);
            servingTime.getAndAdd(total);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processQuery(Socket socket, int id) throws IOException {
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();

        Stopwatch transmission = Stopwatch.createStarted();
        Message message = Message.parseDelimitedFrom(inputStream);

        pool.execute(() -> {
            Stopwatch sorting = Stopwatch.createStarted();
            Message response = getResponse(message);
            sortingTime.getAndAdd(sorting.elapsed(MILLISECONDS));
            writers[id].execute(() -> {
                try {
                    response.writeDelimitedTo(outputStream);
                    transmittingTime.getAndAdd(transmission.elapsed(MILLISECONDS));
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
    }
}

