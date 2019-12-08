package com.justai.cm.core;

import com.justai.cm.core.domain.Env;
import com.justai.cm.utils.FileHelper;
import com.justai.cm.utils.YamlUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class EnvLoader {

    public static Env load(String fn, Components components) {
        FileHelper folder = new FileHelper(fn);

        Env env = YamlUtils.load(folder.child("env.yml").file, Env.class);
        if (env.getInclude() != null) {
            env.getInclude().forEach(includeName -> {
                Env include = YamlUtils.load(folder.child(includeName).file, Env.class);
                if (env.getProps() == null) {
                    env.setProps(new HashMap<>());
                }
                if (include.getProps() != null) {
                    env.getProps().putAll(include.getProps());
                }

                if (env.getComponents() == null) {
                    env.setComponents(new ArrayList<>());
                }
                if (include.getComponents() != null) {
                    env.getComponents().addAll(include.getComponents());
                }

                if (env.getHosts() == null) {
                    env.setHosts(new ArrayList<>());
                }
                if (include.getHosts() != null) {
                    env.getHosts().addAll(include.getHosts());
                }
            });
        }
        env.setName(folder.name());

        env.build(folder);

        env.getComponents().forEach(c -> c.build(components, env, folder.child("!render"), folder.child("!pull")));

        return env;
    }

}
