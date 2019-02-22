package com.justai.cm.utils;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;

import static com.justai.cm.utils.ExceptionUtils.wrap;

public class YamlUtils {

    public static <T> T load(File file, Class<T> clazz) {
        String content = wrap(() -> FileUtils.readFile(file.toString()));
        return load(content, clazz);
    }

    public static <T> T load(String content, Class<T> clazz) {
        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);
        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setAllowDuplicateKeys(false);
        Yaml yaml = new Yaml(new Constructor(clazz), representer, new DumperOptions(), loaderOptions);
        return yaml.loadAs(content, clazz);
    }

}
