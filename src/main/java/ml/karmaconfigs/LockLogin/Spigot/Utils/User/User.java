package ml.karmaconfigs.LockLogin.Spigot.Utils.User;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import ml.karmaconfigs.LockLogin.CheckType;
import ml.karmaconfigs.LockLogin.Logs.Logger;
import ml.karmaconfigs.LockLogin.MySQL.Utils;
import ml.karmaconfigs.LockLogin.Platform;
import ml.karmaconfigs.LockLogin.Security.PasswordUtils;
import ml.karmaconfigs.LockLogin.Spigot.API.Events.PlayerVerifyEvent;
import ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot;
import ml.karmaconfigs.LockLogin.Spigot.Utils.DataFiles.LastLocation;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.FileCreator;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.SpigotFiles;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Inventory.PinInventory;
import ml.karmaconfigs.LockLogin.Spigot.Utils.StringUtils;
import ml.karmaconfigs.LockLogin.WarningLevel;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public final class User implements LockLoginSpigot, SpigotFiles {

    private final static HashMap<Player, Boolean> logStatus = new HashMap<>();
    private final static HashMap<Player, Boolean> tempLog = new HashMap<>();
    private final static HashMap<Player, Integer> playerTries = new HashMap<>();
    private final static HashMap<Player, Collection<PotionEffect>> playerEffects = new HashMap<>();

    private final Player player;

    /**
     * Setup the user
     *
     * @param player the player
     */
    public User(Player player) {
        this.player = player;
    }

    /**
     * Setup the player file
     */
    public final void setupFile() {
        if (!config.isBungeeCord()) {
            if (config.isYaml()) {
                PlayerFile playerFile = new PlayerFile(player);

                if (playerFile.isOld()) {
                    out.Alert("Detected old player &f( &b" + player.getName() + " &f)&e data file, converting it...", WarningLevel.WARNING);
                    playerFile.startConversion();
                } else {
                    playerFile.setupFile();
                }
            } else {
                Utils sql = new Utils(player);

                sql.createUser();
            }
        }
    }

    /**
     * Send a message to the player
     *
     * @param text the message
     */
    public final void Message(String text) {
        player.sendMessage(StringUtils.toColor(text));
    }

    /**
     * Send a list of messages to the palyer
     *
     * @param messages the messages
     */
    public final void Message(List<String> messages) {
        for (String str : messages) {
            player.sendMessage(StringUtils.toColor(str));
        }
    }

    /**
     * Send a json message to the player
     *
     * @param JSonMessage the json message
     */
    public final void Message(TextComponent JSonMessage) {
        player.spigot().sendMessage(JSonMessage);
    }

    /**
     * Send a list of messages to the player
     *
     * @param messages the messages
     */
    public final void Message(HashSet<String> messages) {
        for (String str : messages) {
            Message(str);
        }
    }

    /**
     * Send a title to the player
     *
     * @param Title    the title
     * @param Subtitle the subtitle
     */
    public final void Title(String Title, String Subtitle) {
        TitleSender sender = new TitleSender(player, Title, Subtitle);
        sender.send();
    }

    /**
     * Teleport the player to the location
     *
     * @param location the location
     */
    public final void Teleport(Location location) {
        player.teleport(location);
    }

    /**
     * Kick the player
     *
     * @param reason the kick reason
     */
    public final void Kick(String reason) {
        player.kickPlayer(StringUtils.toColor("&eLockLogin\n\n" + reason));
    }

    /**
     * Add the potion effect to the player
     *
     * @param type         the potion effect type
     * @param duration     the potion effect duration
     * @param amp          the potion effect amplification
     * @param isSource     if the potion has some source
     * @param hasParticles if the potion should spawn particles
     */
    public final void sendEffect(PotionEffectType type, int duration, int amp, boolean isSource, boolean hasParticles) {
        PotionEffect effect = new PotionEffect(type, 20 * duration, amp, isSource, hasParticles);

        player.addPotionEffect(effect);
    }

    /**
     * Add the potion effects to the player
     *
     * @param effects the potion effects
     */
    public final void sendEffects(Collection<PotionEffect> effects) {
        for (PotionEffect effect : effects) {
            player.addPotionEffect(effect);
        }
    }

    /**
     * Remove the player potion effect type
     *
     * @param type the potion effect type
     */
    public final void removeEffect(PotionEffectType type) {
        player.removePotionEffect(type);
    }

    /**
     * Set the player session status
     *
     * @param value true/false
     */
    public final void setLogStatus(boolean value) {
        logStatus.put(player, value);
    }

    /**
     * Auth the player and perform his
     * login
     */
    public final void authPlayer(String password) {
        PasswordUtils utils = new PasswordUtils(password, getPassword());
        PlayerVerifyEvent event = new PlayerVerifyEvent(player);

        if (utils.PasswordIsOk()) {
            plugin.getServer().getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                setLogStatus(true);

                LastLocation lastLoc = new LastLocation(player);
                if (config.TakeBack()) {
                    Teleport(lastLoc.getLastLocation());
                }
                if (config.LoginBlind()) {
                    removeBlindEffect();
                }

                player.setAllowFlight(hasFly());

                if (hasPin()) {
                    PinInventory inventory = new PinInventory(player);
                    inventory.open();

                    setTempLog(true);
                } else {
                    if (has2FA()) {
                        Message(messages.GAuthInstructions());
                        setTempLog(true);
                    } else {
                        Message(messages.Prefix() + event.getLoginMessage());
                    }
                }
            } else {
                Message(messages.Prefix() + event.getCancelMessage());
            }
        } else {
            if (!hasTries()) {
                delTries();
                Kick("&eLockLogin\n\n" + messages.LogError());
                return;
            }
            restTries();
            Message(messages.Prefix() + messages.LogError());
        }
    }

    /**
     * Rest a trie for the player
     */
    public final void restTries() {
        playerTries.put(player, getTriesLeft() - 1);
    }

    /**
     * Remove the player from tries
     * left
     */
    public final void delTries() {
        playerTries.remove(player);
    }

    /**
     * Set the player 2fa status
     *
     * @param value true/false
     */
    public final void set2FA(boolean value) {
        if (config.isYaml()) {
            PlayerFile playerFile = new PlayerFile(player);

            playerFile.set2FA(value);
        } else {
            Utils sql = new Utils(player);

            sql.gAuthStatus(value);
        }
    }

    /**
     * Set the player 2fa token
     *
     * @param token the token
     */
    public final void setToken(String token) {
        if (config.isYaml()) {
            PlayerFile playerFile = new PlayerFile(player);

            playerFile.setToken(token);
        } else {
            Utils sql = new Utils(player);

            sql.setGAuth(token, true);
        }
    }

    /**
     * Set the player fly status
     *
     * @param value true/false
     */
    public final void setFly(boolean value) {
        if (!config.isBungeeCord()) {
            if (config.isYaml()) {
                PlayerFile playerFile = new PlayerFile(player);

                playerFile.setFly(value);
            } else {
                Utils sql = new Utils(player);

                sql.setFly(value);
            }
        } else {
            FlyData data = new FlyData(player);

            if (value) {
                if (!data.contains()) {
                    data.write();
                }
            } else {
                data.remove();
            }
        }
    }

    /**
     * Remove the player file
     * or if using mysql, player info
     */
    public final void remove() {
        if (config.isYaml()) {
            PlayerFile playerFile = new PlayerFile(player);

            playerFile.removeFile();
        } else {
            Utils sql = new Utils(player);

            sql.removeUser();
        }
    }

    /**
     * Save the player current effects
     */
    public final void saveCurrentEffects() {
        playerEffects.putIfAbsent(player, player.getActivePotionEffects());
    }

    /**
     * Apply the blind effect to the player
     */
    public final void applyBlindEffect() {
        removeEffect(PotionEffectType.BLINDNESS);
        removeEffect(PotionEffectType.NIGHT_VISION);
        removeEffect(PotionEffectType.CONFUSION);
        sendEffect(PotionEffectType.BLINDNESS, 10000, 100, true, false);
        sendEffect(PotionEffectType.NIGHT_VISION, 10000, 100, true, false);
        sendEffect(PotionEffectType.CONFUSION, 10000, 100, true, false);
    }

    /**
     * Remove the user blind effects
     */
    public final void removeBlindEffect() {
        removeEffect(PotionEffectType.BLINDNESS);
        removeEffect(PotionEffectType.NIGHT_VISION);
        removeEffect(PotionEffectType.CONFUSION);
        if (playerEffects.containsKey(player)) {
            sendEffects(playerEffects.get(player));
            playerEffects.remove(player);
        }
    }

    /**
     * Check the player status
     */
    public final void checkStatus() {
        CheckType checkType;
        if (!isRegistered()) {
            checkType = CheckType.REGISTER;
        } else {
            checkType = CheckType.LOGIN;
        }

        new StartCheck(player, checkType);
        switch (checkType) {
            case REGISTER:
                Message(messages.Prefix() + messages.Register());
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (config.RegisterBlind()) {
                        saveCurrentEffects();
                        applyBlindEffect();
                    }
                }, 5);
                break;
            case LOGIN:
                Message(messages.Prefix() + messages.Login());
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (config.LoginBlind()) {
                        saveCurrentEffects();
                        applyBlindEffect();
                    }
                }, 5);
                break;
        }
    }

    /**
     * Set the player password
     *
     * @param password the password
     */
    public final void setPassword(String password) {
        if (config.isYaml()) {
            PlayerFile playerFile = new PlayerFile(player);

            playerFile.setPassword(password);
        } else {
            Utils sql = new Utils(player);

            sql.setPassword(password, false);
        }
    }

    /**
     * Set the player pin
     *
     * @param pin the pin
     */
    public final void setPin(Object pin) {
        if (config.isYaml()) {
            PlayerFile playerFile = new PlayerFile(player);

            playerFile.setPin(pin);
        } else {
            Utils sql = new Utils(player);

            sql.setPin(pin, false);
        }
    }

    /**
     * Remove the user pin
     */
    public final void removePin() {
        if (config.isYaml()) {
            PlayerFile playerFile = new PlayerFile(player);

            playerFile.delPin();
        } else {
            Utils sql = new Utils(player);

            sql.delPin();
        }
    }

    /**
     * Set if the player is in temp-login
     * status
     *
     * @param value true/false
     */
    public final void setTempLog(boolean value) {
        tempLog.put(player, value);
    }

    /**
     * Get the player UUID
     * <code>No longer used</code>
     *
     * @return a UUID
     */
    @Deprecated
    public final UUID getUUID() {
        if (!config.isBungeeCord()) {
            if (config.isYaml()) {
                PlayerFile playerFile = new PlayerFile(player);

                return playerFile.getUUID();
            } else {
                Utils sql = new Utils(player);

                return sql.getUUID();
            }
        }
        return plugin.getServer().getOfflinePlayer(player.getUniqueId()).getUniqueId();
    }

    /**
     * Check if the player is registered
     *
     * @return a boolean
     */
    public final boolean isRegistered() {
        if (!config.isBungeeCord()) {
            if (config.isYaml()) {
                PlayerFile playerFile = new PlayerFile(player);

                return playerFile.getPassword() != null && !playerFile.getPassword().isEmpty();
            } else {
                Utils sql = new Utils(player);

                return sql.getPassword() != null && !sql.getPassword().isEmpty();
            }
        }
        return false;
    }

    /**
     * Check if the player has a pin set
     *
     * @return a boolean
     */
    public final boolean hasPin() {
        if (!config.isBungeeCord()) {
            if (config.EnablePins()) {
                if (config.isYaml()) {
                    PlayerFile playerFile = new PlayerFile(player);

                    return playerFile.getPin() != null && !playerFile.getPin().isEmpty();
                } else {
                    Utils sql = new Utils(player);

                    return sql.getPin() != null && !sql.getPin().isEmpty();
                }
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * Check if the player has 2fa enabled
     *
     * @return a boolean
     */
    public final boolean has2FA() {
        if (!config.isBungeeCord()) {
            if (config.Enable2FA()) {
                if (config.isYaml()) {
                    PlayerFile playerFile = new PlayerFile(player);

                    return playerFile.has2FA();
                } else {
                    Utils sql = new Utils(player);

                    return sql.has2fa();
                }
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * Check if the player has fly
     *
     * @return a boolean
     */
    public final boolean hasFly() {
        if (!config.isBungeeCord()) {
            if (config.isYaml()) {
                PlayerFile playerFile = new PlayerFile(player);

                return playerFile.hasFly();
            } else {
                Utils sql = new Utils(player);

                return sql.hasFly();
            }
        } else {
            FlyData data = new FlyData(player);

            return data.contains();
        }
    }

    /**
     * Check if the player is logged
     *
     * @return a boolean
     */
    public final boolean isLogged() {
        return logStatus.getOrDefault(player, false).equals(true);
    }

    /**
     * Get the user log status
     *
     * @return a booblean
     */
    public final boolean getLogStatus() {
        return logStatus.getOrDefault(player, false);
    }

    /**
     * Check if the player has tries left
     *
     * @return a boolean
     */
    public final boolean hasTries() {
        if (!config.isBungeeCord()) {
            if (playerTries.containsKey(player)) {
                return playerTries.get(player) != 0;
            } else {
                playerTries.put(player, config.GetMaxTries());
                return true;
            }
        }
        return true;
    }

    /**
     * Check if the player is in temp
     * login status
     *
     * @return a boolean
     */
    public final boolean isTempLog() {
        return tempLog.getOrDefault(player, false);
    }

    /**
     * Check if the code is ok
     *
     * @param code the code
     * @return a boolean
     */
    public final boolean validateCode(int code) {
        GoogleAuthenticator gauth = new GoogleAuthenticator();

        return gauth.authorize(getToken(true), code);
    }

    /**
     * Get the player password
     *
     * @return a String
     */
    public final String getPassword() {
        if (!config.isBungeeCord()) {
            if (config.isYaml()) {
                PlayerFile playerFile = new PlayerFile(player);

                return playerFile.getPassword();
            } else {
                Utils sql = new Utils(player);

                return sql.getPassword();
            }
        }
        return "";
    }

    /**
     * Get the player pin
     *
     * @return a String
     */
    public final String getPin() {
        if (!config.isBungeeCord()) {
            if (config.isYaml()) {
                PlayerFile playerFile = new PlayerFile(player);

                return playerFile.getPin();
            } else {
                Utils sql = new Utils(player);

                return sql.getPin();
            }
        }
        return "";
    }

    /**
     * Get the player token
     *
     * @return a String
     */
    public final String getToken(boolean unHashed) {
        if (!config.isBungeeCord()) {
            if (config.isYaml()) {
                PlayerFile playerFile = new PlayerFile(player);
                if (!unHashed) {
                    return playerFile.getToken();
                } else {
                    return new PasswordUtils(playerFile.getToken()).UnHash();
                }
            } else {
                Utils sql = new Utils(player);
                if (!unHashed) {
                    return sql.getToken();
                } else {
                    return new PasswordUtils(sql.getToken()).UnHash();
                }
            }
        }
        return "";
    }

    /**
     * Generate a google authenticator token
     * for the player
     *
     * @return a String
     */
    public final String genToken() {
        if (!config.isBungeeCord()) {
            if (getToken(true).isEmpty()) {
                GoogleAuthenticator gauth = new GoogleAuthenticator();
                GoogleAuthenticatorKey key = gauth.createCredentials();

                return key.getKey();
            } else {
                return getToken(true);
            }
        }
        return "";
    }

    /**
     * Generate a google authenticator token
     * for the player
     *
     * @return a String
     */
    public final String genNewToken() {
        if (!config.isBungeeCord()) {
            GoogleAuthenticator gauth = new GoogleAuthenticator();
            GoogleAuthenticatorKey key = gauth.createCredentials();

            return key.getKey();
        }
        return "";
    }

    /**
     * Get the player tries left
     *
     * @return an integer
     */
    public final int getTriesLeft() {
        return playerTries.getOrDefault(player, config.GetMaxTries());
    }
}

class FlyData implements LockLoginSpigot {

    private final Player player;

    private final File file = new File(plugin.getDataFolder() + "/data/", "fly.userdb");

    protected FlyData(Player player) {
        this.player = player;

        createFlyData();
    }

    /**
     * Create fly data file
     */
    private void createFlyData() {
        FileCreator creator = new FileCreator("fly.userdb", "data", false);

        if (!creator.exists()) {
            creator.createFile();
            creator.saveFile();
        }
    }

    /**
     * Save the player fly data
     */
    protected final void write() {
        String name = plugin.getServer().getOfflinePlayer(player.getUniqueId()).getName();

        try {
            InputStreamReader inReader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(inReader);

            List<String> lines = new ArrayList<>();

            String line;
            while ((line = reader.readLine()) != null) {
                if (!lines.contains(line)) {
                    lines.add(line);
                }
            }

            if (!lines.contains(name))
                lines.add(name + ";");

            FileWriter writer = new FileWriter(file);

            for (int i = 0; i < lines.size(); i++) {
                if (i != lines.size() - 1) {
                    writer.write(lines.get(i) + "\n");
                } else {
                    writer.write(lines.get(i));
                }
            }

            inReader.close();
            reader.close();
            writer.flush();
            writer.close();
        } catch (Throwable e) {
            Logger.log(Platform.ANY, "ERROR WHILE WRITING IN LockLogin FLY DATA", e);
        }
    }

    /**
     * Remove the user from the fly data
     */
    protected final void remove() {
        String name = plugin.getServer().getOfflinePlayer(player.getUniqueId()).getName();

        try {
            InputStreamReader inReader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(inReader);

            List<String> lines = new ArrayList<>();

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.replace(";", "").equals(name)) {
                    if (!lines.contains(line)) {
                        lines.add(line);
                    }
                }
            }

            FileWriter writer = new FileWriter(file);

            for (int i = 0; i < lines.size(); i++) {
                if (i != lines.size() - 1) {
                    writer.write(lines.get(i) + "\n");
                } else {
                    writer.write(lines.get(i));
                }
            }

            inReader.close();
            reader.close();
            writer.flush();
            writer.close();
        } catch (Throwable e) {
            Logger.log(Platform.ANY, "ERROR WHILE REMOVING IN LockLogin FLY DATA", e);
        }
    }

    /**
     * Check if the data contains the player
     *
     * @return a boolean
     */
    protected final boolean contains() {
        String name = plugin.getServer().getOfflinePlayer(player.getUniqueId()).getName();

        try {
            String toString = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            String[] finalData = toString.split(";");

            ArrayList<String> names = new ArrayList<>();
            for (String str : finalData) {
                if (!names.contains(str)) {
                    names.add(str);
                }
            }

            return names.contains(name);
        } catch (Throwable e) {
            Logger.log(Platform.ANY, "ERROR WHILE RETRIEVING LockLogin FLY DATA", e);
            return false;
        }
    }
}