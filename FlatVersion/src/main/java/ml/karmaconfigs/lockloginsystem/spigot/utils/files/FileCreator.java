package ml.karmaconfigs.lockloginsystem.spigot.utils.files;

import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.api.spigot.Console;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Private GSA code
 *
 * The use of this code
 * without GSA team authorization
 * will be a violation of
 * terms of use determined
 * in <a href="https://karmaconfigs.ml/license/"> here </a>
 * @deprecated Now using: KarmaAPI
 */
@Deprecated
public final class FileCreator implements LockLoginSpigot {

    private final File folder;
    private final File file;
    private final boolean isResource;
    private YamlConfiguration config;
    private YamlConfiguration cfg;

    /**
     * Starts the file creator
     *
     * @param fileName   the file name
     * @param fileDir    the file dir
     * @param isResource if the file is inside the plugin itself
     */
    public FileCreator(String fileName, String fileDir, boolean isResource) {
        this.isResource = isResource;
        if (isResource) {
            InputStream theFile = (plugin).getClass().getResourceAsStream("/" + fileName);
            InputStreamReader DF = new InputStreamReader(theFile, StandardCharsets.UTF_8);
            this.cfg = YamlConfiguration.loadConfiguration(DF);
        }

        this.file = new File(plugin.getDataFolder() + File.separator + fileDir, fileName);
        this.folder = new File(plugin.getDataFolder() + File.separator + fileDir);
    }

    /**
     * Starts the file creator
     *
     * @param fileName   the file name
     * @param isResource if the file is inside the plugin itself
     */
    public FileCreator(String fileName, boolean isResource) {
        this.isResource = isResource;
        if (isResource) {
            InputStream theFile = (plugin).getClass().getResourceAsStream("/" + fileName);
            InputStreamReader DF = new InputStreamReader(theFile, StandardCharsets.UTF_8);
            this.cfg = YamlConfiguration.loadConfiguration(DF);
        }

        this.file = new File(plugin.getDataFolder(), fileName);
        this.folder = plugin.getDataFolder();
    }

    /**
     * Starts the file creator
     *
     * @param fileName     the file name
     * @param resourceFile if the resource file is custom
     */
    public FileCreator(String fileName, String resourceFile) {
        this.isResource = true;
        InputStream theFile = (plugin).getClass().getResourceAsStream("/" + resourceFile);
        InputStreamReader DF = new InputStreamReader(theFile, StandardCharsets.UTF_8);
        this.cfg = YamlConfiguration.loadConfiguration(DF);
        this.file = new File(plugin.getDataFolder(), fileName);
        this.folder = plugin.getDataFolder();
    }

    /**
     * Starts the file creator
     *
     * @param fileName     the file name
     * @param fileDir      the file dir
     * @param resourceFile if the resource file is custom
     */
    public FileCreator(String fileName, String fileDir, String resourceFile) {
        this.isResource = true;
        InputStream theFile = (plugin).getClass().getResourceAsStream("/" + resourceFile);
        InputStreamReader DF = new InputStreamReader(theFile, StandardCharsets.UTF_8);
        this.cfg = YamlConfiguration.loadConfiguration(DF);
        this.file = new File(plugin.getDataFolder() + "/" + fileDir, fileName);
        this.folder = new File(plugin.getDataFolder() + "/" + fileDir);
    }

    /**
     * Create the file and the folder
     * if not exists
     */
    public final void createFile() {
        if (!folder.exists()) {
            if (folder.mkdir()) {
                Console.send(plugin, "The folder {0} has been created", Level.INFO, folder.getName());
            } else {
                Console.send(plugin, "Tried to create folder {0}, but failed", Level.GRAVE, folder.getName());
            }
        }
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    Console.send(plugin, "Tried to create file {0}, but failed", Level.GRAVE, file.getName());
                } else {
                    Console.send(plugin, "The file {0} has been created", Level.INFO, file.getName());
                }
            } catch (Throwable e) {
                logger.scheduleLog(Level.GRAVE, e);
                logger.scheduleLog(Level.INFO, "Error while creating file " + file.getName());
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Set the defaults for the file
     * reading the internal file
     */
    public final void setDefaults() {
        if (isResource) {
            List<String> sections = new ArrayList<>();

            for (String path : config.getKeys(false)) {
                if (cfg.get(path) == null) {
                    config.set(path, null);
                }
            }

            for (String path : cfg.getKeys(false)) {
                if (config.get(path) == null) {
                    config.set(path, cfg.get(path));
                } else {
                    if (cfg.get(path) instanceof Boolean) {
                        if (!(config.get(path) instanceof Boolean)) {
                            config.set(path, cfg.get(path));
                        }
                    }
                    if (cfg.get(path) instanceof Integer) {
                        if (!(config.get(path) instanceof Integer)) {
                            config.set(path, cfg.get(path));
                        }
                    }
                    if (cfg.get(path) instanceof Double) {
                        if (!(config.get(path) instanceof Double)) {
                            config.set(path, cfg.get(path));
                        }
                    }
                    if (cfg.get(path) instanceof Long) {
                        if (!(config.get(path) instanceof Long)) {
                            config.set(path, cfg.get(path));
                        }
                    }
                    if (cfg.get(path) instanceof String) {
                        if (!(config.get(path) instanceof String)) {
                            config.set(path, cfg.get(path));
                        }
                    }
                    if (cfg.get(path) instanceof List) {
                        if (!(config.get(path) instanceof List)) {
                            config.set(path, cfg.get(path));
                        }
                    }
                }
                if (config.get(path) instanceof ConfigurationSection) {
                    sections.add(path);
                }
            }
            if (!sections.isEmpty()) {
                for (String section : sections) {
                    if (cfg.getConfigurationSection(section) != null) {
                        for (String str : Objects.requireNonNull(cfg.getConfigurationSection(section)).getKeys(false)) {
                            String path = section + "." + str;
                            if (config.get(path) == null) {
                                config.set(path, cfg.get(path));
                            } else {
                                if (cfg.get(path) instanceof Boolean) {
                                    if (!(config.get(path) instanceof Boolean)) {
                                        config.set(path, cfg.get(path));
                                    }
                                }
                                if (cfg.get(path) instanceof Integer) {
                                    if (!(config.get(path) instanceof Integer)) {
                                        config.set(path, cfg.get(path));
                                    }
                                }
                                if (cfg.get(path) instanceof Double) {
                                    if (!(config.get(path) instanceof Double)) {
                                        config.set(path, cfg.get(path));
                                    }
                                }
                                if (cfg.get(path) instanceof Long) {
                                    if (!(config.get(path) instanceof Long)) {
                                        config.set(path, cfg.get(path));
                                    }
                                }
                                if (cfg.get(path) instanceof String) {
                                    if (!(config.get(path) instanceof String)) {
                                        config.set(path, cfg.get(path));
                                    }
                                }
                                if (cfg.get(path) instanceof List) {
                                    if (!(config.get(path) instanceof List)) {
                                        config.set(path, cfg.get(path));
                                    }
                                }
                            }
                        }
                    }
                }
                for (String section : sections) {
                    for (String str : Objects.requireNonNull(config.getConfigurationSection(section)).getKeys(false)) {
                        String path = section + "." + str;
                        if (cfg.get(path) == null) {
                            config.set(path, null);
                        }
                    }
                }
            }
        }
        saveFile();
    }

    /**
     * Save the file
     */
    public final void saveFile() {
        try {
            config.save(file);
        } catch (Throwable e) {
            logger.scheduleLog(Level.GRAVE, e);
            logger.scheduleLog(Level.INFO, "Error while saving file " + file.getName());
        }
    }

    /**
     * Check if the file exists
     *
     * @return if the file exists
     */
    public final boolean exists() {
        return file.exists();
    }

    /**
     * Get the file
     *
     * @return the created file
     */
    public final File getFile() {
        return file;
    }
}
