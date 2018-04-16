package ru.spbau.mit.oquechy.ftp.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server for simple FTP operations:
 *        - listing files from the server's directory
 *        - fetching a file from the server
 *
 * Implemented with blocking socket, so it can only interact with
 * the one client at a time.
 */
public class FTPServer {

    /**
     * Port which server listens to.
     */
    public static final int PORT = 3030;

    private static final int BUF_SIZE = 4096;
    private static byte[] buffer = new byte[BUF_SIZE];

    /**
     * Reads from clients in the loop until interrupted.
     * If the client ends his session, server starts to wait for a new client.
     * If the client makes incorrect query, connection is closed and server
     * also starts to wait for a new client.
     *
     * Arguments are ignored.
     *
     * @throws IOException when I/O fails
     */
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(PORT);
        System.out.println("Server is active");
        while (!Thread.interrupted()) {
            Socket socket = server.accept();
            System.out.println("Connected to " + socket.getInetAddress() + ":" + socket.getPort());
            try (DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                 DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())) {
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
                socket.close();
                System.out.println("Connection closed");
            }
        }

        System.out.println("Server was stopped");
    }

    private static void putFile(String path, DataOutputStream outputStream) throws IOException {
        File file = new File(path);

        outputStream.writeInt(!file.isFile() ? 0 : (int) file.length());

        if (!file.exists() || file.isDirectory()) {
            return;
        }

        try (FileInputStream inputStream = new FileInputStream(path)) {
            for (int r = inputStream.read(buffer); r > 0; r = inputStream.read(buffer)) {
                outputStream.write(buffer, 0, r);
            }
        }
    }

    private static void putList(String path, DataOutputStream outputStream) throws IOException {
        File[] files = new File(path).listFiles();
        outputStream.writeInt(files == null ? 0 : files.length);

        if (files == null) {
            return;
        }

        for (File file : files) {
            outputStream.writeUTF(file.getName());
            outputStream.writeBoolean(file.isDirectory());
        }
    }

    /**
     * Starts separate process with server.
     * @return {@link Process} instance
     * @throws IOException when I/O fails
     */
    public static Process start() throws IOException {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = FTPServer.class.getCanonicalName();

        ProcessBuilder builder = new ProcessBuilder(javaBin, "-cp", classpath, className);

        return builder.start();
    }
}
