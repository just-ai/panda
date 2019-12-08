package com.justai.cm.core.domain;

import com.justai.cm.core.actions.Encryptor;
import com.justai.cm.utils.FileHelper;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Env {

    String name;

    Map<String, Object> props = new HashMap<>();

    List<Host> hosts;

    List<Cmp> components;

    List<String> include;

    transient FileHelper zFolder;
    transient HashMap<String, Host> zHosts = new HashMap<>();
    transient HashMap<String, Cmp> zComponents = new HashMap<>();

    public void build(FileHelper folder) {
        zFolder = folder;
        hosts.forEach(h -> {
            if (StringUtils.isEmpty(h.getName()) || zHosts.containsKey(h.getName())) {
                throw new RuntimeException("Host name is not unique: " + h.getName());
            }
            zHosts.put(h.getName(), h);
        });
        components.forEach(c -> {
            if (StringUtils.isEmpty(c.getId()) || zComponents.containsKey(c.getId())) {
                throw new RuntimeException("Component id is not unique: " + c.getId());
            }
            zComponents.put(c.getId(), c);
        });

        Encryptor.decryptProps(props);
    }

    public void setProps(Map<String, Object> props) {
        if (props == null) {
            this.props = new HashMap<>();
        }
        else {
            this.props = props;
        }
    }
}
