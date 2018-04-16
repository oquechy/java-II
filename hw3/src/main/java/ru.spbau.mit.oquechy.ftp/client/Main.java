package ru.spbau.mit.oquechy.ftp.client;

import ru.spbau.mit.oquechy.ftp.types.FileEntry;
import ru.spbau.mit.oquechy.ftp.types.FileInfo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Scanner;

public class Main {

    public final static String USAGE =
            "Pass the address of running FTPServer as argument\n" +
            "\n" +
            "Commands in interactive mode:\n" +
            "\tls <dir_path> \t\t-- list remote directory\n" +
            "\twget <file_path> \t-- fetch remote file\n" +
            "\tq \t\t\t\t\t-- quit\n";

    public static void main(String[] args) throws IOException {
        System.out.println(USAGE);
        if (args.length != 1) {
            return;
        }

        try (FTPClient client = FTPClient.start(args[0])) {
            Scanner scanner = new Scanner(System.in);

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
                        String file = scanner.next();
                        print(client.get(file));
                        break;
                    default:
                        System.out.println(USAGE);
                }
            }
        }
    }

    private static void print(FileEntry fileEntry) {
        if (fileEntry.size == 0) {
            System.out.println("Directory, empty file or nonexistent file");
            return;
        }

        System.out.println(new String(fileEntry.file, Charset.defaultCharset()));
    }

    private static void print(List<FileInfo> list) {
        if (list.size() == 0) {
            System.out.println("Empty or nonexistent directory");
        }
        for (FileInfo fileInfo : list) {
            System.out.println((fileInfo.isDirectory ? "\u001B[1;32m" : "") + fileInfo.name +
                    (fileInfo.isDirectory ? File.separator : "") + "\u001B[0m");
        }
    }
}
