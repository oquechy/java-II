package ru.spbau.mit.oquechy.hasher;

import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ForkJoinPool;


/**
 * Compares time needed to compute checksum with simple
 * recursive algorithm and with fork-join framework
 */
public class Main {
    private static final String PATH = SimpleDigest.class.getResource("/1").getPath();

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        System.out.println("Simple digest:");
        SimpleDigest digest = new SimpleDigest();
        long t = System.currentTimeMillis();
        System.out.println(digest.hashRecursive(PATH));
        System.out.print("time in mills: ");
        System.out.println(System.currentTimeMillis() - t);

        System.out.println("Fork-join digest:");
        t = System.currentTimeMillis();
        System.out.println(Hex.encodeHexString(new ForkJoinPool(2)
                .invoke(new ForkJoinDigest.DigestTask(Paths.get(PATH)))));
        System.out.print("time in mills: ");
        System.out.println(System.currentTimeMillis() - t);
    }
}
