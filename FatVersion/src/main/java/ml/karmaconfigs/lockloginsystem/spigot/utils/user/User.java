package ml.karmaconfigs.lockloginsystem.spigot.utils.user;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.api.spigot.Console;
import ml.karmaconfigs.api.spigot.KarmaFile;
import ml.karmaconfigs.api.spigot.reflections.TitleMessage;
import ml.karmaconfigs.lockloginsystem.shared.CheckType;
import ml.karmaconfigs.lockloginsystem.shared.ipstorage.BFSystem;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.PasswordUtils;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.Passwords;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Utils;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.api.events.PlayerVerifyEvent;
import ml.karmaconfigs.lockloginsystem.spigot.utils.StringUtils;
import ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles.LastLocation;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import ml.karmaconfigs.lockloginsystem.spigot.utils.inventory.PinInventory;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

/*
GNU LESSER GENERAL PUBLIC LICENSE
                       Version 2.1, February 1999

 Copyright (C) 1991, 1999 Free Software Foundation, Inc.
 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 Everyone is permitted to copy and distribute verbatim copies
 of this license document, but changing it is not allowed.

[This is the first released version of the Lesser GPL.  It also counts
 as the successor of the GNU Library Public License, version 2, hence
 the version number 2.1.]
 */

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
                    Console.send(plugin, "Detected old player ( {0} ) data file, converting it...", Level.INFO, player.getUniqueId());
                    playerFile.startConversion();
                } else {
                    playerFile.setupFile();
                }

                if (!playerFile.getName().equals(player.getName()))
                    playerFile.setName(player.getName());
            } else {
                Utils sql = new Utils(player);

                sql.createUser();

                String name = sql.getName();
                if (name != null && !name.equals(player.getName()))
                    sql.setName(player.getName());
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
     * Send a list of messages to the player
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
     * Send a title to the players
     *
     * @param title    the title
     * @param subtitle the subtitle
     * @param fadeIn   time to show title to screen
     * @param fadeOut  time to hide title from screen
     * @param show     time to show title in screen
     */
    public final void sendTitle(String title, String subtitle, int fadeIn, int show, int fadeOut) {
        if (!title.isEmpty() || !subtitle.isEmpty()) {
            TitleMessage title_message = new TitleMessage(player, title, subtitle);
            try {
                title_message.send(fadeIn, show, fadeOut);
            } catch (Throwable e) {
                logger.scheduleLog(Level.GRAVE, e);
                logger.scheduleLog(Level.INFO, "Error while sending title to " + player.getName());
            }
        }
    }

    /**
     * Teleport the player to the location
     *
     * @param location the location
     */
    public final void Teleport(Location location) {
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> player.teleport(location));
    }

    /**
     * Kick the player
     *
     * @param reason the kick reason
     */
    public final void Kick(String reason) {
        player.kickPlayer(StringUtils.toColor(reason));
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
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            PotionEffect effect = new PotionEffect(type, 20 * duration, amp, isSource, hasParticles);

            player.addPotionEffect(effect);
        });
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
     * Auth the player and perform his
     * login
     *
     * @param password the used password
     */
    public final void authPlayer(String password) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            PasswordUtils utils = new PasswordUtils(password, getPassword());

            BFSystem bf_prevention = new BFSystem(player.getAddress().getAddress());
            if (utils.PasswordIsOk()) {
                bf_prevention.success();
                if (!Passwords.isSecure(password, player)) {
                    setLogStatus(false);
                    Message(messages.Prefix() + messages.PasswordInsecure());

                    File player_data = new File(plugin.getDataFolder(), "playerdata");
                    File data = new File(player_data, player.getUniqueId().toString().replace("-", "") + ".yml");
                    YamlConfiguration data_file = YamlConfiguration.loadConfiguration(data);
                    data_file.set("Password", null);

                    try {
                        data_file.save(data);
                    } catch (Throwable e) {
                        logger.scheduleLog(Level.GRAVE, e);
                        logger.scheduleLog(Level.INFO, "Couldn't remove player " + player.getName() + " account password");
                    }

                    setLogStatus(true);
                    if (config.LoginBlind()) {
                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> removeBlindEffect(config.LoginNausea()));
                    }

                    new StartCheck(player, CheckType.REGISTER);
                } else {
                    PlayerVerifyEvent event = new PlayerVerifyEvent(player);
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> plugin.getServer().getPluginManager().callEvent(event));

                    if (!event.isCancelled()) {
                        setLogStatus(true);

                        sendTitle("", "", 1, 2, 1);

                        LastLocation lastLoc = new LastLocation(player);
                        if (config.TakeBack()) {
                            Teleport(lastLoc.getLastLocation());
                        }
                        if (config.LoginBlind()) {
                            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> removeBlindEffect(config.LoginNausea()));
                        }

                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> player.setAllowFlight(hasFly()));

                        if (hasPin()) {
                            PinInventory inventory = new PinInventory(player);
                            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, inventory::open);

                            setTempLog(true);
                        } else {
                            if (has2FA()) {
                                Message(messages.GAuthInstructions());
                                setTempLog(true);
                            } else {
                                Message(messages.Prefix() + event.getLoginMessage());
                            }
                        }

                        if (Passwords.isLegacySalt(getPassword())) {
                            setPassword(password);
                            Message(messages.Prefix() + "&cYour account password was using legacy encryption and has been updated");
                        }
                    } else {
                        Message(messages.Prefix() + event.getCancelMessage());
                    }
                }
            } else {
                if (bf_prevention.getTries() >= config.BFMaxTries() && config.BFMaxTries() > 0) {
                    bf_prevention.block();
                    bf_prevention.updateTime(config.BFBlockTime());

                    Timer unban = new Timer();
                    unban.schedule(new TimerTask() {
                        final BFSystem saved_system = bf_prevention;
                        int back = config.BFBlockTime();

                        @Override
                        public void run() {
                            if (back == 0) {
                                saved_system.unlock();
                                cancel();
                            }
                            saved_system.updateTime(back);
                            back--;
                        }
                    }, 0, TimeUnit.SECONDS.toMillis(1));

                    plugin.getServer().getScheduler().runTask(plugin, () -> Kick("&eLockLogin\n\n" + messages.ipBlocked(bf_prevention.getBlockLeft())));
                }
                if (!hasTries()) {
                    delTries();
                    bf_prevention.fail();
                    plugin.getServer().getScheduler().runTask(plugin, () -> Kick("&eLockLogin\n\n" + messages.LogError()));
                    return;
                }
                restTries();
                Message(messages.Prefix() + messages.LogError());
            }
        });
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
     *
     * @param nausea apply nausea effect?
     */
    public final void applyBlindEffect(boolean nausea) {
        removeEffect(PotionEffectType.BLINDNESS);
        removeEffect(PotionEffectType.NIGHT_VISION);
        if (nausea) {
            removeEffect(PotionEffectType.CONFUSION);
            sendEffect(PotionEffectType.CONFUSION, 10000, 100, true, false);
        }
        sendEffect(PotionEffectType.BLINDNESS, 10000, 100, true, false);
        sendEffect(PotionEffectType.NIGHT_VISION, 10000, 100, true, false);
    }

    /**
     * Remove the user blind effects
     *
     * @param nausea remove nausea effect?
     */
    public final void removeBlindEffect(boolean nausea) {
        removeEffect(PotionEffectType.BLINDNESS);
        removeEffect(PotionEffectType.NIGHT_VISION);
        if (nausea) {
            removeEffect(PotionEffectType.CONFUSION);
        }
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
                        applyBlindEffect(config.RegisterNausea());
                    }
                }, 5);
                break;
            case LOGIN:
                Message(messages.Prefix() + messages.Login());
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (config.LoginBlind()) {
                        saveCurrentEffects();
                        applyBlindEffect(config.LoginNausea());
                    }
                }, 5);
                break;
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
     * Get the player UUID
     * <code>No longer used</code>
     *
     * @return the player UUID
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
     * @return if the player is registered
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
     * @return if the player has a pin
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
     * @return if the player has 2fa in his account
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
     * @return if the player has fly
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
     * @return if the player is logged
     */
    public final boolean isLogged() {
        return logStatus.getOrDefault(player, false).equals(true);
    }

    /**
     * The same as "isLogged" but for
     * different purpose
     *
     * @return if the player is logged
     */
    public final boolean getLogStatus() {
        return logStatus.getOrDefault(player, false);
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
     * Check if the player has tries left
     *
     * @return if the player has login tries left
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
     * @return if the player is in temp log status
     * For example, if he has 2Fa or pin
     */
    public final boolean isTempLog() {
        return tempLog.getOrDefault(player, false);
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
     * Check if the code is ok
     *
     * @param code the code
     * @return if the specified code is valid
     */
    public final boolean validateCode(int code) {
        GoogleAuthenticator gauth = new GoogleAuthenticator();

        return gauth.authorize(getToken(true), code);
    }

    /**
     * Get the player password
     *
     * @return the player password
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
     * Get the player pin
     *
     * @return the player pin
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
     * Get the player token
     *
     * @param unHashed get the token with hash?
     * @return the player google auth token
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
     * @return the player google auth token if set, if not, a new google auth token
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
     * @return a new google auth token
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
     * @return the amount of login tries left
     * of the player
     */
    public final int getTriesLeft() {
        return playerTries.getOrDefault(player, config.GetMaxTries());
    }
}

/*
GNU LESSER GENERAL PUBLIC LICENSE
                       Version 2.1, February 1999

 Copyright (C) 1991, 1999 Free Software Foundation, Inc.
 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 Everyone is permitted to copy and distribute verbatim copies
 of this license document, but changing it is not allowed.

[This is the first released version of the Lesser GPL.  It also counts
 as the successor of the GNU Library Public License, version 2, hence
 the version number 2.1.]
 */

class FlyData implements LockLoginSpigot {

    private final static KarmaFile data = new KarmaFile(plugin, "fly.userdb", "data");
    private final Player player;

    protected FlyData(Player player) {
        this.player = player;

        createFlyData();
    }

    /**
     * Create fly data file
     */
    private void createFlyData() {
        if (!data.exists()) {
            data.create();
        }
    }

    /**
     * Save the player fly data
     */
    protected final void write() {
        String name = plugin.getServer().getOfflinePlayer(player.getUniqueId()).getName();
        data.set(name + ";");
    }

    /**
     * Remove the user from the fly data
     */
    protected final void remove() {
        String name = plugin.getServer().getOfflinePlayer(player.getUniqueId()).getName();
        data.unset(name + ";");
    }

    /**
     * Check if the data contains the player
     *
     * @return if the data contains the player
     */
    protected final boolean contains() {
        String name = plugin.getServer().getOfflinePlayer(player.getUniqueId()).getName();

        String[] data_info = data.toString().replaceAll("\\s", "").split(";");
        List<String> data_users = Arrays.asList(data_info);

        return data_users.contains(name);
    }
}