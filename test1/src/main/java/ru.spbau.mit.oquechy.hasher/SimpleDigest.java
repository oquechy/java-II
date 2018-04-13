package ru.spbau.mit.oquechy.hasher;

import org.apache.commons.codec.binary.Hex;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.TreeSet;

import static com.google.common.io.ByteStreams.nullOutputStream;

/**
 * Class for recursive computing md5 checksum.
 */
public class SimpleDigest {

    private final static int BUF_SIZE = 4096;

    /**
     * Naive algorithm for computing md5 checksum.
     * @param path root directory
     * @return hex string with hash
     */
    public String hashRecursive(String path) throws IOException, NoSuchAlgorithmException {
        return Hex.encodeHexString(hashRecursive(Paths.get(path)));
    }

    private byte[] hashRecursive(Path path) throws IOException, NoSuchAlgorithmException {
        TreeSet<Path> sorted = new TreeSet<>();
        Files.newDirectoryStream(path, p -> Files.isRegularFile(p) || Files.isDirectory(p))
                .forEach(sorted::add);

        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        try (DigestOutputStream digestOutputStream = new DigestOutputStream(nullOutputStream(), messageDigest)) {
            for (Path p : sorted) {
                if (Files.isDirectory(p)) {
                    digestOutputStream.write(hashRecursive(p));
                } else {
                    digestOutputStream.write(hashFile(p));
                }
            }
            return digestOutputStream.getMessageDigest().digest();
        }
    }

    private byte[] hashFile(Path file) throws NoSuchAlgorithmException, IOException {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        FileInputStream inputStream = new FileInputStream(file.toFile());
        try (DigestInputStream digestInputStream = new DigestInputStream(inputStream, messageDigest)) {

            byte[] buffer = new byte[BUF_SIZE];
            //noinspection StatementWithEmptyBody
            while (digestInputStream.read(buffer) > -1) { }
            return digestInputStream.getMessageDigest().digest();
        }
    }
}

