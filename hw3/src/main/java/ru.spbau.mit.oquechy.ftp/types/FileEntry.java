package ru.spbau.mit.oquechy.ftp.types;

public class FileEntry {
    public int size;
    public byte[] file;

    public FileEntry(int size) {
        this.size = size;
        this.file = new byte[size];
    }
}
