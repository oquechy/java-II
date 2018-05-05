package ru.spbau.mit.oquechy.ftp.server;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.spbau.mit.oquechy.ftp.client.FTPClient;
import ru.spbau.mit.oquechy.ftp.types.FileInfo;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FTPTest {
    private final static String TEST_DIR = System.getProperty("user.dir") +
            File.separator + "src" +
            File.separator + "test" +
            File.separator + "resources";
    private final static String DST_DIR = TEST_DIR + File.separator + "test";
    private final static String FILE = TEST_DIR + File.separator + "file";
    private final static String LOCALHOST = "localhost";
    private Process server;
    @Nullable
    private Socket socket;

    @BeforeEach
    public void setup() throws IOException, InterruptedException {
        FileUtils.cleanDirectory(new File(DST_DIR));
        server = FTPServer.start();
        Thread.sleep(1000);            // waiting for server to init
    }

    @Test
    public void correctListQuery() throws IOException {
        for (int i = 0; i < 2; i++) {
            try (@NotNull FTPClient client = FTPClient.start(LOCALHOST)) {
                @NotNull List<FileInfo> list = client.list(TEST_DIR);

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
    public void invalidListQuery() throws IOException {
        for (int i = 0; i < 2; i++) {
            try (@NotNull FTPClient client = FTPClient.start(LOCALHOST)) {
                @NotNull String path = TEST_DIR + File.separator + "file";
                @NotNull List<FileInfo> list = client.list(path);
                assertThat(list, is(empty()));
                assertThat(server.isAlive(), is(true));
            }
        }
    }

    @Test
    public void invalidGetQuery() throws IOException {
        for (int i = 0; i < 2; i++) {
            try (@NotNull FTPClient client = FTPClient.start(LOCALHOST)) {
                assertThat(client.get(DST_DIR, DST_DIR + File.separator + "invalidGet.txt"), is(false));
                assertThat(Objects.requireNonNull(new File(DST_DIR).listFiles()).length, is(0));
                assertThat(server.isAlive(), is(true));
            }
        }
    }

    @Test
    public void correctGetAndListQueries() throws IOException {
        for (int i = 0; i < 2; i++) {
            try (@NotNull FTPClient client = FTPClient.start(LOCALHOST)) {
                @NotNull List<FileInfo> list = client.list(TEST_DIR);

                //noinspection unchecked
                assertThat(list, containsInAnyOrder(
                        equalTo(new FileInfo("main", true)),
                        equalTo(new FileInfo("test", true)),
                        equalTo(new FileInfo("file", false))
                ));

                @NotNull String dstFile = DST_DIR + File.separator + "getAndList.txt";
                assertThat(client.get(FILE, dstFile), is(true));
                @NotNull File expected = new File(FILE);
                @NotNull File actual = new File(dstFile);
                assertThat(FileUtils.contentEquals(expected, actual), is(true));
                assertThat(server.isAlive(), is(true));
            }
        }
    }

    @Test
    public void correctGetQuery() throws IOException {
        for (int i = 0; i < 2; i++) {
            try (@NotNull FTPClient client = FTPClient.start(LOCALHOST)) {
                @NotNull String dstFile = DST_DIR + File.separator + "get.txt";
                assertThat(client.get(FILE, dstFile), is(true));
                @NotNull File expected = new File(FILE);
                @NotNull File actual = new File(dstFile);
                assertThat(FileUtils.contentEquals(expected, actual), is(true));
                assertThat(server.isAlive(), is(true));
            }
        }
    }

    @Test
    public void getBigFile() throws IOException {
        @NotNull String srcFile = DST_DIR + File.separator + "bigFile" +
                ".bin";
        @NotNull String dstFile = DST_DIR + File.separator + "getBigFile.bin";
        try (@NotNull RandomAccessFile bigFile = new RandomAccessFile(srcFile, "rw")) {
            bigFile.setLength(100_000_000);
        }

        for (int i = 0; i < 2; i++) {
            try (@NotNull FTPClient client = FTPClient.start(LOCALHOST)) {
                assertThat(client.get(srcFile, dstFile), is(true));
                @NotNull File expected = new File(srcFile);
                @NotNull File actual = new File(dstFile);
                assertThat(FileUtils.contentEquals(expected, actual), is(true));
                assertThat(server.isAlive(), is(true));
            }
        }
    }

    @Test
    public void badClients() throws InterruptedException, IOException {
        @Nullable DataOutputStream outputStream = null;
        for (int i = 0; i < 2; i++) {
            socket = new Socket(LOCALHOST, FTPServer.PORT);
            outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeInt(0);
        }

        // waiting for server to finish processing queries
        Thread.sleep(1000);
        assertThat(server.isAlive(), is(true));

        // checking the socket to be closed by the server
        @Nullable DataOutputStream finalOutputStream = outputStream;
        assertThrows(IOException.class, () -> finalOutputStream.writeInt(0));
    }

    @Test
    public void multiClients() throws IOException {
        @NotNull String dstFile1;
        @NotNull String dstFile2;
        try (@NotNull FTPClient client1 = FTPClient.start(LOCALHOST);
             @NotNull FTPClient client2 = FTPClient.start(LOCALHOST)) {
            dstFile1 = DST_DIR + File.separator + "multiClients1.txt";
            dstFile2 = DST_DIR + File.separator + "multiClients2.txt";
            assertThat(client1.get(FILE, dstFile1), is(true));
            assertThat(client2.get(FILE, dstFile2), is(true));
        }
        @NotNull File expected = new File(FILE);
        @NotNull File actual1 = new File(dstFile1);
        @NotNull File actual2 = new File(dstFile2);
        assertThat(FileUtils.contentEquals(expected, actual1), is(true));
        assertThat(FileUtils.contentEquals(expected, actual2), is(true));
        assertThat(server.isAlive(), is(true));
    }

    @AfterEach
    public void teardown() throws IOException, InterruptedException {
        FileUtils.cleanDirectory(new File(DST_DIR));
        server.destroy();
        socket = null;
        Thread.sleep(1000);
    }
}