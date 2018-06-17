package ru.spbau.mit.oquechy.stress.server;

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

public class ServerNonBlocking extends Server {

    private final Selector channelsToRead = Selector.open();
    private final Selector channelsToWrite = Selector.open();

    public ServerNonBlocking() throws IOException { }

    public static void main(String[] args) throws IOException, InterruptedException {
        new ServerNonBlocking().run(1, 1);
    }

    @Override
    public Statistic run(int clientsNumber, int queriesNumber) throws IOException, InterruptedException {
        ServerSocketChannel server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress(PORT));

        Thread readingThread = new Thread(() -> readingCycle(clientsNumber));
        readingThread.setName("Reader");
        readingThread.start();
        Thread writingThread = new Thread(() -> writingCycle(clientsNumber));
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

        readingThread.join();
        writingThread.join();
        return null;
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
                    synchronized (trinket) {
                        ByteBuffer buffer = trinket.getMessageBuffer();
                        if (buffer == null) {
                            continue;
                        }

                        ByteBuffer sizeBuffer = trinket.getSizeBuffer();
                        clientChannel.write(new ByteBuffer[]{sizeBuffer, buffer});
                        clientChannel.socket().getOutputStream().flush();

                        if (buffer.position() == buffer.limit()) {
                            trinket.setMessageBuffer(null);
                            trinket.queryDone();
//                            if (trinket.isFinished()) {
//                                key.cancel();
//                                clientsNumber--;
//                            }
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

                    clientChannel.read(buffer == null ? sizeBuffer : buffer);

                    if (sizeBuffer.position() == sizeBuffer.capacity()) {
                        sizeBuffer.flip();
                        int received = getInt(sizeBuffer);
                        System.out.println("integer received = " + received);
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
                        System.out.println("arrayFromClient = " + message.getNumberList());
                        trinket.setMessageBuffer(null);
                        trinket.queryDone();

                        message = getResponse(message);
                        trinket.setWritingTrinket(message.getSerializedSize(), ByteBuffer.wrap(message.toByteArray()));
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