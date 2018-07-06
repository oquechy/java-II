package ru.spbau.mit.oquechy.stress.utils;

import ru.spbau.mit.oquechy.stress.MessageProto;

import java.io.*;

public class Protobuf {
    public static MessageProto.Message parseDelimitedFrom(InputStream inputStream) throws IOException {
        int serializedSize = new DataInputStream(inputStream).readInt();
        byte[] bytes = new byte[serializedSize];
        for (int read = inputStream.read(bytes); read < bytes.length; ) {
            read += inputStream.read(bytes, read, bytes.length - read);
        }
        return MessageProto.Message.parseFrom(bytes);
    }

    public static void writeDelimitedTo(MessageProto.Message message, OutputStream outputStream) throws IOException {
        new DataOutputStream(outputStream).writeInt(message.getSerializedSize());
        message.writeTo(outputStream);
    }
}
