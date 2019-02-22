package com.justai.cm.core.actions;

import com.justai.cm.core.domain.Cmp;
import com.justai.cm.core.domain.Commands;

public class Reload extends BaseAction {

    @Override
    protected void exec0(Cmp cmp) {
        if (!settings.noPush) {
            actions.get("push").exec0(cmp);
        }

        // execute deploy command
        executeRemoteCommand(cmp, "reload");
    }

}
