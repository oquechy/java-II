package ru.spbau.mit.oquechy.stress.client;

import java.io.IOException;

import static java.lang.Thread.sleep;

public class ClientLauncher {
    private static final String USAGE = "<host> <port> <length> <queries> <delay> " +
            "<clients> <property> <iterations> <step>";

    public static void main(String[] args) throws InterruptedException {
        if (args.length != 9) {
            System.out.println("USAGE: " + USAGE);
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        int length = Integer.parseInt(args[2]);
        int queries = Integer.parseInt(args[3]);
        int delay = Integer.parseInt(args[4]);
        int clients = Integer.parseInt(args[5]);
        String property = args[6];
        int iterations = Integer.parseInt(args[7]);
        int step = Integer.parseInt(args[8]);

        Thread[] threads;

        for (int i = 0; i < iterations; i++) {
            threads = new Thread[clients];
            for (int j = 0; j < clients; j++) {
                int finalLength = length;
                int finalDelay = delay;
                threads[j] = new Thread(() -> {
                    try {
                        new Client(host, port, finalLength, queries, finalDelay);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                threads[j].start();
            }
            length += (property.equals("N") ? 1 : 0) * step;
            clients += (property.equals("M") ? 1 : 0) * step;
            delay += (property.equals("D") ? 1 : 0) * step;
            for (Thread thread : threads) {
                thread.join();
            }
            sleep(2000);
        }
    }
}
