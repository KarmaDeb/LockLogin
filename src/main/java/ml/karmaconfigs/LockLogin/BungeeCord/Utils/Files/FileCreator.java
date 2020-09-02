package ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files;

import ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee;
import ml.karmaconfigs.LockLogin.Logs.Logger;
import ml.karmaconfigs.LockLogin.Platform;
import ml.karmaconfigs.LockLogin.WarningLevel;
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
 *
 * The use of this code
 * without GSA team authorization
 * will be a violation of
 * terms of use determined
 * in <a href="https://karmaconfigs.ml/license/"> here </a>
 */
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
        if (!this.folder.exists()) {
            if (folder.mkdir()) {
                out.Alert("The folder " + folder.getName() + " didn't exist and one have been created", WarningLevel.WARNING);
            } else {
                out.Alert("The plugin tried to create the folder " + folder.getName() + " but an error occurred", WarningLevel.ERROR);
            }
        }
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    out.Alert("The plugin tried to create the folder " + file.getName() + " but an error occurred", WarningLevel.ERROR);
                } else {
                    out.Alert("The file " + file.getName() + " didn't exist and have been created", WarningLevel.WARNING);
                }
            } catch (Throwable e) {
                Logger.log(Platform.BUNGEE, "ERROR WHILE CREATING FILE " + file.getName(), e);
            }
        }
        try {
            this.config = YamlConfiguration.getProvider(YamlConfiguration.class).load(file);
        } catch (Exception e) {
            Logger.log(Platform.BUNGEE, "ERROR WHILE SETTING UP FILE " + file.getName(), e);
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
            Logger.log(Platform.BUNGEE, "ERROR WHILE SAVING FILE " + file.getName(), e);
        }
    }

    /**
     * Check if the file exists
     *
     * @return a boolean
     */
    public final boolean exists() {
        return file.exists();
    }

    /**
     * Reloads the file
     * <code>No longer used</code>
     */
    @Deprecated
    public final void reloadFile() {
        Logger.log(Platform.BUNGEE, "WARNING", "USAGE OF DEPRECATED METHOD FROM FileCreator.class (reloadFile())");
        try {
            out.Alert("reloadFile() method is deprecated, because its not longer used", WarningLevel.WARNING);
            this.config = YamlConfiguration.getProvider(YamlConfiguration.class).load(file);
        } catch (Throwable e) {
            Logger.log(Platform.BUNGEE, "ERROR WHILE RELOADING FILE " + file.getName(), e);
        }
    }
}
