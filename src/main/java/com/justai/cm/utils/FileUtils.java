package com.justai.cm.utils;


import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.List;
import java.util.function.Consumer;

import static com.justai.cm.utils.ExceptionUtils.wrap;

public class FileUtils {


    public static void write(String filename, Consumer<PrintWriter> consumer) {
        File output = file(filename);

        PrintWriter pw;
        try {
            pw = new PrintWriter(new FileOutputStream(output));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        consumer.accept(pw);

        pw.close();
    }

    public static List<String> readLines(String filename) {
        try (InputStream is = open(filename)) {
            return IOUtils.readLines(is, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String readFile(String filename) {
        try (InputStream is = open(filename)) {
            return IOUtils.toString(is, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static File file(String filename) {
        return new File(filename).getAbsoluteFile();
    }

    public static FileInputStream open(String filename) {
        return wrap(() -> new FileInputStream(new File(filename).getAbsoluteFile()));
    }

}