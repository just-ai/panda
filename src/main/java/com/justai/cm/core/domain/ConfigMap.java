package com.justai.cm.core.domain;

import lombok.Data;

@Data
public class ConfigMap {
    String source;
    String target;
    String user;
    String mode;
    boolean optional;
    boolean generatedByApp;
    boolean mkdirs;
    boolean directory;
}
