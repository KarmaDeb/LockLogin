package ml.karmaconfigs.LockLogin.Spigot.Utils.Inventory;

import ml.karmaconfigs.LockLogin.Logs.Logger;
import ml.karmaconfigs.LockLogin.Platform;
import ml.karmaconfigs.LockLogin.Security.PasswordUtils;
import ml.karmaconfigs.LockLogin.Spigot.API.Events.PlayerPinEvent;
import ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot;
import ml.karmaconfigs.LockLogin.Spigot.Utils.BungeeSender;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.SpigotFiles;
import ml.karmaconfigs.LockLogin.Spigot.Utils.StringUtils;
import ml.karmaconfigs.LockLogin.Spigot.Utils.User.User;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Private GSA code
 *
 * The use of this code
 * without GSA team authorization
 * will be a violation of
 * terms of use determined
 * in <a href="https://karmaconfigs.ml/license/"> here </a>
 */
public final class PinInventory implements LockLoginSpigot, SpigotFiles {

    private static Player player;
    private final Inventory inventory;

    private final static ArrayList<Player> verified = new ArrayList<>();

    private final static HashMap<Player, String> input = new HashMap<>();

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

        fillEmptySlots(new ItemStack(Material.LEGACY_STAINED_GLASS_PANE, 1));
    }

    /**
     * Open the inventory to the player
     */
    public final void open() {
        try {
            makeInventory();
            player.openInventory(inventory);
        } catch (Throwable e) {
            Logger.log(Platform.SPIGOT, "AN ERROR OCCURRED WHILE OPENING INVENTORY FOR player", e);
        }
    }

    /**
     * Close the inventory to the player
     */
    public final void close() {
        if (player != null && player.isOnline()) {
            if (player.getInventory() != null) {
                player.closeInventory();
            }
        }
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
     * Confirm the pin input
     */
    public final void confirm() {
        User user = new User(player);

        if (!input.getOrDefault(player, "/-/-/-/").contains("/")) {
            if (!config.isBungeeCord()) {
                String pin = input.get(player).replaceAll("-", "");

                PasswordUtils utils = new PasswordUtils(pin, user.getPin());

                if (new PasswordUtils(pin, user.getPin()).PasswordIsOk()) {
                    if (user.has2FA()) {
                        user.Message(messages.GAuthInstructions());
                    } else {
                        user.setTempLog(false);
                        user.Message(messages.Prefix() + messages.Logged(player));
                    }

                    verified.add(player);
                    input.put(player, "/-/-/-/");
                    updateInput();
                    close();
                } else {
                    user.Message(messages.Prefix() + messages.IncorrectPin());
                    input.put(player, "/-/-/-/");
                    updateInput();
                }

                PlayerPinEvent event = new PlayerPinEvent(player, utils.PasswordIsOk());
                plugin.getServer().getPluginManager().callEvent(event);
            } else {
                String pinText = input.get(player).replaceAll("-", "");

                new BungeeSender(player).sendPinInput(Integer.parseInt(pinText));

                input.put(player, "/-/-/-/");
            }
        } else {
            user.Message(messages.Prefix() + messages.PinLength());
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

    /**
     * Get the player pin input as
     * item
     *
     * @return an ItemStack
     */
    public final ItemStack getInput() {
        ItemStack paper = new ItemStack(Material.PAPER, 1);
        ItemMeta paperMeta = paper.getItemMeta();

        paperMeta.setDisplayName(StringUtils.toColor("&c" + input.getOrDefault(player, "/-/-/-/")));

        paper.setItemMeta(paperMeta);

        return paper;
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
     * @return a boolean
     */
    public final boolean isVerified() {
        return verified.contains(player);
    }

    /**
     * Clear the list of verified players
     */
    public static void clearVerifiedList() {
        verified.clear();
    }
}
