package com.justai.cm.core.actions;

import com.justai.cm.Settings;
import com.justai.cm.core.domain.Env;
import com.justai.cm.core.ssh.SshManager;

import java.util.HashMap;

public class Actions {

    private final Settings settings;
    private final SshManager sshManager;
    private final Env env;
    public HashMap<String, BaseAction> actions;

    public Actions(SshManager sshManager, Env env, Settings settings) {
        this.sshManager = sshManager;
        this.env = env;
        this.settings = settings;
        init();
    }

    private void init() {
        actions = new HashMap<>();
        actions.put("render", new Render());
        actions.put("push", new Push());
        actions.put("pull", new Pull());
        actions.put("deploy", new Deploy());
        actions.put("reload", new Reload());
        actions.put("start", new Start());
        actions.put("stop", new Stop());
        actions.put("restart", new Restart());

        actions.put("encrypt", new Encrypt());
        actions.put("decrypt", new Decrypt());

        actions.put("encryptFile", new EncryptFile());
        actions.put("decryptFile", new DecryptFile());

        actions.values().forEach(a -> {
            a.actions = this;
            a.sshManager = sshManager;
            a.env = env;
            a.settings = settings;
        });
    }

    public BaseAction get(String action) {
        return actions.get(action);
    }

    public <T> T get(Class<T> clazz) {
        return (T) actions.values().stream().filter(c -> c.getClass() == clazz).findAny().get();
    }

}
