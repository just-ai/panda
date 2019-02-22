package com.justai.cm.core.domain;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.util.HashMap;

import static com.justai.cm.utils.ExceptionUtils.wrap;

@Data
public class Host {
    String name;
    String fqdn;
    String sshPort = "22";
    String ip;

    HashMap<String, Object> props = new HashMap<>();

    public String getIp() {
        if (StringUtils.isNotEmpty(ip)) {
            return ip;
        }
        return wrap(() -> InetAddress.getByName(fqdn).getHostAddress());
    }
}
