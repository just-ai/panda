package com.justai.cm.core.ssh;

import com.jcraft.jsch.*;
import com.justai.cm.utils.FileHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.justai.cm.utils.ExceptionUtils.wrap;

@Slf4j
public class SshConnection {

    private final String addr;
    private final int port;
    private final String login;
    private final String password;
    private String sshKeyPath;
    protected final boolean noChange;
    private boolean failOnScriptError;

    private Session session;
    private InputStream stdin;
    private OutputStream stdout;
    private OutputStream stderr;

    @Getter @Setter
    private boolean active;

    public SshConnection(String addr, int port, String login, String password, String sshKeyPath, boolean noChange, boolean failOnScriptError) {
        this.addr = addr;
        this.port = port;
        this.login = login;
        this.password = password;
        this.sshKeyPath = sshKeyPath;
        this.noChange = noChange;
        this.failOnScriptError = failOnScriptError;

        this.session = openSession();
    }

    public Pair<Integer, String> exec(String command) {
        return exec(command.split(" "));
    }

    public Pair<Integer, String> exec(String[] command) {
        ChannelExec channel = (ChannelExec) wrap(() -> this.session.openChannel("exec"));
        String cmd = Arrays.stream(command).map(s -> s.contains(" ") ? "'" + s + "'" : s).collect(Collectors.joining(" "));
        System.out.println(cmd);
        if (noChange) {
            return Pair.of(0, "");
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
            channel.setErrStream(new TeeOutputStream(out, System.err), true);
            channel.setOutputStream(new TeeOutputStream(out, System.out), true);
            channel.setInputStream(in, false);
            channel.setCommand(cmd);
            wrap(() -> channel.connect());

            int code = wrap(() -> code(channel));
            String msg = wrap(() -> out.toString("UTF-8"));

            if (failOnScriptError && code != 0) {
                throw new RuntimeException("Remote script failed with status code " + code + " and message " + msg);
            }
            return Pair.of(code, msg);
        } finally {
            channel.disconnect();
        }
    }

    public void pushFile(FileHelper source, FileHelper target) {
        ChannelSftp channel = (ChannelSftp) wrap(() -> this.session.openChannel("sftp"));
        System.out.println("push " + source + " -> " + target);

        if (noChange) {
            return;
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
            channel.setOutputStream(out, false);
            channel.setInputStream(in, false);

            wrap(() -> channel.connect());

            channel.put(source.stream(), target.toString());
        } catch (SftpException e) {
            System.err.println("Cannot push file: " + e.getMessage());
        } finally {
            channel.disconnect();
        }
    }

    public void pushDirectory(FileHelper source, FileHelper target) {
        ChannelSftp channel = (ChannelSftp) wrap(() -> this.session.openChannel("sftp"));
        System.out.println("push " + source + "/* -> " + target);

        if (noChange) {
            return;
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
            channel.setOutputStream(out, false);
            channel.setInputStream(in, false);

            wrap(() -> channel.connect());

            channel.mkdir(target.toString());
            for (String f : source.list()) {
                if (!source.child(f).file.isFile()) {
                    continue;
                }
                System.out.println("push " + source.toString().replaceAll(".", " ") + "/" + f + " -> ");
                channel.put(source.child(f).stream(), target.child(f).toString());
            }
        } catch (SftpException e) {
            System.err.println("Cannot push file: " + e.getMessage());
        } finally {
            channel.disconnect();
        }
    }

    public void pullFile(FileHelper source, FileHelper target) {
        ChannelSftp channel = (ChannelSftp) wrap(() -> this.session.openChannel("sftp"));

        System.out.println("pull " + target + " <- " + source);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
            channel.setOutputStream(out, false);
            channel.setInputStream(in, false);

            wrap(() -> channel.connect());

            InputStream is = channel.get(source.toString());
            FileUtils.copyInputStreamToFile(is, target.file);
        } catch (Exception e) {
            System.err.println("Cannot pull file: " + e.getMessage());
        } finally {
            channel.disconnect();
        }
    }

    public void pullDirectory(FileHelper source, FileHelper target) {
        ChannelSftp channel = (ChannelSftp) wrap(() -> this.session.openChannel("sftp"));

        System.out.println("pull " + target + " <- " + source + "/*");
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
            channel.setOutputStream(out, false);
            channel.setInputStream(in, false);

            wrap(() -> channel.connect());

            Vector<ChannelSftp.LsEntry> list = channel.ls(source.toString());
            List<String> files = list.stream()
                    .map(f -> f.getFilename())
                    .filter(f -> !(f.equals(".") || f.equals("..")))
                    .collect(Collectors.toList());
            for (String f : files) {
                System.out.println("pull " + target.toString().replaceAll(".", " ") + " <- " + source + "/" + f);
                try {
                    InputStream is = channel.get(source.child(f).toString());
                    FileUtils.copyInputStreamToFile(is, target.child(f).file);
                } catch (Exception e) {
                    System.err.println("Cannot pull file: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Cannot pull file: " + e.getMessage());
        } finally {
            channel.disconnect();
        }
    }

    private int code(final ChannelExec exec) throws IOException {
        while (!exec.isClosed()) {
            try {
                this.session.sendKeepAliveMsg();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(ex);
            }
        }
        return exec.getExitStatus();
    }

    protected Session openSession() {
        try {
            JSch.setConfig("StrictHostKeyChecking", "no");
            JSch.setConfig("PreferredAuthentications", "publickey,password");
            JSch.setLogger(new JschLogger());
            JSch jsch = new JSch();
            if (StringUtils.isNotEmpty(sshKeyPath)) {
                jsch.addIdentity(sshKeyPath);
            }
            log.debug(
                    "Opening SSH session to {}@{}:{} (auth with password)...",
                    login, addr, port
            );
            Session session = jsch.getSession(login, addr, port);
            session.setPassword(this.password);
            session.setServerAliveInterval((int) TimeUnit.SECONDS.toMillis(10));
            session.setServerAliveCountMax(1000000);
            session.connect();
            return session;
        } catch (final JSchException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void close() {
        session.disconnect();
    }

}
