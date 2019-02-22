package com.justai.cm.core;

import com.justai.cm.core.domain.Env;
import com.justai.cm.utils.FileHelper;
import com.justai.cm.utils.YamlUtils;

public class EnvLoader {

    public static Env load(String fn, Components components) {
        FileHelper folder = new FileHelper(fn);

        Env env = YamlUtils.load(folder.child("env.yml").file, Env.class);
        env.setName(folder.name());

        env.build(folder);

        env.getComponents().forEach(c -> c.build(components, env, folder.child("!render"), folder.child("!pull")));

        return env;
    }

}
