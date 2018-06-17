package ru.spbau.mit.oquechy.stress.server;

import com.google.common.base.Stopwatch;
import ru.spbau.mit.oquechy.stress.MessageProto;
import ru.spbau.mit.oquechy.stress.utils.Protobuf;
import ru.spbau.mit.oquechy.stress.utils.Statistic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ServerSeparateThreads extends Server {

    @Override
    public Statistic run(int clientsNumber, int queriesNumber) throws IOException, InterruptedException {
        sortingTime = new AtomicInteger(0);
        transmittingTime = new AtomicInteger(0);
        servingTime = new AtomicInteger(0);
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
            servingTime.getAndAdd(receiveStatistic(socket));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processQuery(Socket socket) throws IOException {
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();

        MessageProto.Message message = Protobuf.parseDelimitedFrom(inputStream);
        Stopwatch transmission = Stopwatch.createStarted();

        Stopwatch sorting = Stopwatch.createStarted();
        MessageProto.Message response = getResponse(message);
        sortingTime.getAndAdd((int) sorting.elapsed(MILLISECONDS));

        Protobuf.writeDelimitedTo(response, outputStream);
        transmittingTime.getAndAdd((int) transmission.elapsed(MILLISECONDS));
        outputStream.flush();
    }
}
