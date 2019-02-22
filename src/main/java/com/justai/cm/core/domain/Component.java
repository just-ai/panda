package com.justai.cm.core.domain;

import com.justai.cm.core.actions.Encryptor;
import com.justai.cm.utils.FileHelper;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Component {
    String name;

    List<ConfigMap> configMap = new ArrayList<>();

    Map<String, Object> props = new HashMap<>();

    Commands commands;

    transient FileHelper configFolder;
    transient FileHelper scriptsFolder;

    public void build(FileHelper folder) {
        this.configFolder = folder.child("config");
        this.scriptsFolder = folder.child("scripts");

        Encryptor.decryptProps(getProps());
    }

}
