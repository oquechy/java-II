package ru.spbau.mit.oquechy.stress.server;

import com.google.common.base.Stopwatch;
import ru.spbau.mit.oquechy.stress.MessageProto.Message;
import ru.spbau.mit.oquechy.stress.types.Trinket;
import ru.spbau.mit.oquechy.stress.utils.Statistic;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ServerNonBlocking extends Server {

    private final Selector channelsToRead = Selector.open();
    private final Selector channelsToWrite = Selector.open();
    private ExecutorService pool = Executors.newFixedThreadPool(3);

    public ServerNonBlocking() throws IOException { }

    @Override
    public Statistic run(int clientsNumber, int queriesNumber) throws IOException, InterruptedException {
        sortingTime = new AtomicInteger(0);
        transmittingTime = new AtomicInteger(0);
        servingTime = new AtomicInteger(0);
        Thread readingThread;
        Thread writingThread;
        try (ServerSocketChannel server = ServerSocketChannel.open()) {
            server.bind(new InetSocketAddress(PORT));

            readingThread = new Thread(() -> readingCycle(clientsNumber));
            readingThread.setName("Reader");
            readingThread.start();
            writingThread = new Thread(() -> writingCycle(clientsNumber));
            writingThread.setName("Writer");
            writingThread.start();

            for (int i = 0; i < clientsNumber; i++) {
                SocketChannel client = server.accept();
                client.configureBlocking(false);

                SelectionKey writeKey;
                Trinket trinket;
                synchronized (channelsToWrite) {
                    writeKey = client.register(channelsToWrite, SelectionKey.OP_WRITE);
                    trinket = new Trinket(queriesNumber, null);
                    writeKey.attach(trinket);
                }

                SelectionKey readKey;
                synchronized (channelsToRead) {
                    readKey = client.register(channelsToRead, SelectionKey.OP_READ);
                    readKey.attach(new Trinket(queriesNumber, trinket));
                }
            }
        }

        readingThread.join();
        writingThread.join();
        return new Statistic(sortingTime.get(), transmittingTime.get(), servingTime.get(),
                clientsNumber * queriesNumber);
    }

    private void writingCycle(int clientsNumber) {
        try {
            while (clientsNumber > 0) {
                synchronized (channelsToWrite) {
                    if (channelsToWrite.selectNow() == 0) {
                        continue;
                    }
                }

                Set<SelectionKey> selectedKeys;
                synchronized (channelsToWrite) {
                    selectedKeys = channelsToWrite.selectedKeys();
                }
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    SocketChannel clientChannel = (SocketChannel) key.channel();

                    Trinket trinket = (Trinket) key.attachment();
                    // I prefer trusting nio
                    //noinspection SynchronizationOnLocalVariableOrMethodParameter
                    synchronized (trinket) {
                        ByteBuffer buffer = trinket.getMessageBuffer();
                        if (buffer == null) {
                            continue;
                        }

                        ByteBuffer sizeBuffer = trinket.getSizeBuffer();
                        clientChannel.write(new ByteBuffer[]{sizeBuffer, buffer});
                        clientChannel.socket().getOutputStream().flush();

                        if (buffer.position() == buffer.limit()) {
                            Stopwatch stopwatch = trinket.getStopwatch();
                            transmittingTime.getAndAdd((int) stopwatch.elapsed(MILLISECONDS));
                            stopwatch.reset();

                            trinket.getSizeBuffer().clear();
                            trinket.setMessageBuffer(null);
                            trinket.queryDone();
                            if (trinket.isFinished()) {
                                key.cancel();
                                clientsNumber--;
                            }
                        }
                    }
                }
            }
            System.out.println("Finish writing");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readingCycle(int clientsNumber) {
        try {
            while (clientsNumber > 0) {
                synchronized (channelsToRead) {
                    if (channelsToRead.selectNow() == 0) {
                        continue;
                    }
                }

                Set<SelectionKey> selectedKeys;
                synchronized (channelsToRead) {
                    selectedKeys = channelsToRead.selectedKeys();
                }
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    SocketChannel clientChannel = (SocketChannel) key.channel();

                    Trinket trinket = (Trinket) key.attachment();
                    ByteBuffer sizeBuffer = trinket.getSizeBuffer();
                    ByteBuffer buffer = trinket.getMessageBuffer();
                    Stopwatch stopwatch = trinket.getStopwatch();

                    if (!stopwatch.isRunning()) {
                        stopwatch.start();
                    }

                    clientChannel.read(buffer == null ? sizeBuffer : buffer);

                    if (sizeBuffer.position() == sizeBuffer.capacity()) {
                        sizeBuffer.flip();
                        int received = getInt(sizeBuffer);
                        sizeBuffer.clear();

                        if (trinket.isFinished()) {
                            servingTime.getAndAdd(received);
                            clientsNumber--;
                            key.cancel();
                        } else {
                            buffer = ByteBuffer.allocate(received);
                            clientChannel.read(buffer);
                            trinket.setMessageBuffer(buffer);
                        }
                    }

                    if (buffer != null && buffer.position() == buffer.capacity()) {
                        buffer.flip();
                        byte[] serialized = new byte[buffer.capacity()];
                        buffer.get(serialized);
                        Message message = Message.parseFrom(serialized);
                        trinket.setMessageBuffer(null);
                        trinket.queryDone();

                        pool.execute(() -> {
                            Stopwatch sorting = Stopwatch.createStarted();
                            Message response = getResponse(message);
                            sortingTime.getAndAdd((int) sorting.elapsed(MILLISECONDS));
                            trinket.setWritingTrinket(response.getSerializedSize(),
                                    ByteBuffer.wrap(message.toByteArray()));
                        });
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getInt(ByteBuffer sizeBuffer) throws IOException {
        return new DataInputStream(new ByteArrayInputStream(sizeBuffer.array())).readInt();
    }
}