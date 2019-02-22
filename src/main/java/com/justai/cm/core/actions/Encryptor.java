package com.justai.cm.core.actions;

import com.justai.cm.Settings;
import com.justai.cm.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.util.text.BasicTextEncryptor;

import java.util.HashMap;
import java.util.Map;

public class Encryptor {

    private static BasicTextEncryptor encryptor;

    public static void init(Settings settings) {
        encryptor = createEncryptor(settings);
    }

    public static String encrypt(String message) {
        return encryptor.encrypt(message);
    }

    public static String decrypt(String encryptedMessage) {
        return encryptor.decrypt(encryptedMessage);
    }

    private static BasicTextEncryptor createEncryptor(Settings settings) {
        String key = null;
        if (StringUtils.isNotEmpty(settings.master_pass)) {
            key = settings.master_pass;
        }
        if (StringUtils.isNotEmpty(settings.master_key)) {
            key = FileUtils.readFile(settings.master_key);
        }

        if (key == null) {
            System.err.println("No password supplied");
            System.exit(1);
        }

        BasicTextEncryptor encryptor = new BasicTextEncryptor();
        encryptor.setPassword(key);
        return encryptor;
    }

    public static String encryptWithPassword(String text, String password) {
        BasicTextEncryptor encryptor = new BasicTextEncryptor();
        encryptor.setPassword(password);
        return encryptor.encrypt(text);
    }

    public static String encryptWithKeyFile(String text, String key) {
        BasicTextEncryptor encryptor = new BasicTextEncryptor();
        encryptor.setPassword(FileUtils.readFile(key));
        return encryptor.encrypt(text);
    }

    public static String encryptWithEncryptedKeyFile(String text, String key) {
        BasicTextEncryptor encryptor2 = new BasicTextEncryptor();
        String pass = FileUtils.readFile(key);
        pass = encryptor.decrypt(pass);
        encryptor2.setPassword(pass);
        return encryptor2.encrypt(text);
    }

    public static void decryptProps(Map<String, Object> props) {
        for (Map.Entry<String, Object> e : props.entrySet()) {
            if (e.getValue() instanceof String) {
                String s = (String) e.getValue();
                if (s.startsWith("SECRET:")) {
                    e.setValue(
                            decrypt(s.substring("SECRET:".length())));
                }
            }
            if (e.getValue() instanceof Map) {
                decryptProps((Map<String, Object>) e.getValue());
            }
        }
    }
}
