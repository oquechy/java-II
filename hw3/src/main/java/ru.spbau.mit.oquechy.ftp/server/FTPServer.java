package ru.spbau.mit.oquechy.ftp.server;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server for simple FTP operations:
 * - listing files from the server's directory
 * - fetching a file from the server
 * <p>
 * Implemented with blocking socket. Can interact with
 * several clients at a time.
 */
public class FTPServer {

    /**
     * Port which server listens to.
     */
    public static final int PORT = 3030;

    private static final int BUF_SIZE = 4096;
    @NotNull
    private static byte[] buffer = new byte[BUF_SIZE];

    /**
     * Runs the server as a main program.
     * Arguments are ignored.
     */
    public static void main(String[] args) {
        run();
    }

    /**
     * Reads from clients in the loop until interrupted.
     * Starts a new {@link Thread} for each client.
     * If the client makes an incorrect query, connection is closed and server
     * starts to wait for a new client.
     */
    public static void run() {
        @NotNull ServerSocket server;
        try {
            server = new ServerSocket(PORT);
        } catch (IOException e) {
            System.out.println("Failed to open the socket. Exiting...");
            return;
        }

        System.out.println("Server is active");
        while (!Thread.interrupted()) {
            Socket socket;
            try {
                socket = server.accept();
            } catch (IOException e) {
                System.out.println("Failed to accept the connection. Exiting...");
                return;
            }
            System.out.println("Connected to " + socket.getInetAddress() + ":" + socket.getPort());
            new Thread(() -> {
                try (@NotNull DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                     @NotNull DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())) {
                    int query;
                    do {
                        query = inputStream.readInt();
                        String path;
                        switch (query) {
                            case 1:
                                System.out.print("Listing");
                                path = inputStream.readUTF();
                                System.out.println(" " + path);
                                putList(path, outputStream);
                                break;
                            case 2:
                                System.out.print("Sending");
                                path = inputStream.readUTF();
                                System.out.println(" " + path);
                                putFile(path, outputStream);
                                break;
                            default:
                                socket.close();
                                System.out.println("Incorrect query: " + query + ". Connection closed");
                        }
                    } while (query == 1 || query == 2);
                } catch (IOException e) {
                    try {
                        socket.close();
                    } catch (IOException ignored) {
                    }
                    System.out.println("Connection closed");
                }
            }).start();
        }
        System.out.println("Server was stopped");
    }

    private static void putFile(@NotNull String path, DataOutputStream outputStream) throws IOException {
        @NotNull File file = new File(path);

        outputStream.writeInt(!file.isFile() ? 0 : (int) file.length());

        if (!file.exists() || file.isDirectory()) {
            return;
        }

        try (@NotNull FileInputStream inputStream = new FileInputStream(path)) {
            for (int r = inputStream.read(buffer); r > 0; r = inputStream.read(buffer)) {
                outputStream.write(buffer, 0, r);
            }
        }
    }

    private static void putList(@NotNull String path, DataOutputStream outputStream) throws IOException {
        @Nullable File[] files = new File(path).listFiles();
        outputStream.writeInt(files == null ? 0 : files.length);

        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file != null) {
                outputStream.writeUTF(file.getName());
                outputStream.writeBoolean(file.isDirectory());
            }
        }
    }

    /**
     * Starts separate process with server.
     *
     * @return {@link Process} instance
     * @throws IOException when I/O fails
     */
    public static Process start() throws IOException {
        String javaHome = System.getProperty("java.home");
        @NotNull String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = FTPServer.class.getCanonicalName();

        @NotNull ProcessBuilder builder = new ProcessBuilder(javaBin, "-cp", classpath, className);

        return builder.start();
    }
}
