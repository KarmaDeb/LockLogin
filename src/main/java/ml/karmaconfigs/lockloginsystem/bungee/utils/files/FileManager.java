package ml.karmaconfigs.lockloginsystem.bungee.utils.files;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.bungee.karmayaml.YamlReloader;
import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.lockloginsystem.bungee.LockLoginBungee;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.nio.file.Files;
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
 */
@SuppressWarnings("unused")
public final class FileManager implements LockLoginBungee {

    private final File managed;
    private String internal_name = null;
    private Configuration file;

    /**
     * Starts the file manager
     *
     * @param fileName the file name
     */
    public FileManager(String fileName) {
        this.managed = new File(plugin.getDataFolder(), fileName);
        try {
            if (!managed.getParentFile().exists()) {
                Files.createDirectory(managed.getParentFile().toPath());
            }
            if (!managed.exists()) {
                Files.createFile(managed.toPath());
            }
            this.file = YamlConfiguration.getProvider(YamlConfiguration.class).load(managed);
        } catch (Throwable e) {
            logger.scheduleLog(Level.GRAVE, "Error while creating/saving file " + managed.getName());
        }
    }

    /**
     * Starts the file manager
     *
     * @param fileName the file name
     * @param fileDir  the file directory
     */
    public FileManager(String fileName, String fileDir) {
        this.managed = new File(plugin.getDataFolder() + "/" + fileDir, fileName);
        try {
            if (!managed.exists()) {
                File dir = new File(plugin.getDataFolder(), fileDir);
                if (!dir.exists()) {
                    Files.createDirectory(dir.toPath());
                }
                Files.createFile(managed.toPath());
            }
            this.file = YamlConfiguration.getProvider(YamlConfiguration.class).load(managed);
        } catch (Throwable e) {
            logger.scheduleLog(Level.GRAVE, "Error while creating/saving file " + managed.getName());
        }
    }

    /**
     * Gets the managed file
     *
     * @return the managed file
     */
    public final File getManaged() {
        return managed;
    }

    /**
     * Gets the managed file configuration
     *
     * @return YamlConfiguration format file configuration
     */
    public final Configuration getFile() {
        return file;
    }

    /**
     * Set the internal file name
     *
     * @param name the internal file name
     */
    public final void setInternal(final String name) {
        internal_name = name;
    }

    /**
     * Set a path with no info
     *
     * @param path the path
     */
    public final void set(String path) {
        file.set(path, "");
        save();
    }

    /**
     * Set a path value as object
     *
     * @param path  the path
     * @param value the value
     */
    public final void set(String path, Object value) {
        file.set(path, value);
        save();
    }

    /**
     * Set a path value as object
     *
     * @param path  the path
     * @param value the value
     */
    public final void set(String path, String value) {
        file.set(path, value);
        save();
    }

    /**
     * Set a path value as a string list
     *
     * @param path  the path
     * @param value the value
     */
    public final void set(String path, List<String> value) {
        file.set(path, value);
        save();
    }

    /**
     * Set a path value as boolean
     *
     * @param path  the path
     * @param value the value
     */
    public final void set(String path, Boolean value) {
        file.set(path, value);
        save();
    }

    /**
     * Set a path value as integer
     *
     * @param path  the path
     * @param value the value
     */
    public final void set(String path, Integer value) {
        file.set(path, value);
        save();
    }

    /**
     * Set a path value as double
     *
     * @param path  the path
     * @param value the value
     */
    public final void set(String path, Double value) {
        file.set(path, value);
        save();
    }

    /**
     * Set a path value as float
     *
     * @param path  the path
     * @param value the value
     */
    public final void set(String path, Float value) {
        file.set(path, value);
        save();
    }

    /**
     * Removes a path
     *
     * @param path the path
     */
    public final void unset(String path) {
        file.set(path, null);
        save();
    }

    /**
     * Check if the path is
     * empty
     *
     * @param path the path
     * @return if the config value of the path is empty
     */
    public final boolean isEmpty(String path) {
        if (isSet(path)) {
            return get(path).toString().isEmpty();
        } else {
            return true;
        }
    }

    /**
     * Check if the path is
     * set
     *
     * @param path the path
     * @return if the config path is set
     */
    public final boolean isSet(String path) {
        return get(path) != null;
    }

    /**
     * Gets the value of a path
     *
     * @param path the path
     * @return the object of the specified file path
     */
    public final Object get(String path) {
        try {
            return file.get(path);
        } catch (Throwable ex) {
            return "";
        }
    }

    /**
     * Gets the value of a path
     *
     * @param path the path
     * @return the string of the specified file path
     */
    public final String getString(String path) {
        try {
            return file.getString(path, "");
        } catch (Throwable ex) {
            return "";
        }
    }

    /**
     * Gets the value of a path
     *
     * @param path the path
     * @return the list of the specified file path
     */
    public final List<String> getList(String path) {
        try {
            return file.getStringList(path);
        } catch (Throwable ex) {
            return new ArrayList<>();
        }
    }

    /**
     * Gets the value of a path
     *
     * @param path the path
     * @return the boolean of the specified file path
     */
    public final Boolean getBoolean(String path) {
        try {
            return file.getBoolean(path);
        } catch (Throwable ex) {
            return false;
        }
    }

    /**
     * Gets the value of a path
     *
     * @param path the path
     * @return the integer of the specified file path
     */
    public final int getInt(String path) {
        try {
            return file.getInt(path);
        } catch (Throwable ex) {
            return 0;
        }
    }

    /**
     * Gets the value of a path
     *
     * @param path the path
     * @return the double of the specified file path
     */
    public final double getDouble(String path) {
        try {
            return file.getDouble(path);
        } catch (Throwable ex) {
            return 0D;
        }
    }

    /**
     * Gets the value of a path
     *
     * @param path the path
     * @return the float of the specified file path
     */
    public final float getFloat(String path) {
        try {
            return (float) file.getDouble(path);
        } catch (Throwable ex) {
            return 0F;
        }
    }

    /**
     * Tries to delete the managed file
     */
    public final void delete() {
        if (managed.delete()) {
            Console.send(plugin, "The file {0} have been removed", Level.INFO, managed.getName());
        } else {
            Console.send(plugin, "The file {0} couldn't be removed", Level.WARNING, managed.getName());
        }
    }

    /**
     * Tries to save the managed file values
     */
    public final void save() {
        try {
            YamlConfiguration.getProvider(YamlConfiguration.class).save(file, managed);
            if (internal_name != null) {
                YamlReloader reloader = new YamlReloader(plugin, managed, internal_name);
                reloader.reloadAndCopy();
            }
        } catch (Throwable e) {
            logger.scheduleLog(Level.GRAVE, e);
            logger.scheduleLog(Level.INFO, "Error while saving file " + managed.getName());
        }
    }
}
