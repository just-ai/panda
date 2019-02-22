package com.justai.cm.core.domain;

import com.justai.cm.core.actions.Encryptor;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class TemplateProps {
    Env env;
    Cmp cmp;
    Component component;
    Host host;

    HashMap<String, Object> props;
    HashMap<String, Cmp> components;
    Map<String, String> localProps;

    public String encrypt(String text) {
        return Encryptor.encrypt(text);
    }

    public String encryptWithPassword(String text, String password) {
        return Encryptor.encryptWithPassword(text, password);
    }

    public String encryptWithKey(String text, String key) {
        return Encryptor.encryptWithKeyFile(text, key);
    }

    public String encryptWithEncryptedKey(String text, String key) {
        return Encryptor.encryptWithEncryptedKeyFile(text, key);
    }

    public String decrypt(String text) {
        return Encryptor.decrypt(text);
    }
}
