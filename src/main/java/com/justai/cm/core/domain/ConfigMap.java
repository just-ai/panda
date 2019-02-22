package com.justai.cm.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class ConfigMap {
    String source;
    String target;
    String user;
    String mode;
    boolean optional;
    boolean mkdirs;
    boolean directory;
}
