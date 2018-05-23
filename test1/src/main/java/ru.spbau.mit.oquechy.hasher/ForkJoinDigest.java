package ru.spbau.mit.oquechy.hasher;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.RecursiveTask;

import static com.google.common.io.ByteStreams.nullOutputStream;


/**
 * Recursively walks a directory and computes md5 hash of the contained files.
 * Forks are made for each subdirectory of the given directory.
 */
public class ForkJoinDigest {

    /**
     * Fork-Join pool task. Runs naive algorithm for directories without
     * nested subdirectories. Forks for computing each subdirectory.
     */
    public static class DigestTask extends RecursiveTask<byte[]> {

        private final static int BUF_SIZE = 4096;

        private Path path;

        /**
         * Accepts root directory.
         */
        public DigestTask(Path path) {
            this.path = path;
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

        @Override
        protected byte[] compute() {

            try {
                long folders = Files.find( path, 1, (path, attributes) -> attributes.isDirectory()).count() - 1;
                if (folders == 0) {
                    return hashFiles();
                }

                Map<Path, RecursiveTask<byte[]>> tasks = new HashMap<>();
                Files.newDirectoryStream(path, p -> Files.isDirectory(p)).forEach(p -> {
                    DigestTask task = new DigestTask(p);
                    task.fork();
                    tasks.put(p, task);
                });


                TreeSet<Path> sorted = new TreeSet<>();
                Files.newDirectoryStream(path, p -> Files.isRegularFile(p) || Files.isDirectory(p))
                        .forEach(sorted::add);

                MessageDigest messageDigest = MessageDigest.getInstance("MD5");
                try (DigestOutputStream digestOutputStream = new DigestOutputStream(nullOutputStream(), messageDigest)) {
                    for (Path p : sorted) {
                        if (Files.isDirectory(p)) {
                            digestOutputStream.write(tasks.get(p).join());
                        } else {
                            digestOutputStream.write(hashFile(p));
                        }
                    }
                    return digestOutputStream.getMessageDigest().digest();
                }

            } catch (IOException | NoSuchAlgorithmException e) {
                throw new RuntimeException("Checksum failed");
            }
        }

        private byte[] hashFiles() throws IOException, NoSuchAlgorithmException {
            TreeSet<Path> sorted = new TreeSet<>();
            Files.newDirectoryStream(path, p -> Files.isRegularFile(p))
                    .forEach(sorted::add);

            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            try (DigestOutputStream digestOutputStream = new DigestOutputStream(nullOutputStream(), messageDigest)) {
                for (Path p : sorted) {
                    digestOutputStream.write(hashFile(p));
                }
                return digestOutputStream.getMessageDigest().digest();
            }
        }
    }
}
