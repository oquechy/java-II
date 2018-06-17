package ru.spbau.mit.oquechy.stress.server;

import ru.spbau.mit.oquechy.stress.MessageProto;
import ru.spbau.mit.oquechy.stress.utils.Statistic;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static ru.spbau.mit.oquechy.stress.utils.Sort.bubbleSort;

public abstract class Server {
    int PORT = 3030;

    public abstract Statistic run(int expectedClients, int expectedQueries) throws IOException, InterruptedException;

    protected AtomicInteger sortingTime;
    protected AtomicInteger transmittingTime;
    protected AtomicInteger servingTime;

    protected MessageProto.Message getResponse(MessageProto.Message message) {
        int[] array = message.getNumberList().stream().mapToInt(x -> x).toArray();
        List<Integer> list = Arrays.stream(bubbleSort(array)).boxed().collect(Collectors.toList());
        return MessageProto.Message.newBuilder().addAllNumber(list).build();
    }

    protected int receiveStatistic(Socket socket) throws IOException {
        return new DataInputStream(socket.getInputStream()).readInt();
    }
}
