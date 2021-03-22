package ml.karmaconfigs.lockloginsystem.shared;

import ml.karmaconfigs.lockloginsystem.shared.version.VersionChannel;
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
                yml.close();
                return values.getOrDefault("project_version", "1.0.0").toString();
            }
            jar.close();

            return "-1";
        } catch (Throwable ex) {
            return "-1";
        }
    }

    static boolean apiDebug(final File file) {
        try {
            JarFile jar = new JarFile(file);
            JarEntry jar_info = jar.getJarEntry("global.yml");

            if (jar_info != null) {
                InputStream yml = jar.getInputStream(jar_info);

                Yaml yaml = new Yaml();
                Map<String, Object> values = yaml.load(yml);
                yml.close();
                return Boolean.parseBoolean(values.getOrDefault("project_debug", false).toString());
            }
            jar.close();

            return false;
        } catch (Throwable ex) {
            return false;
        }
    }

    static VersionChannel getChannel(final File file) {
        try {
            JarFile jar = new JarFile(file);
            JarEntry jar_info = jar.getJarEntry("global.yml");

            if (jar_info != null) {
                InputStream yml = jar.getInputStream(jar_info);

                Yaml yaml = new Yaml();
                Map<String, Object> values = yaml.load(yml);
                yml.close();

                String value = values.getOrDefault("project_build", "RELEASE").toString();
                return VersionChannel.valueOf(value.toUpperCase());
            }
            jar.close();

            return VersionChannel.RELEASE;
        } catch (Throwable ex) {
            return VersionChannel.RELEASE;
        }
    }
}
