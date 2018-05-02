package ru.spbau.mit.oquechy.ftp.client;

import org.jetbrains.annotations.NotNull;
import ru.spbau.mit.oquechy.ftp.types.FileEntry;
import ru.spbau.mit.oquechy.ftp.types.FileInfo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Scanner;

/**
 * Console app providing interactive mode of communication
 * with {@link ru.spbau.mit.oquechy.ftp.server.FTPServer}
 * via {@link FTPClient}
 *
 * IMPORTANT: Paths must be absolute and mustn't contain quotes
 *            Destination path can't contain nonexistent directories
 *
 *          ls dir_path               -- list remote directory
 *          wget src_path dst_path    -- fetch file from server's path src_path
 *                                       and save it to local path dst_path
 *          q                         -- quit
 */
public class ConsoleClient {

    private final static String USAGE =
            "Pass the address of running FTPServer as argument\n" +
            "\n" +
            "Commands in interactive mode:\n" +
            "\t%-30s -- list remote directory\n" +
            "\t%-30s -- fetch remote file and save it locally\n" +
            "\t%-30s -- quit\n";

    /**
     * Entry point
     * @param args arg[0], the only argument, contains hostname
     * @throws IOException when I/O fails
     */
    public static void main(String[] args) throws IOException {
        usage();
        if (args.length != 1) {
            return;
        }

        try (@NotNull FTPClient client = FTPClient.start(args[0])) {
            @NotNull Scanner scanner = new Scanner(System.in);

            while (true) {
                String mode = scanner.next();

                switch (mode) {
                    case "q":
                        return;
                    case "ls":
                        String path = scanner.next();
                        print(client.list(path));
                        break;
                    case "wget":
                        String src = scanner.next();
                        String dst = scanner.next();
                        print(client.get(src, dst), dst);
                        break;
                    default:
                        usage();
                }
            }
        }
    }

    private static void usage() {
        System.out.printf(USAGE, "ls <dir_path>", "wget <src_path> <dst_path>", "q");
    }

    private static void print(boolean success, String dst) {
        if (success) {
            System.out.println("Saved to " + dst);
            return;
        }

        System.out.println("Directory, empty file or nonexistent file");
    }

    private static void print(List<FileInfo> list) {
        if (list.size() == 0) {
            System.out.println("Empty or nonexistent directory");
        }
        for (@NotNull FileInfo fileInfo : list) {
            System.out.println((fileInfo.isDirectory ? "\u001B[1;32m" : "") + fileInfo.name +
                    (fileInfo.isDirectory ? File.separator : "") + "\u001B[0m");
        }
    }
}
