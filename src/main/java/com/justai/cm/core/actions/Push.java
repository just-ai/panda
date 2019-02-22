package com.justai.cm.core.actions;

import com.justai.cm.core.domain.Cmp;
import com.justai.cm.utils.FileHelper;

public class Push extends BaseAction {

    @Override
    protected void exec0(Cmp cmp) {
        // render configs
        if (!settings.noRender) {
            actions.get("render").exec0(cmp);
        }

        FileHelper renderFolder = cmp.getZRenderFolder();
        String remoteFolder = ".cm/" + cmp.getId();

        // copy config files
        sshManager.doWithConnection(cmp.getZHost(), ssh -> {
            ssh.exec("rm -Rf " + remoteFolder);
            ssh.exec("mkdir -p " + remoteFolder + "/config");
            for (String f : renderFolder.child("config").list()) {
                FileHelper source = renderFolder.child("config").child(f);
                String target = remoteFolder + "/config/" + f;
                if (source.file.isFile()) {
                    ssh.pushFile(source, new FileHelper(target));
                } else {
                    ssh.pushDirectory(source, new FileHelper(target));
                }
            }
        });

        // copy script files
        sshManager.doWithConnection(cmp.getZHost(), ssh -> {
            ssh.exec("mkdir -p " + remoteFolder + "/scripts");
            for (String f : renderFolder.child("scripts").list()) {
                String target = remoteFolder + "/scripts/" + f;

                ssh.pushFile(renderFolder.child("scripts").child(f), new FileHelper(target));
            }
        });

        // set permissions
        sshManager.doWithConnection(cmp.getZHost(), ssh -> {
            ssh.exec("chmod +x `find " + remoteFolder + " -name '*.sh'`");
        });

        // execute config map
        sshManager.doWithConnection(cmp.getZHost(), ssh -> {
            ssh.exec(String.format("cd %s/config; chmod +x ./config-map.sh; sudo ./config-map.sh", remoteFolder));
        });
    }

}
