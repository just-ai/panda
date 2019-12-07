package com.justai.cm;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created by User on 22.07.2016.
 */
public class Settings {
    public static final String PANDA_HOME = System.getProperty("PANDA_HOME");

    // property keys
    public static final Option ENV = new Option("env", true, "Environment");
    public static final Option NO_START = new Option("noStart", false, "Don't start service on deploy");
    public static final Option NO_MIGRATE = new Option("noMigrate", false, "Don't migrate db schema");
    public static final Option NO_PUSH = new Option("noPush", false, "Don't push configuration before deploy/reload commands");
    public static final Option NO_RENDER = new Option("noRender", false, "Don't render configuration before push command");
    public static final Option NO_CHANGE = new Option("n", false, "Don't modify anything on remote hosts");


    public static final Option YES = new Option("y", false, "Accept all confirmations");

    public static final Option CONFIG_FOLDER = new Option("config_folder", true, "Folder with configuration files");
    public static final Option USERNAME = new Option("ssh_user", true, "Username for remote ssh user");
    public static final Option PASSWORD = new Option("ssh_pass", true, "Password for remote ssh user");
    public static final Option SUDO_WITH_PASS = new Option("sudo_with_pass", false, "Pass password for sudo");
    public static final Option SSH_KEY_PATH = new Option("ssh_key_path", true, "Path to ssh private key");
    public static final Option MASTER_PASSWORD = new Option("master_pass", true, "Password to decrypt secret properties");
    public static final Option MASTER_KEY = new Option("master_key", true, "Key file to decrypt secret properties");
    public static final Option VERBOSE = new Option("v", false, "Enable verbose output");
    public static final Option FAIL_ON_SCRIPT_ERROR = new Option("f", false, "Fail on error status code");

    public static final Options options = new Options();

    static {
        options.addOption(Settings.ENV);
        options.addOption(Settings.NO_START);
        options.addOption(Settings.NO_PUSH);
        options.addOption(Settings.NO_RENDER);
        options.addOption(Settings.NO_CHANGE);
        options.addOption(Settings.NO_MIGRATE);

        options.addOption(Settings.YES);

        options.addOption(Settings.CONFIG_FOLDER);

        options.addOption(Settings.USERNAME);
        options.addOption(Settings.PASSWORD);
        options.addOption(Settings.SUDO_WITH_PASS);

        options.addOption(Settings.MASTER_PASSWORD);
        options.addOption(Settings.MASTER_KEY);

        options.addOption(Settings.VERBOSE);
        options.addOption(Settings.FAIL_ON_SCRIPT_ERROR);
    }

    public final Map<String, String> props;

    public final String env;
    public final boolean noStart;
    public final boolean noPush;
    public final boolean noRender;
    public final boolean noChange;
    public final boolean noMigrate;

    public final boolean yes;

    public final String config_folder;

    public final String ssh_user;
    public final String ssh_pass;
    public final boolean sudo_with_pass;
    public final String ssh_key_path;
    public final boolean verbose;
    public final boolean failOnScriptError;

    public final String master_pass;
    public final String master_key;

    public Settings(CommandLine cmd) {
        this(loadProperties(cmd));
    }

    public Settings(Map<String, String> properties) {
        this.props = Collections.unmodifiableMap(properties);
        this.env = properties.get(ENV.getOpt());
        this.noStart = properties.containsKey(NO_START.getOpt());
        this.noPush = properties.containsKey(NO_PUSH.getOpt());
        this.noRender = properties.containsKey(NO_RENDER.getOpt());
        this.noChange = properties.containsKey(NO_CHANGE.getOpt());
        this.noMigrate = properties.containsKey(NO_MIGRATE.getOpt());

        this.yes = properties.containsKey(YES.getOpt());

        this.config_folder = properties.get(CONFIG_FOLDER.getOpt());

        this.ssh_user = properties.get(USERNAME.getOpt());
        this.ssh_pass = properties.get(PASSWORD.getOpt());
        this.sudo_with_pass = properties.containsKey(SUDO_WITH_PASS.getOpt());
        this.ssh_key_path = properties.get(SSH_KEY_PATH.getOpt());

        this.master_pass = properties.get(MASTER_PASSWORD.getOpt());
        this.master_key = properties.get(MASTER_KEY.getOpt());
        this.verbose = properties.containsKey(VERBOSE.getOpt());
        this.failOnScriptError = properties.containsKey(FAIL_ON_SCRIPT_ERROR.getOpt());
    }

    private boolean propertyExist(Map<String, String> properties, String property) {
        return properties.containsKey(property) && (properties.get(property) == null || Boolean.parseBoolean(properties.get(property)));
    }

    private static Map<String, String> loadProperties(CommandLine cmd) {
        Map<String, String> ret = new HashMap<>();

        File[] locations = new File[] {
                new File(PANDA_HOME + "/conf/panda.properties"), // default props
                new File(System.getProperty("user.home") + "/.panda.properties"), // user props
                new File("panda.properties"), // project props
                new File("local.properties") // local props for project
        };

        for (File f : locations) {
            if (f.exists()) {
                loadFromFile(f, ret);
            }
        }

        loadFromCommandLine(cmd, ret);

        return ret;
    }

    public static void loadFromFile(File file, Map<String, String> map) {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Map.Entry<Object, Object> e : props.entrySet()) {
            map.put(e.getKey().toString(), e.getValue().toString());
        }
    }

    public static void loadFromCommandLine(CommandLine commandLine, Map<String, String> map) {
        for (Option opt : commandLine.getOptions()) {
            map.put(opt.getOpt(), opt.getValue());
        }
    }
}
