package com.justai.cm.core.actions;

import com.justai.cm.Settings;
import com.justai.cm.core.domain.Cmp;
import com.justai.cm.core.domain.Commands;
import com.justai.cm.core.domain.Env;
import com.justai.cm.core.domain.Host;
import com.justai.cm.core.ssh.SshManager;
import com.justai.cm.utils.FileHelper;
import org.apache.commons.cli.CommandLine;

import java.io.Console;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

public abstract class BaseAction {

    protected final String actionName;

    public Actions actions;
    public SshManager sshManager;
    public Env env;
    public Settings settings;

    protected BaseAction() {
        this.actionName = this.getClass().getName();
    }

    public final void exec(Env env) {
        if (!settings.yes) {
            Console console = System.console();
            String answer = console.readLine("Action will be proceeded for whole environment %s, %d components will be affected. Are you sure (y/n)? ", env.getName(), env.getComponents().size());
            if (!answer.equalsIgnoreCase("y")) {
                return;
            }
        }
        logHeader("Execute " + actionName + " for the environment " + env.getName());

        List<Cmp> aPlan = computeActionSequence(env);

        aPlan.forEach(this::exec);
    }

    private List<Cmp> computeActionSequence(Env env) {
        ArrayList<Cmp> ret = new ArrayList<>();
        HashSet<String> processed = new HashSet<String>();

        ArrayList<Cmp> source = new ArrayList<>(env.getComponents());
        int idx = 0;
        while (!source.isEmpty()) {
            if (idx == source.size()) {
                throw new IllegalStateException("Circular dependency between components");
            }
            Cmp c = source.get(idx);
            if (processed.containsAll(c.getDependsOn())) {
                ret.add(c);
                source.remove(idx);
                idx = 0;
            } else {
                idx++;
            }
        }
        return ret;
    }

    public final void exec(Cmp cmp) {
        // check wildcards in host and invoke multiple times for each host
        List<Host> hosts = cmp.getZHosts();
        if (hosts == null) {
            hosts = Collections.singletonList(cmp.getZHost());
        }

        for (Host h : hosts) {
            cmp.setZHost(h);
            cmp.setZRenderFolder(env.getZFolder().child("!render").child(h.getName()).child(cmp.getId()));
            cmp.setZPullFolder(env.getZFolder().child("!pull").child(h.getName()).child(cmp.getId()));
            logHeader("Execute " + actionName + " for the component " + cmp.getFullId());

            exec0(cmp);
        }
    }

    protected void logHeader(String msg, Object ... args) {
        System.out.println("==== " + String.format(msg, args) + " ====");
    }

    protected void log(String msg, Object ... args) {
        System.out.println(String.format(msg, args));
    }

    protected abstract void exec0(Cmp cmp);

    public void exec(Settings settings, CommandLine commandLine) {
        if (commandLine.getArgs().length == 2) {
            String[] cmpIds = commandLine.getArgs()[1].split(",");
            for (String cmpId : cmpIds) {
                if (cmpId.endsWith("*")) {
                    env.getZComponents().entrySet().stream()
                            .filter(e -> e.getKey().startsWith(cmpId.substring(0, cmpId.length() - 1)))
                            .map(e -> e.getValue())
                            .forEach(c -> exec(c));
                } else {
                    Cmp cmp = env.getZComponents().get(cmpId);
                    if (cmp == null) {
                        throw new RuntimeException("No such component: " + cmpId);
                    }
                    exec(cmp);
                }
            }
        } else {
            exec(env);
        }
    }

    protected void executeRemoteCommand(Cmp cmp, String command) {
        // write props and arguments
        FileHelper source = cmp.getZRenderFolder().child("scripts").child("arguments.sh");
        FileHelper target = new FileHelper("cm/" + cmp.getId() + "/scripts/arguments.sh");

        writeSettings(source);
        sshManager.doWithConnection(cmp.getZHost(), ssh -> {
            ssh.pushFile(source, target);
        });


        // execute command
        sshManager.doWithConnection(cmp.getZHost(), ssh -> {
            Commands cmd = cmp.getZComponent().getCommands();
            String remoteFolder = ".cm/" + cmp.getId() + "/scripts/";

            // TODO: pass noStart argument
            if (cmd.getCommands().get(command) != null) {
                ssh.exec("sudo " + remoteFolder + cmd.getCommands().get(command).getCmd());
            } else {
                ssh.exec("sudo " + remoteFolder + cmd.getScript() + " " + command);
            }
        });

    }

    private void writeSettings(FileHelper source) {
        source.write(pw -> {
            pw.printf("ARG_NOSTART=%s\n", settings.noStart);
            pw.printf("ARG_NOMIGRATE=%s\n", settings.noMigrate);
            pw.printf("ARG_VERBOSE=%s\n", settings.verbose);
        });

    }
}
