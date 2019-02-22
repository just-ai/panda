package com.justai.cm.core.actions;

import com.justai.cm.core.domain.Cmp;

public class Start extends BaseAction {

    @Override
    protected void exec0(Cmp cmp) {
        if (!settings.noPush) {
            actions.get("push").exec0(cmp);
        }
        executeRemoteCommand(cmp, "start");
    }

}
