package com.justai.cm.core.actions;

import com.justai.cm.core.domain.Cmp;
import com.justai.cm.core.domain.ConfigMap;
import com.justai.cm.core.domain.TemplateProps;
import com.justai.cm.utils.FileHelper;
import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.justai.cm.utils.ExceptionUtils.wrap;

public class Render extends BaseAction {

    @Override
    public void exec0(Cmp cmp) {
        log("render component: %s", cmp.getFullId());
        // collect template properties
        TemplateProps props = collectProps(cmp);

        // copy and apply templates for folder component.getFolder();
        FileHelper fl = cmp.getZRenderFolder();
        if (fl == null) {
            System.out.println();
        }
        fl.mkdir();
        fl.clean();
        fl.child("config").mkdir().clean();
        fl.child("scripts").mkdir().clean();

        // render override configs
        if (cmp.getZConfigFolder() != null) {
            // наверное стоит хранить перегруженные конфиги в папке config, что бы их можно было удобно копировать после pull'а
            // а пока поддержим оба варианта
            FileHelper overrideFolder = cmp.getZConfigFolder();
            if (cmp.getZConfigFolder().child("config").file.isDirectory()) {
                overrideFolder = cmp.getZConfigFolder().child("config");
            }

            for (String f : overrideFolder.list()) {
                FileHelper source = overrideFolder.child(f);
                FileHelper target = fl.child("config").child(f);

                renderFile(source, props, target);
            }
        }

        FileHelper componentFolder = cmp.getZComponent().getConfigFolder();
        for (ConfigMap map : cmp.getZComponent().getConfigMap()) {
            FileHelper source = componentFolder.child(map.getSource());
            FileHelper target = fl.child("config").child(map.getSource());

            if (map.isOptional() && !source.file.exists()) {
                continue;
            }
            // skip, if already wrote this file from override folder
            if (!target.file.exists()) {
                renderFile(source, props, target);
            }
        }

        renderConfigMap(fl.child("config"), cmp.getZComponent().getConfigMap(), props);

        FileHelper scriptsFolder = cmp.getZComponent().getScriptsFolder();
        for (String f : scriptsFolder.list()) {
            FileHelper source = scriptsFolder.child(f);
            FileHelper target = fl.child("scripts").child(f);

            renderFile(source, props, target);
        }

    }

    TemplateProps collectProps(Cmp cmp) {
        TemplateProps tp = new TemplateProps();
        tp.setEnv(cmp.getZEnv());
        tp.setCmp(cmp);
        tp.setComponent(cmp.getZComponent());
        tp.setHost(cmp.getZHost());
        tp.setLocalProps(settings.props);

        HashMap<String, Object> props = new HashMap<>();
        props.putAll(cmp.getZComponent().getProps());
        props.putAll(cmp.getZEnv().getProps());
        if (cmp.getZHost() != null) {
            props.putAll(cmp.getZHost().getProps());
        }
        props.putAll(cmp.getProps());

        Encryptor.decryptProps(props);

        tp.setProps(props);

        tp.setComponents(cmp.getZEnv().getZComponents());

        return tp;
    }

    void renderFile(FileHelper source, TemplateProps props, FileHelper target)  {
        if (source.name().endsWith(".jks") || source.name().endsWith(".p12") || source.name().endsWith(".zip")) {
            copyFile(source, target);
        } else {
            renderFile0(source, props, target);
        }
    }

    private void renderFile0(FileHelper source, TemplateProps props, FileHelper target)  {
        log("render file: %s -> %s", source, target);

        if (source.file.isDirectory()) {
            target.mkdir();
            for (String s : source.list()) {
                renderFile(source.child(s), props, target.child(s));
            }
            return;
        }

        String src = source.read();
        String body = renderTemplate(source.toString(), src, props, source.file);
        body = body.replaceAll("\r\n", "\n");

        target.write(body);
    }

    void copyFile(FileHelper source, FileHelper target)  {
        log("copy file: %s -> %s", source, target);
        wrap(() -> FileUtils.copyFile(source.file, target.file));
    }

    String renderTemplate(String source, TemplateProps props)  {
        return renderTemplate("string", source, props, null);
    }

    String renderTemplate(String name, String source, TemplateProps props, File rootFile)  {
        try {
            Configuration cfg = new Configuration(new Version(2, 3, 28));
            cfg.setNumberFormat("computer");
//            cfg.setInterpolationSyntax();

            // Some other recommended settings:
            cfg.setDefaultEncoding("UTF-8");
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            cfg.setClassicCompatible(true);

            if (rootFile != null) {
                if (rootFile.isDirectory()) {
                    cfg.setDirectoryForTemplateLoading(rootFile);
                } else {
                    cfg.setDirectoryForTemplateLoading(rootFile.getParentFile());
                }
            }

            Template template = new Template(name, source, cfg);
            StringWriter sw = new StringWriter();
            template.process(props, sw);

            String body = sw.toString();
            return body;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void renderConfigMap(FileHelper configFolder, List<ConfigMap> configMap, TemplateProps props) {
        if (configMap.isEmpty()) {
            return;
        }
        FileHelper cmFile = configFolder.child("config-map.sh");
        cmFile.write(pw -> {
            pw.printf("#!/bin/bash\n");


            List<String> users = configMap.stream().map(s -> s.getUser()).filter( s -> s != null).distinct().collect(Collectors.toList());
            for (String user : users) {
                pw.printf("\n# check user %s exists\n", user);

                pw.printf("if id -u %s >/dev/null 2>&1; then\n" +
                        "   :\n" +
                        "else\n" +
                        "    useradd %s -r -s /sbin/nologin\n" +
                        "fi\n", user, user, user);
            }

            for (ConfigMap map : configMap) {
                pw.printf("\n# %s\n", map.getSource());

                String target = renderTemplate(map.getTarget(), props);

                if (!configFolder.child(map.getSource()).file.exists()) {
                    if (map.isOptional()) {
                        if (!map.isGeneratedByApp()) {
                            pw.printf("rm -f %s\n", target);
                        }
                        continue;
                    } else {
                        throw new RuntimeException("Required file does not exists");
                    }
                }

                if (!map.isDirectory()) {
                    if (map.isMkdirs()) {
                        pw.printf("mkdir -p `dirname %s`\n", target);
                    }
                    pw.printf("cp %s %s\n", map.getSource(), target);
                    if (StringUtils.isNotEmpty(map.getUser())) {
                        pw.printf("chown %s %s\n", map.getUser(), target);
                    }
                    if (StringUtils.isNotEmpty(map.getMode())) {
                        pw.printf("chmod %s %s\n", map.getMode(), target);
                    }
                } else {
                    pw.printf("mkdir -p %s\n", target);
                    // this option is to copy hidden files
                    pw.printf("shopt -s dotglob\n");
                    pw.printf("cp -R %s/* %s\n", map.getSource(), target);
                    pw.printf("shopt -u dotglob\n");

                    if (StringUtils.isNotEmpty(map.getUser())) {
                        pw.printf("chown -R %s %s\n", map.getUser(), target);
                    }
                    if (StringUtils.isNotEmpty(map.getMode())) {
                        pw.printf("chmod %s %s\n", map.getMode(), target);
                    }
                }
            }
        });
    }

}
