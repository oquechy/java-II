package ru.spbau.mit.oquechy.ftp.types;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Data class used by {@link ru.spbau.mit.oquechy.ftp.client.FTPClient}
 * for representing server's responds.
 */
@EqualsAndHashCode
public class FileInfo {
    @Getter
    private String name;

    @Getter
    private boolean isDirectory;

    /**
     * Initializes fields.
     *
     * @param name file name
     * @param isDirectory true if file is a directory
     */
    public FileInfo(String name, boolean isDirectory) {
        this.name = name;
        this.isDirectory = isDirectory;
    }

    /**
     * Gives string representation of file name.
     */
    @Override
    public String toString() {
        return name;
    }
}
