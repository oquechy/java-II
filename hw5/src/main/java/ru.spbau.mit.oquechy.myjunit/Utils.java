package ru.spbau.mit.oquechy.myjunit;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class Utils {
    @NotNull
    public static String getClassName(File classes, File file) {
        return getRelativePath(file, classes).split("\\.class")[0].replace(File.separatorChar, '.');
    }

    private static String getRelativePath(File directory, File file) {
        return file.toPath().relativize(directory.toPath()).toString();
    }
}