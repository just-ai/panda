package com.justai.cm.core.ssh;

import com.jcraft.jsch.Session;
import com.justai.cm.utils.FileHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.justai.cm.utils.ExceptionUtils.wrap;

@Slf4j
public class LocalSshConnection extends SshConnection {

    public LocalSshConnection(boolean noChange) {
        super("", 0, "", "", "", noChange, false);
    }

    @Override
    public Pair<Integer, String> exec(String[] command) {
        System.out.println(Arrays.toString(command));
        if (noChange) {
            return Pair.of(0, "");
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            int ret = ExecUtils.exec(command, out);
            out.close();
            return Pair.of(ret, new String(out.toByteArray(), StandardCharsets.UTF_8)); // "cp866" ?
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void pushFile(FileHelper source, FileHelper target) {
        System.out.println("push " + source + " -> " + target);

        if (noChange) {
            return;
        }
        wrap(() -> FileUtils.copyFile(source.file, target.file));
    }

    @Override
    public void pushDirectory(FileHelper source, FileHelper target) {
        System.out.println("push " + source + "/* -> " + target);

        if (noChange) {
            return;
        }
        wrap(() -> FileUtils.copyDirectory(source.file, target.file));
    }

    @Override
    public void pullFile(FileHelper source, FileHelper target) {
        System.out.println("pull " + target + " <- " + source);
        wrap(() -> FileUtils.copyFile(source.file, target.file));
    }

    @Override
    public void pullDirectory(FileHelper source, FileHelper target) {
        System.out.println("pull " + target + " <- " + source + "/*");
        wrap(() -> FileUtils.copyDirectory(source.file, target.file));
    }

    @Override
    protected Session openSession() {
        return null;
    }

    @Override
    public void close() {
    }
}
