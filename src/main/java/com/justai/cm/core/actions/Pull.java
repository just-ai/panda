package com.justai.cm.core.actions;

import com.justai.cm.core.domain.Cmp;
import com.justai.cm.core.domain.ConfigMap;
import com.justai.cm.core.domain.TemplateProps;
import com.justai.cm.utils.FileHelper;

public class Pull extends BaseAction {

    @Override
    protected void exec0(Cmp cmp) {
        Render render = actions.get(Render.class);

        // cleanup folder
        FileHelper rfl = cmp.getZPullFolder();
        rfl.clean();

        TemplateProps props = render.collectProps(cmp);
        // copy files
        sshManager.doWithConnection(cmp.getZHost(), ssh -> {
            for (ConfigMap map : cmp.getZComponent().getConfigMap()) {
                String target = render.renderTemplate(map.getTarget(), props);
                String source = map.getSource();
                if (!map.isDirectory()) {
                    ssh.pullFile(new FileHelper(target), rfl.child(source));
                } else {
                    ssh.pullDirectory(new FileHelper(target), rfl.child(source));
                }
            }
        });
    }

}
