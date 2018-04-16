package ru.spbau.mit.oquechy.ftp.client;

import ru.spbau.mit.oquechy.ftp.server.FTPServer;
import ru.spbau.mit.oquechy.ftp.types.FileEntry;
import ru.spbau.mit.oquechy.ftp.types.FileInfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class FTPClient implements AutoCloseable {

    private final Socket socket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;

    static public FTPClient start(String host) throws IOException {
        return new FTPClient(host, FTPServer.PORT);
    }

    private FTPClient(String host, int port) throws IOException {
        socket = new Socket(host, port);
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
    }

    public List<FileInfo> list(String path) throws IOException {
        outputStream.writeInt(1);
        outputStream.writeUTF(path);

        ArrayList<FileInfo> files = new ArrayList<>();
        int n = inputStream.readInt();
        for (int i = 0; i < n; i++) {
            files.add(new FileInfo(inputStream.readUTF(), inputStream.readBoolean()));
        }
        return files;
    }

    public FileEntry get(String path) throws IOException {
        outputStream.writeInt(2);
        outputStream.writeUTF(path);

        FileEntry fileEntry = new FileEntry(inputStream.readInt());
        for (int cur = inputStream.read(fileEntry.file), r; cur < fileEntry.file.length; cur += r) {
            if ((r = inputStream.read(fileEntry.file, cur, fileEntry.size - cur)) < 0) {
                throw new RuntimeException("Corrupted file");
            }
        }
        return fileEntry;
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
