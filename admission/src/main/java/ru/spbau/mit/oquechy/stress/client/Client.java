package ru.spbau.mit.oquechy.stress.client;


import com.google.common.base.Stopwatch;
import ru.spbau.mit.oquechy.stress.MessageProto.Message;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;

import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Client {
    public static void main(String[] args) throws IOException, InterruptedException {
        new Client("localhost", 3030, 11, 1, 1);
    }

    public Client(String hostname, int port, int length, int queries, int delay)
            throws IOException, InterruptedException {
        Random random = new Random();
        try (Socket socket = new Socket(hostname, port)) {
            Stopwatch serving = Stopwatch.createStarted();
            for (int i = 0; i < queries; i++) {
                Message.Builder builder = Message.newBuilder();
                random.ints(length).forEach(builder::addNumber);
                builder.build().writeDelimitedTo(socket.getOutputStream());
                Message.parseDelimitedFrom(socket.getInputStream());
                sleep(delay);
            }
            new DataOutputStream(socket.getOutputStream()).writeLong(serving.elapsed(MILLISECONDS));
        }

    }
}
