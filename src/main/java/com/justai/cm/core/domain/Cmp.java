package com.justai.cm.core.domain;

import com.justai.cm.core.Components;
import com.justai.cm.core.actions.Encryptor;
import com.justai.cm.utils.FileHelper;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class Cmp {
    String component;
    String host;
    String config;
    String id;
    boolean dontRenderConfigs;
    Map<String, Object> props = new HashMap<>();
    List<String> dependsOn = new ArrayList<>();

    transient Component zComponent;
    transient Env zEnv;
    transient Host zHost;
    transient List<Host> zHosts;
    transient FileHelper zConfigFolder;
    transient FileHelper zRenderFolder;
    transient FileHelper zPullFolder;
    transient HashMap<String, Object> zProps = new HashMap<>();

    public void build(Components components, Env env, FileHelper renderFolder, FileHelper pullFolder) {
        zComponent = components.get(component);
        if (zComponent == null) {
            throw new RuntimeException("Component " + component + " is not found");
        }
        zHosts = getHostsList(env, host);
        if (zHosts.size() == 1) {
            zHost = zHosts.get(0);
            zRenderFolder = renderFolder.child(zHost.name).child(id);
            zPullFolder = pullFolder.child(zHost.name).child(id);
        }
        zEnv = env;
        if (StringUtils.isNotEmpty(config)) {
            zConfigFolder = env.getZFolder().child(config);
        }
        Encryptor.decryptProps(props);

        zProps.putAll(zComponent.getProps());
        zProps.putAll(props);
    }

    private List<Host> getHostsList(Env env, String hostsPattern) {
        LinkedHashSet<String> result = new LinkedHashSet<>();

        Arrays.stream(hostsPattern.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .flatMap(h -> expandWildcardHosts(env, h))
                .forEach(h -> {
                    if (h.startsWith("!")) {
                        result.remove(h.substring(1));
                    }
                    else {
                        result.add(h);
                    }
                });

        return result.stream()
                .map(h -> {
                    if (env.zHosts.get(h) == null) {
                        throw new RuntimeException("Host " + h + " is not found");
                    }
                    return env.zHosts.get(h);
                })
                .collect(Collectors.toList());
    }

    private Stream<String> expandWildcardHosts(Env env, String host) {
        if (host.endsWith("*")) {
            String prefix = host.substring(0, host.length() - 1);
            return env.hosts.stream()
                    .map(Host::getName)
                    .filter(eh -> eh.startsWith(prefix));
        }
        return Stream.of(host);
    }

    public String getFullId() {
        String name = zHost == null ? host : zHost.name;
        return String.format("%s/%s/%s", zEnv.getName(), name, id);
    }
}
