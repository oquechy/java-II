package ru.spbau.mit.oquechy.stress.client;


import com.google.common.base.Stopwatch;
import ru.spbau.mit.oquechy.stress.MessageProto.Message;
import ru.spbau.mit.oquechy.stress.utils.Protobuf;

import java.io.*;
import java.net.Socket;
import java.util.Random;

import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Client {
    private Random random = new Random();

    private Message generateMessage(int length) {
        Message.Builder builder = Message.newBuilder();
        random.ints(length, 0, 10).forEach(builder::addNumber);
        return builder.build();
    }
    
    public Client(String hostname, int port, int length, int queries, int delay)
            throws IOException, InterruptedException {
        try (Socket socket = new Socket(hostname, port)) {
            Stopwatch serving = Stopwatch.createStarted();
            OutputStream outputStream = socket.getOutputStream();
            for (int i = 0; i < queries; i++) {
                System.out.println("i = " + i);
                Message message = generateMessage(length);
                Protobuf.writeDelimitedTo(message, outputStream);
                outputStream.flush();
                System.out.println("client generated message = " + message.getNumberList());
                Protobuf.parseDelimitedFrom(socket.getInputStream());
                sleep(delay);
            }
            new DataOutputStream(outputStream).writeInt((int) serving.elapsed(MILLISECONDS));
            outputStream.flush();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        new Client("localhost", 3030, 6, 1, 0);
    }

}
