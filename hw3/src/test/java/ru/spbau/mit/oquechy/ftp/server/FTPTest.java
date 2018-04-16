package ru.spbau.mit.oquechy.ftp.server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.spbau.mit.oquechy.ftp.client.FTPClient;
import ru.spbau.mit.oquechy.ftp.types.FileEntry;
import ru.spbau.mit.oquechy.ftp.types.FileInfo;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FTPTest {
    private Process server;
    private Socket socket;
    @BeforeEach
    public void setup() throws IOException, InterruptedException {
        server = FTPServer.start();
        Thread.sleep(2000);            // waiting for server to init
    }

    @Test
    public void list() throws IOException {
        for (int i = 0; i < 2; i++) {
            try (FTPClient client = FTPClient.start("localhost")) {
                String path = System.getProperty("user.dir") +
                        File.separator + "src" +
                        File.separator + "main" +
                        File.separator + "resources";
                List<FileInfo> list = client.list(path);

                //noinspection unchecked
                assertThat(list, containsInAnyOrder(
                        equalTo(new FileInfo("main", true)),
                        equalTo(new FileInfo("test", true)),
                        equalTo(new FileInfo("file", false))
                ));
                assertThat(server.isAlive(), is(true));
            }
        }
    }

    @Test
    public void invalidList() throws IOException {
        try (FTPClient client = FTPClient.start("localhost")) {
            for (int i = 0; i < 2; i++) {
                String path = System.getProperty("user.dir") +
                        File.separator + "src" +
                        File.separator + "main" +
                        File.separator + "resources" +
                        File.separator + "file";
                List<FileInfo> list = client.list(path);
                assertThat(list, is(empty()));
                assertThat(server.isAlive(), is(true));
            }
        }
    }

    @Test
    public void invalidGet() throws IOException {
        try (FTPClient client = FTPClient.start("localhost")) {
            for (int i = 0; i < 2; i++) {
                String path = System.getProperty("user.dir") +
                        File.separator + "src" +
                        File.separator + "main" +
                        File.separator + "sources";
                FileEntry file = client.get(path);
                assertThat(file.size, is(0));
                assertThat(server.isAlive(), is(true));
            }
        }
    }

    @Test
    public void getAndList() throws IOException {
        try (FTPClient client = FTPClient.start("localhost")) {
            for (int i = 0; i < 2; i++) {
                String path = System.getProperty("user.dir") +
                        File.separator + "src" +
                        File.separator + "main" +
                        File.separator + "resources";
                List<FileInfo> list = client.list(path);

                //noinspection unchecked
                assertThat(list, containsInAnyOrder(
                        equalTo(new FileInfo("main", true)),
                        equalTo(new FileInfo("test", true)),
                        equalTo(new FileInfo("file", false))
                ));

                FileEntry file = client.get(path + File.separator + "file");
                Path original = Paths.get(path + File.separator + "file");

                assertThat(file.size, equalTo((int) original.toFile().length()));
                assertThat(file.file, equalTo(Files.readAllBytes(original)));
                assertThat(server.isAlive(), is(true));
            }
        }
    }

    @Test
    public void get() throws IOException {
        try (FTPClient client = FTPClient.start("localhost")) {
            for (int i = 0; i < 2; i++) {
                String path = System.getProperty("user.dir") +
                        File.separator + "src" +
                        File.separator + "main" +
                        File.separator + "resources" +
                        File.separator + "file";
                FileEntry file = client.get(path);
                Path original = Paths.get(path);

                assertThat(file.size, equalTo((int) original.toFile().length()));
                assertThat(file.file, equalTo(Files.readAllBytes(original)));

                assertThat(server.isAlive(), is(true));
            }
        }
    }

    @Test
    public void badClients() throws InterruptedException, IOException {
        DataOutputStream outputStream = null;
        for (int i = 0; i < 2; i++) {
            socket = new Socket("localhost", FTPServer.PORT);
            outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeInt(0);
        }

        Thread.sleep(2000);
        assertThat(server.isAlive(), is(true));

        // checking the socket to be closed by the server
        DataOutputStream finalOutputStream = outputStream;
        assertThrows(IOException.class, () -> finalOutputStream.writeInt(0));
    }

    @AfterEach
    public void teardown() {
        server.destroy();
        socket = null;
    }
}