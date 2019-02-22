package com.justai.cm.core;

import com.justai.cm.core.domain.Component;
import com.justai.cm.utils.FileHelper;
import com.justai.cm.utils.YamlUtils;

import java.util.HashMap;

public class Components {

    public HashMap<String, Component> components = new HashMap<>();

    public static Components load(String fn) {
        FileHelper folder = new FileHelper(fn);
        Components ret = new Components();
        for (String f : folder.list()) {
            ret.loadComponent(fn, folder.child(f));
        }
        return ret;
    }

    private void loadComponent(String baseName, FileHelper folder) {
        if (folder.child("component.yml").file.exists()) {
            String name = folder.file.toString().substring(baseName.length() + 1).replaceAll("\\\\", "/");
            loadComponent0(name, folder);
        } else {
            for (String f : folder.list()) {
                this.loadComponent(baseName, folder.child(f));
            }
        }
    }

    private void loadComponent0(String name, FileHelper folder) {
        Component cmp = YamlUtils.load(folder.child("component.yml").file, Component.class);
        cmp.setName(name);
        cmp.build(folder);

        components.put(name, cmp);
    }

    public Component get(String name) {
        return components.get(name);
    }
}
