package ru.spbau.mit.oquechy.myjunit;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Utilities for operating withs paths and filenames.
 */
public class Utils {
    /**
     * Returns the full name of class described in the file.
     *
     * @param classes directory which contains .class file, the root of package hierarchy
     * @param file file which contains a class
     */
    @NotNull
    public static String getClassName(File classes, File file) {
        return getRelativePath(file, classes).split("\\.class")[0].replace(File.separatorChar, '.');
    }

    private static String getRelativePath(File directory, File file) {
        return file.toPath().relativize(directory.toPath()).toString();
    }
}