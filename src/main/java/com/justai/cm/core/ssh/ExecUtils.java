package com.justai.cm.core.ssh;

import org.apache.commons.io.output.TeeOutputStream;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ExecUtils {

    public static void main(String[] args) {
        System.out.println(execToString("cmd /C dir"));
    }

    public static int exec(String cmd, ByteArrayOutputStream outputStream) throws IOException {
        Process p = Runtime.getRuntime().exec(cmd);

        Thread _stdout = new Thread(new StreamReader(p.getInputStream(), new TeeOutputStream(outputStream, System.out)));
        Thread _stderr = new Thread(new StreamReader(p.getErrorStream(), new TeeOutputStream(outputStream, System.err)));

        _stdout.start();
        _stderr.start();

        int ret = -1000;
        try {
            ret = p.waitFor();
            _stdout.join();
            _stderr.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static String execToString(String cmd) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            int ret = exec(cmd, out);
            out.close();
            if( ret != -1000) {
                return new String(out.toByteArray(), StandardCharsets.UTF_8); // "cp866" ?
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class StreamReader implements Runnable{
        InputStream input;
        OutputStream output;

        private StreamReader(InputStream input, OutputStream output) {
            this.input = input;
            this.output = output;
        }

        @Override
        public void run() {
            try {
                int c;
                byte buff[] = new byte[1024];
                while( (c = input.read(buff)) > 0 ) {
                    output.write(buff, 0, c);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
