package ml.karmaconfigs.lockloginsystem.shared;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public interface FileInfo {

    static String getJarVersion(final File file) {
        try {
            JarFile jar = new JarFile(file);
            JarEntry jar_info = jar.getJarEntry("global.yml");

            if (jar_info != null) {
                InputStream yml = jar.getInputStream(jar_info);

                Yaml yaml = new Yaml();
                Map<String, Object> values = yaml.load(yml);
                return values.getOrDefault("version", "1.0.0").toString();
            }

            return "";
        } catch (Throwable ex) {
            return "";
        }
    }
}
