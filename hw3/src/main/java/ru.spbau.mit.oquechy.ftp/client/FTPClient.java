package ru.spbau.mit.oquechy.ftp.client;

import com.google.common.io.ByteStreams;
import org.jetbrains.annotations.NotNull;
import ru.spbau.mit.oquechy.ftp.server.FTPServer;
import ru.spbau.mit.oquechy.ftp.types.FileInfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Client that can pass two types of queries to {@link FTPServer}:
 *      - list files in the server's directory
 *      - fetch a file from the server
 *
 * TCP blocking socket is used for connection.
 */
public class FTPClient implements AutoCloseable {

    @NotNull
    private final Socket socket;

    @NotNull
    private final DataInputStream inputStream;

    @NotNull
    private final DataOutputStream outputStream;

    private FTPClient(String host, int port) throws IOException {
        socket = new Socket(host, port);
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
    }

    /**
     * Makes an attempt to connect to the server
     *
     * @param host server's address
     * @return {@link FTPClient} instance
     * @throws IOException when fails to create a socket
     */
    static public FTPClient start(String host) throws IOException {
        return new FTPClient(host, FTPServer.PORT);
    }

    /**
     * Asks the server to send a list of files from specified directory
     *
     * @param path path to directory on the server
     * @return list of files in the directory of empty list
     * if the directory doesn't exist
     * @throws IOException when I/O fails
     */
    @NotNull
    public List<FileInfo> list(@NotNull String path) throws IOException {
        outputStream.writeInt(1);
        outputStream.writeUTF(path);

        @NotNull ArrayList<FileInfo> files = new ArrayList<>();
        int n = inputStream.readInt();
        for (int i = 0; i < n; i++) {
            files.add(new FileInfo(inputStream.readUTF(), inputStream.readBoolean()));
        }
        return files;
    }

    /**
     * Asks the server to send a file at the specified path.
     *
     * @param src path to source file on server
     * @param dst path to local destination file
     * @return true if non-empty file was successfully fetched and
     * false if file was empty or any error occurred
     * @throws IOException                   when I/O fails
     * @throws UnsupportedOperationException when promised size of file doesn't suit the real data
     */
    public boolean get(@NotNull String src, @NotNull String dst) throws IOException {
        outputStream.writeInt(2);
        outputStream.writeUTF(src);

        int size = inputStream.readInt();
        if (size == 0) {
            return false;
        }

        @NotNull FileOutputStream fileOutputStream = new FileOutputStream(dst);
        long transferred = ByteStreams.limit(inputStream, size).transferTo(fileOutputStream);

        if (transferred < size) {
            throw new UnsupportedOperationException("Corrupted file");
        }

        return true;
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
