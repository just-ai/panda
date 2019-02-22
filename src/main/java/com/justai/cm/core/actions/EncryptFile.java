package com.justai.cm.core.actions;

import com.justai.cm.Settings;
import com.justai.cm.core.domain.Cmp;
import com.justai.cm.utils.FileUtils;
import org.apache.commons.cli.CommandLine;

import java.util.List;

public class EncryptFile extends BaseAction {

    @Override
    public void exec(Settings settings, CommandLine commandLine) {
        String sourceFile = commandLine.getArgList().get(1);
        String targetFile = commandLine.getArgList().get(2);

        System.out.printf("Encrypt file %s to %s\n", sourceFile, targetFile);

        String body = FileUtils.readFile(sourceFile);
        String encryptedBody = Encryptor.encrypt(body);
        FileUtils.write(targetFile, pw -> pw.print(encryptedBody));
    }

    @Override
    protected void exec0(Cmp cmp) {
        throw new IllegalStateException();
    }

}
