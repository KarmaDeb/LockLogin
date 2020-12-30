package ml.karmaconfigs.lockloginsystem.bungeecord.utils.files;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Private GSA code
 * <p>
 * The use of this code
 * without GSA team authorization
 * will be a violation of
 * terms of use determined
 * in <a href="https://karmaconfigs.ml/license/"> here </a>
 *
 * @deprecated Now using: KarmaAPI
 */
@Deprecated
public final class FileCreator implements LockLoginBungee {

    private final File folder;
    private final File file;
    private boolean isResource;
    private Configuration config;
    private Configuration cfg;

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
            if (theFile != null) {
                InputStreamReader DF = new InputStreamReader(theFile, StandardCharsets.UTF_8);
                this.cfg = YamlConfiguration.getProvider(YamlConfiguration.class).load(DF);
            } else {
                this.isResource = false;
            }
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
            if (theFile != null) {
                InputStreamReader DF = new InputStreamReader(theFile, StandardCharsets.UTF_8);
                this.cfg = YamlConfiguration.getProvider(YamlConfiguration.class).load(DF);
            } else {
                this.isResource = false;
            }
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
        InputStream theFile = (plugin).getClass().getResourceAsStream("/" + resourceFile);
        if (theFile != null) {
            InputStreamReader DF = new InputStreamReader(theFile, StandardCharsets.UTF_8);
            this.cfg = YamlConfiguration.getProvider(YamlConfiguration.class).load(DF);
            this.isResource = true;
        } else {
            this.isResource = false;
        }
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
        InputStream theFile = (plugin).getClass().getResourceAsStream("/" + resourceFile);
        if (theFile != null) {
            InputStreamReader DF = new InputStreamReader(theFile, StandardCharsets.UTF_8);
            this.cfg = YamlConfiguration.getProvider(YamlConfiguration.class).load(DF);
            this.isResource = true;
        } else {
            this.isResource = false;
        }

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
        try {
            this.config = YamlConfiguration.getProvider(YamlConfiguration.class).load(file);
        } catch (Exception e) {
            logger.scheduleLog(Level.GRAVE, e);
            logger.scheduleLog(Level.INFO, "Error while setting up file " + file.getName());
        }
    }

    /**
     * Set the defaults for the file
     * reading the internal file
     */
    public final void setDefaults() {
        if (isResource) {
            List<String> sections = new ArrayList<>();

            for (String path : config.getKeys()) {
                if (cfg.get(path) == null) {
                    config.set(path, null);
                }
            }

            for (String path : cfg.getKeys()) {
                if (config.get(path) == null) {
                    config.set(path, cfg.get(path));
                } else {
                    if (cfg.get(path) instanceof Boolean) {
                        if (!(config.get(path) instanceof Boolean)) {
                            config.set(path, cfg.get(path));
                        }
                    } else {
                        if (cfg.get(path) instanceof Integer) {
                            if (!(config.get(path) instanceof Integer)) {
                                config.set(path, cfg.get(path));
                            }
                        } else {
                            if (cfg.get(path) instanceof String) {
                                if (!(config.get(path) instanceof String)) {
                                    config.set(path, cfg.get(path));
                                }
                            } else {
                                if (cfg.get(path) instanceof List) {
                                    if (!(config.get(path) instanceof List)) {
                                        config.set(path, cfg.get(path));
                                    }
                                } else {
                                    if (cfg.get(path) instanceof Configuration) {
                                        sections.add(path);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (!sections.isEmpty()) {
                for (String section : sections) {
                    for (String str : cfg.getSection(section).getKeys()) {
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
                        if (config.get(path) instanceof Configuration) {
                            sections.add(path);
                        }
                    }
                }
                for (String section : sections) {
                    for (String str : config.getSection(section).getKeys()) {
                        String path = section + "." + str;
                        if (cfg.get(path) == null) {
                            config.set(path, null);
                        }
                    }
                }
            }
        }
    }

    /**
     * Save the file
     */
    public final void saveFile() {
        try {
            YamlConfiguration.getProvider(YamlConfiguration.class).save(config, file);
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
}
