package ru.spbau.mit.oquechy.hasher;

import org.junit.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class SimpleDigestTest {

    private static final String PATH = SimpleDigest.class.getResource("/1").getPath();
    private static final String ANSWER = "ed78d2941b004b8f8fa1ad155f82da3c";

    @Test
    public void test() throws IOException, NoSuchAlgorithmException {
        System.out.println("Simple digest:");
        SimpleDigest digest = new SimpleDigest();
        long t = System.currentTimeMillis();
        String hash = digest.hashRecursive(PATH);
        assertThat(hash, equalTo(ANSWER));
        System.out.println(hash);
        System.out.print("time in mills: ");
        System.out.println(System.currentTimeMillis() - t);
    }
}