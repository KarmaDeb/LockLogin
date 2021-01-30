package ml.karmaconfigs.lockloginsystem.spigot.utils.inventory;

import dev.dbassett.skullcreator.SkullCreator;
import ml.karmaconfigs.api.spigot.StringUtils;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.OfflineUser;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class AltsAccountInventory implements InventoryHolder, LockLoginSpigot {

    private static final HashMap<UUID, Integer> playerPage = new HashMap<>();
    private static final HashMap<UUID, AltsAccountInventory> inventories = new HashMap<>();
    private final ArrayList<Inventory> pages = new ArrayList<>();
    private final Player player;

    /**
     * Initialize the infinite inventory page
     *
     * @param user    the player that called the inventory
     * @param players the players to show in the GUI
     */
    public AltsAccountInventory(final Player user, final HashSet<OfflineUser> players) {
        player = user;
        Inventory page = getBlankPage();

        for (OfflineUser player : players) {
            ItemStack item = SkullCreator.itemFromUuid(player.getUUID());
            ItemMeta meta = item.getItemMeta();

            meta.setDisplayName(StringUtils.toColor("&f" + player.getName()));
            if (!player.getUUID().equals(user.getUniqueId())) {
                meta.setLore(Arrays.asList("\n", StringUtils.toColor("&7UUID: &e" + player.getUUID())));
            } else {
                meta.setLore(Arrays.asList("\n", StringUtils.toColor("&7UUID: &e" + player.getUUID()), StringUtils.toColor("&8&l&o( &cYOU &8&l&o)")));
            }

            item.setItemMeta(meta);

            page.addItem(item);
        }

        pages.add(page);
        playerPage.put(player.getUniqueId(), 0);
        inventories.put(player.getUniqueId(), this);
    }

    /**
     * Initialize the infinite inventory page
     *
     * @param id    the id of the player that called the GUI
     * @param uuids the id of players to show in the GUI
     */
    public AltsAccountInventory(final UUID id, final HashSet<UUID> uuids) {
        player = plugin.getServer().getPlayer(id);
        Inventory page = getBlankPage();

        for (UUID uuid : uuids) {
            ItemStack item = SkullCreator.itemFromUuid(uuid);
            ItemMeta meta = item.getItemMeta();

            OfflinePlayer player = plugin.getServer().getOfflinePlayer(uuid);

            meta.setDisplayName(StringUtils.toColor("&f" + player.getName()));
            if (!player.getUniqueId().equals(id)) {
                meta.setLore(Arrays.asList("\n", StringUtils.toColor("&7UUID: &e" + player.getUniqueId())));
            } else {
                meta.setLore(Arrays.asList("\n", StringUtils.toColor("&7UUID: &e" + player.getUniqueId()), StringUtils.toColor("&8&l&o( &cYOU &8&l&o)")));
            }

            item.setItemMeta(meta);

            page.addItem(item);
        }

        pages.add(page);
        playerPage.put(player.getUniqueId(), 0);
        inventories.put(player.getUniqueId(), this);
    }

    /**
     * Open the player a member list in that
     * page
     *
     * @param page the page
     */
    public final void openPage(int page) {
        player.openInventory(pages.get(page));
        playerPage.put(player.getUniqueId(), page);

    }

    /**
     * Get the player inventory page
     *
     * @return a integer
     */
    public final int getPlayerPage() {
        if (player != null) {
            if (playerPage.get(player.getUniqueId()) != null) {
                return playerPage.get(player.getUniqueId());
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    /**
     * Get all the inventory pages
     *
     * @return an integer
     */

    public final int getPages() {
        return pages.size();
    }

    /**
     * Creates a new inventory page
     *
     * @return an Inventory
     */
    private Inventory getBlankPage() {
        String title = StringUtils.toColor(StringUtils.toColor("&8&lAlt accounts"));
        Inventory inv = plugin.getServer().createInventory(this, 54, title);

        inv.setItem(45, utils.backButton());
        inv.setItem(53, utils.nextButton());
        return inv;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return pages.get(getPlayerPage());
    }

    public interface manager {

        @Nullable
        static AltsAccountInventory getInventory(final Player player) {
            return inventories.getOrDefault(player.getUniqueId(), null);
        }
    }

    public interface utils {

        static ItemStack nextButton() {
            ItemStack next;
            try {
                next = new ItemStack(Material.GREEN_WOOL, 1);
            } catch (Throwable e) {
                try {
                    next = new ItemStack(Objects.requireNonNull(Material.matchMaterial("WOOL", true)), 1, DyeColor.GREEN.getWoolData());
                } catch (Throwable ex) {
                    next = new ItemStack(Objects.requireNonNull(Material.matchMaterial("WOOL")), 1, DyeColor.GREEN.getWoolData());
                }
            }
            ItemMeta meta = next.getItemMeta();

            meta.setDisplayName(StringUtils.toColor("&eNext"));
            next.setItemMeta(meta);

            return next;
        }

        static ItemStack backButton() {
            ItemStack back;
            try {
                back = new ItemStack(Material.RED_WOOL, 1);
            } catch (Throwable e) {
                try {
                    back = new ItemStack(Objects.requireNonNull(Material.matchMaterial("WOOL", true)), 1, DyeColor.RED.getWoolData());
                } catch (Throwable ex) {
                    back = new ItemStack(Objects.requireNonNull(Material.matchMaterial("WOOL")), 1, DyeColor.RED.getWoolData());
                }
            }
            ItemMeta meta = back.getItemMeta();

            meta.setDisplayName(StringUtils.toColor("&eBack"));
            back.setItemMeta(meta);

            return back;
        }
    }
}
