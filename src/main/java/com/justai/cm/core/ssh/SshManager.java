package com.justai.cm.core.ssh;

import com.justai.cm.core.domain.Host;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.function.Consumer;

@Slf4j
public class SshManager {

    private final String user;
    private final String password;
    private String sshKeyPath;
    private final boolean noChange;
    private boolean failOnScriptError;
    private final HashMap<String, SshConnection> connections;

    public SshManager(
            String user,
            String password,
            String sshKeyPath,
            boolean noChange,
            boolean failOnScriptError) {
        this.user = user;
        this.password = password;
        this.sshKeyPath = sshKeyPath;
        this.noChange = noChange;
        this.failOnScriptError = failOnScriptError;
        connections = new HashMap<>();
    }

    public void doWithConnection(Host host, Consumer<SshConnection> action) {
        SshConnection connection = obtainConnection(host);
        try {
            action.accept(connection);
        } finally {
            releaseConnection(connection);
        }
    }

    synchronized private SshConnection obtainConnection(Host host) {
        SshConnection cn = getOrCreateConnection(host);
        while (cn.isActive()) {
            try {
                this.wait(1000);
            } catch (InterruptedException e) {
            }
        }
        cn.setActive(true);
        return cn;
    }

    private synchronized SshConnection getOrCreateConnection(Host host) {
        SshConnection cn = connections.get(host.getFqdn());
        if (cn == null) {
            String addr = StringUtils.isNotEmpty(host.getIp()) ? host.getIp() : host.getFqdn();
            if (addr.equals("localhost") || addr.equals("127.0.0.1")) {
                cn = new LocalSshConnection(noChange);
            } else {
                cn = new SshConnection(addr, Integer.parseInt(host.getSshPort()), user, password, sshKeyPath, noChange, failOnScriptError);
            }
            connections.put(host.getFqdn(), cn);
        }
        return cn;
    }

    private synchronized void releaseConnection(SshConnection connection) {
        connection.setActive(false);
        this.notifyAll();
    }

    public void shutdown() {
        connections.values().forEach(c -> c.close());
    }
}
