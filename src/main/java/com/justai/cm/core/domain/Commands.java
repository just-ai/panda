package com.justai.cm.core.domain;

import lombok.Data;

import java.util.HashMap;

@Data
public class Commands {
    String script;

    HashMap<String, Command> commands = new HashMap<>();

}
