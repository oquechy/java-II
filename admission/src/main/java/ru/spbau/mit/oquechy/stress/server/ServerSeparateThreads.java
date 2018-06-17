package ru.spbau.mit.oquechy.stress.server;

import com.google.common.base.Stopwatch;
import ru.spbau.mit.oquechy.stress.MessageProto;
import ru.spbau.mit.oquechy.stress.utils.Statistic;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ServerSeparateThreads extends Server {

    @Override
    public Statistic run(int clientsNumber, int queriesNumber) throws IOException, InterruptedException {
        sortingTime = new AtomicLong(0);
        transmittingTime = new AtomicLong(0);
        servingTime = new AtomicLong(0);
        Thread[] threads = new Thread[clientsNumber];
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started");
            for (int i = 0; i < clientsNumber; i++) {
                Socket socket = serverSocket.accept();
                threads[i] = new Thread(() -> processClient(socket, queriesNumber));
                threads[i].start();
            }
        }
        for (Thread thread : threads) {
            thread.join();
        }
        return new Statistic(sortingTime.get(), transmittingTime.get(), servingTime.get(),
                clientsNumber * queriesNumber);
    }

    private void processClient(Socket socket, int expectedQueries) {
        try {
            for (int i = 0; i < expectedQueries; i++) {
                processQuery(socket);
            }
            long total = receiveStatistic(socket);
            servingTime.getAndAdd(total);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processQuery(Socket socket) throws IOException {
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();

        Stopwatch transmission = Stopwatch.createStarted();
        MessageProto.Message message = MessageProto.Message.parseDelimitedFrom(inputStream);

        Stopwatch sorting = Stopwatch.createStarted();
        MessageProto.Message response = getResponse(message);
        sortingTime.getAndAdd(sorting.elapsed(MILLISECONDS));

        response.writeDelimitedTo(outputStream);
        transmittingTime.getAndAdd(transmission.elapsed(MILLISECONDS));
        outputStream.flush();
    }
}
