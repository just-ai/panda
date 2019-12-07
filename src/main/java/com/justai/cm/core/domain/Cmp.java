package com.justai.cm.core.domain;

import com.justai.cm.core.Components;
import com.justai.cm.core.actions.Encryptor;
import com.justai.cm.utils.FileHelper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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

        renderProps(props, env);
        zProps.putAll(zComponent.getProps());
        zProps.putAll(props);
    }

    private void renderProps(Map<String, Object> props, Env env) {
        props.forEach((k,v) -> {
            if (v instanceof String) {
                String value = renderProps(env.getProps(), (String) v);
                props.replace(k, value);
            }
            if (v instanceof Map) {
                renderProps((Map<String, Object>) v, env);
            }
        });
    }

    private String renderProps(Map<String, Object> props, String prop)  {
        try {
            Configuration cfg = new Configuration(new Version(2, 3, 28));
            cfg.setNumberFormat("computer");
            cfg.setDefaultEncoding("UTF-8");
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

            Template template = new Template("test", prop, cfg);
            StringWriter sw = new StringWriter();
            template.process(props, sw);

            return sw.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    @Override
    public String toString() {
        return "Cmp{" +
                "component='" + component + '\'' +
                ", host='" + host + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
