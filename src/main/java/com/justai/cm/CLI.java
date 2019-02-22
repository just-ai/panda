package com.justai.cm;

import com.justai.cm.core.EnvLoader;
import com.justai.cm.core.actions.Actions;
import com.justai.cm.core.Components;
import com.justai.cm.core.actions.BaseAction;
import com.justai.cm.core.actions.Encryptor;
import com.justai.cm.core.domain.Cmp;
import com.justai.cm.core.domain.Env;
import com.justai.cm.core.ssh.SshManager;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;

public class CLI {

    public static class Test {
        public static void main(String[] args) {
            CLI.main(
                    "-env stage render mon-server"
                    .split("\\s"));
        }
    }

    public static void main(String[] args) {
        if (System.getProperty("PANDA_HOME") == null) {
            System.setProperty("PANDA_HOME", ".");
        }
        try {
            new CLI().run(args);
        } catch (ExitError e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    public void run(String[] args) {
        CommandLine commandLine = parseCommandLine(args);

        if (commandLine.getArgs().length == 0) {
            throw new ExitError("No command name supplied");
        }

        Settings settings = parseSettings(commandLine);
        Encryptor.init(settings);

        SshManager sshManager = new SshManager(settings.ssh_user, settings.ssh_pass, settings.noChange);
        Components components = Components.load(settings.config_folder + "/Components");
        Env env = EnvLoader.load(settings.config_folder + "/" + settings.env, components);

        try {
            Actions actions = new Actions(sshManager, env, settings);

            String cmd0 = commandLine.getArgs()[0];
            for (String cmd : cmd0.split(",")) {
                BaseAction action = actions.actions.get(cmd);
                if (action == null) {
                    if (!cmd.equals("help")) {
                        System.err.println("Unknown command: " + cmd);
                    }
                    System.out.println("Supported commands are: ");
                    for (String s : actions.actions.keySet()) {
                        System.err.println("\t" + s);
                    }
                    return;
                }

                action.exec(settings, commandLine);
            }
        } finally {
            sshManager.shutdown();
        }
    }

    private Settings parseSettings(CommandLine commandLine) {
        return new Settings(commandLine);
    }

    private CommandLine parseCommandLine(String[] args) {
        CommandLineParser parser = new DefaultParser();
        try {
            return parser.parse(Settings.options, args);
        } catch (ParseException e) {
            throw new ExitError("Couldn't parse command line args: " + e.getMessage());
        }
    }

}
