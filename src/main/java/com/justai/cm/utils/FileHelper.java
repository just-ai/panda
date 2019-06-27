package com.justai.cm.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.justai.cm.utils.FileUtils;

import static com.justai.cm.utils.ExceptionUtils.wrap;

public class FileHelper {
    public final File file;

    public FileHelper(String file) {
        this.file = new File(file);
    }

    public FileHelper(File file) {
        this.file = file;
    }

    public FileHelper child(String path) {
        return new FileHelper(new File(file, path));
    }

    public List<String> list() {
        String[] list = file.list();
        if (list == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(Arrays.asList(list));
    }

    public enum FileListFilter {
        ALL,
        FILES,
        FOLDERS
    }
    public List<String> list(FileListFilter filter) {
        File[] files = file.listFiles(file -> {
            switch (filter) {
                case ALL: return true;
                case FOLDERS: return file.isDirectory();
                case FILES: return !file.isDirectory();
            }
            throw new IllegalStateException();
        });
        if (files == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(files).map(File::getName).collect(Collectors.toList());
    }

    public String name() {
        return file.getName();
    }

    @Override
    public String toString() {
        return file.toString().replaceAll("\\\\", "/");
    }

    public String read() {
        return FileUtils.readFile(file.getAbsolutePath());
    }

    public List<String> readLines() {
       return FileUtils.readLines(file.getAbsolutePath());
    }

    public void write(String body) {
        FileUtils.write(file.getAbsolutePath(), pw -> pw.print(body));
    }

    public void write(Consumer<PrintWriter> action) {
        FileUtils.write(file.getAbsolutePath(), action);
    }

    public InputStream stream() {
        return wrap(() -> new FileInputStream(file));
    }

    public FileHelper mkdir() {
        file.mkdirs();
        return this;
    }

    public FileHelper delete() {
        file.delete();
        return this;
    }

    public void clean() {
        if (file.listFiles() == null) {
            return;
        }
        for (File f : file.listFiles()) {
            if (f.isFile()) {
                f.delete();
            }
            if (f.isDirectory()) {
                wrap(() -> org.apache.commons.io.FileUtils.deleteDirectory(f));
            }
        }
    }

    public void copy(FileHelper child) {
        wrap(() -> org.apache.commons.io.FileUtils.copyFile(file, child.file));
    }


}
