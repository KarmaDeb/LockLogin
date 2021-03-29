package ml.karmaconfigs.lockloginsystem.spigot.utils.inventory;

import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.api.common.StringUtils;
import ml.karmaconfigs.lockloginsystem.shared.AuthType;
import ml.karmaconfigs.lockloginsystem.shared.EventAuthResult;
import ml.karmaconfigs.lockloginsystem.shared.Motd;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.PasswordUtils;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.api.events.PlayerAuthEvent;
import ml.karmaconfigs.lockloginsystem.spigot.utils.BungeeSender;
import ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles.LastLocation;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.User;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * Private GSA code
 * <p>
 * The use of this code
 * without GSA team authorization
 * will be a violation of
 * terms of use determined
 * in <a href="https://karmaconfigs.ml/license/"> here </a>
 */
public final class PinInventory implements LockLoginSpigot, SpigotFiles {

    private final static ArrayList<Player> verified = new ArrayList<>();
    private final static HashMap<Player, String> input = new HashMap<>();
    private static Player player;
    private final Inventory inventory;

    /**
     * Intiailize the pin inventory for
     * the player
     *
     * @param player the player
     */
    public PinInventory(Player player) {
        PinInventory.player = player;
        inventory = plugin.getServer().createInventory(null, 45, StringUtils.toColor("&eLockLogin pinner"));
    }

    /**
     * Clear the list of verified players
     */
    public static void clearVerifiedList() {
        verified.clear();
    }

    /**
     * Initialize the inventory items
     */
    private void makeInventory() {
        inventory.setItem(12, Numbers.getOne());
        inventory.setItem(13, Numbers.getTwo());
        inventory.setItem(14, Numbers.getThree());
        inventory.setItem(21, Numbers.getFour());
        inventory.setItem(22, Numbers.getFive());
        inventory.setItem(23, Numbers.getSix());
        inventory.setItem(25, getInput());
        inventory.setItem(30, Numbers.getSeven());
        inventory.setItem(31, Numbers.getEight());
        inventory.setItem(32, Numbers.getNine());
        inventory.setItem(36, Numbers.getEraser());
        inventory.setItem(40, Numbers.getZero());
        inventory.setItem(44, Numbers.getConfirm());

        try {
            fillEmptySlots(new ItemStack(Objects.requireNonNull(Material.matchMaterial("STAINED_GLASS_PANE", true)), 1));
        } catch (Throwable e) {
            fillEmptySlots(new ItemStack(Objects.requireNonNull(Material.matchMaterial("STAINED_GLASS_PANE")), 1));
        }
    }

    /**
     * Open the inventory to the player
     */
    public final void open() {
        try {
            makeInventory();
            player.openInventory(inventory);
        } catch (Throwable e) {
            logger.scheduleLog(Level.GRAVE, e);
            logger.scheduleLog(Level.INFO, "Couldn't open pin GUI to player " + player.getName());
        }
    }

    /**
     * Close the inventory to the player
     */
    public final void close() {
        if (player != null && player.isOnline()) {
            player.getInventory();
            player.closeInventory();
        }
    }

    /**
     * Confirm the pin input
     */
    public final void confirm() {
        User user = new User(player);

        if (!input.getOrDefault(player, "/-/-/-/").contains("/")) {
            if (!config.isBungeeCord()) {
                PlayerAuthEvent event = new PlayerAuthEvent(AuthType.PIN, EventAuthResult.WAITING, player, "");

                String pin = input.get(player).replaceAll("-", "");

                PasswordUtils utils = new PasswordUtils(pin, user.getPin());

                if (utils.validate()) {
                    if (user.has2FA()) {
                        event.setAuthResult(EventAuthResult.SUCCESS_TEMP, messages.gAuthInstructions());
                    } else {
                        event.setAuthResult(EventAuthResult.SUCCESS, messages.prefix() + messages.logged(player));
                    }
                } else {
                    event.setAuthResult(EventAuthResult.FAILED, messages.prefix() + messages.incorrectPin());
                }

                plugin.getServer().getPluginManager().callEvent(event);

                switch (event.getAuthResult()) {
                    case SUCCESS:
                        if (utils.validate()) {
                            user.setTempLog(false);
                            user.send(event.getAuthMessage());

                            if (utils.needsRehash(config.pinEncryption())) {
                                user.setPin(pin);
                            }

                            if (config.takeBack()) {
                                LastLocation lastLoc = new LastLocation(player);
                                if (lastLoc.hasLastLocation())
                                    user.teleport(lastLoc.getLastLocation());
                            }

                            user.removeBlindEffect();

                            verified.add(player);
                            input.put(player, "/-/-/-/");
                            updateInput();
                            close();

                            File motd_file = new File(plugin.getDataFolder(), "motd.locklogin");
                            Motd motd = new Motd(motd_file);

                            if (motd.isEnabled())
                                plugin.getServer().getScheduler().runTaskLater(plugin, () -> user.send(motd.onLogin(player.getName(), config.serverName())), 20L * motd.getDelay());
                        } else {
                            logger.scheduleLog(Level.WARNING, "Someone tried to force log (PIN AUTH) " + player.getName() + " using event API");
                        }
                        break;
                    case SUCCESS_TEMP:
                        if (utils.validate()) {
                            verified.add(player);

                            if (utils.needsRehash(config.pinEncryption())) {
                                user.setPin(pin);
                            }

                            if (!user.has2FA()) {
                                if (config.takeBack()) {
                                    LastLocation lastLoc = new LastLocation(player);
                                    user.teleport(lastLoc.getLastLocation());
                                }

                                user.removeBlindEffect();

                                user.setTempLog(false);
                            }
                        } else {
                            logger.scheduleLog(Level.WARNING, "Someone tried to force temp log (PIN AUTH) " + player.getName() + " using event API");
                        }

                        user.send(event.getAuthMessage());
                        break;
                    case FAILED:
                        break;
                    case ERROR:
                    case WAITING:
                        user.send(event.getAuthMessage());
                        break;
                }
            } else {
                String pinText = input.get(player).replaceAll("-", "");

                new BungeeSender(player).sendPinInput(pinText);

                input.put(player, "/-/-/-/");
            }
        } else {
            user.send(messages.prefix() + messages.pinLength());
        }
    }

    /**
     * Erase the last digit from
     * the input
     */
    public final void eraseInput() {
        String finalNew = "/-/-/-/";

        String[] current = input.getOrDefault(player, "/-/-/-/").split("-");
        String first = current[0];
        String second = current[1];
        String third = current[2];
        String fourth = current[3];

        if (!fourth.equals("/")) {
            finalNew = first + "-" + second + "-" + third + "-/";
        } else {
            if (!third.equals("/")) {
                finalNew = first + "-" + second + "-/-/";
            } else {
                if (!second.equals("/")) {
                    finalNew = first + "-/-/-/";
                } else {
                    if (!first.equals("/")) {
                        finalNew = "/-/-/-/";
                    }
                }
            }
        }

        input.put(player, finalNew);

        updateInput();
    }

    /**
     * Update the player inventory input
     */
    public final void updateInput() {
        player.getOpenInventory().setItem(25, getInput());
    }

    /**
     * Get the player pin input as
     * item
     *
     * @return the input item stack
     */
    public final ItemStack getInput() {
        ItemStack paper = new ItemStack(Material.PAPER, 1);
        ItemMeta paperMeta = paper.getItemMeta();
        assert paperMeta != null;

        paperMeta.setDisplayName(StringUtils.toColor("&c" + input.getOrDefault(player, "/-/-/-/")));

        paper.setItemMeta(paperMeta);

        return paper;
    }

    /**
     * Set the player pin input
     *
     * @param newInput the input
     */
    public final void setInput(String newInput) {
        String finalNew = "/-/-/-/";

        if (input.getOrDefault(player, "/-/-/-/").contains("/")) {
            String[] current = input.getOrDefault(player, "/-/-/-/").split("-");
            String first = current[0];
            String second = current[1];
            String third = current[2];
            String fourth = current[3];

            if (first.equals("/")) {
                finalNew = newInput + "-/-/-/";
            } else {
                if (second.equals("/")) {
                    finalNew = first + "-" + newInput + "-/-/";
                } else {
                    if (third.equals("/")) {
                        finalNew = first + "-" + second + "-" + newInput + "-/";
                    } else {
                        if (fourth.equals("/")) {
                            finalNew = first + "-" + second + "-" + third + "-" + newInput;
                        }
                    }
                }
            }

            input.put(player, finalNew);
        }
    }

    /**
     * Fill the empty inventory slots
     * with the specified item
     *
     * @param item the item
     */
    private void fillEmptySlots(ItemStack item) {
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack != null) {
                if (stack.getType().equals(Material.AIR)) {
                    inventory.setItem(i, item);
                }
            } else {
                inventory.setItem(i, item);
            }
        }
    }

    /**
     * Check if the player is verified
     *
     * @return if the player is verified
     */
    public final boolean isVerified() {
        return verified.contains(player);
    }

    /**
     * Set the player verification status
     *
     * @param status the status
     */
    public final void setVerified(boolean status) {
        if (status) {
            if (!verified.contains(player))
                verified.add(player);
        } else {
            verified.remove(player);
        }
    }
}
