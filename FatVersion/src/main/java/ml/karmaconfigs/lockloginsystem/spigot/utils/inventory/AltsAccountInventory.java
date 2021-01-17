package ml.karmaconfigs.lockloginsystem.spigot.utils.inventory;

import com.cryptomorin.xseries.XMaterial;
import ml.karmaconfigs.api.spigot.StringUtils;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.OfflineUser;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class AltsAccountInventory implements InventoryHolder, LockLoginSpigot {

    private final ArrayList<Inventory> pages = new ArrayList<>();
    private static final HashMap<UUID, Integer> playerPage = new HashMap<>();
    private static final HashMap<UUID, AltsAccountInventory> inventories = new HashMap<>();

    private final Player player;

    /**
     * Initialize the infinite inventory page
     *
     * @param user the player that called the inventory
     * @param players the players to show in the GUI
     */
    public AltsAccountInventory(final Player user, final HashSet<OfflineUser> players) {
        player = user;
        Inventory page = getBlankPage();

        for (OfflineUser player : players) {
            ItemStack item = new ItemStack(getMaterial());
            try {
                SkullMeta meta = (SkullMeta) item.getItemMeta();

                meta.setDisplayName(StringUtils.toColor("&f" + player.getName()));
                meta.setOwningPlayer(plugin.getServer().getOfflinePlayer(player.getUUID()));
                if (!player.getUUID().equals(user.getUniqueId())) {
                    meta.setLore(Arrays.asList("\n", StringUtils.toColor("&7UUID: &e" + player.getUUID())));
                } else {
                    meta.setLore(Arrays.asList("\n", StringUtils.toColor("&7UUID: &e" + player.getUUID()), StringUtils.toColor("&8&l&o( &cYOU &8&l&o)")));
                }

                item.setItemMeta(meta);
            } catch (Throwable ex) {
                ItemMeta meta = item.getItemMeta();

                meta.setDisplayName(StringUtils.toColor("&f" + player.getName()));
                if (!player.getUUID().equals(user.getUniqueId())) {
                    meta.setLore(Arrays.asList("\n", StringUtils.toColor("&7UUID: &e" + player.getUUID())));
                } else {
                    meta.setLore(Arrays.asList("\n", StringUtils.toColor("&7UUID: &e" + player.getUUID()), StringUtils.toColor("&8&l&o( &cYOU &8&l&o)")));
                }

                item.setItemMeta(meta);
            }

            page.addItem(item);
        }

        pages.add(page);
        playerPage.put(player.getUniqueId(), 0);
        inventories.put(player.getUniqueId(), this);
    }

    /**
     * Get the material using XMaterial API
     * which helps to find a material ignoring version
     * name changes
     *
     * @return an org.bukkit material
     */
    private Material getMaterial() {
        Optional<XMaterial> material = XMaterial.matchXMaterial("PLAYER_HEAD");
        if (material.isPresent()) {
            return material.get().parseMaterial();
        } else {
            return Material.STONE;
        }
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
            ItemStack next = new ItemStack(Material.GREEN_WOOL, 1);
            ItemMeta meta = next.getItemMeta();

            meta.setDisplayName(StringUtils.toColor("&eNext"));
            next.setItemMeta(meta);

            return next;
        }

        static ItemStack backButton() {
            ItemStack back = new ItemStack(Material.RED_WOOL, 1);
            ItemMeta meta = back.getItemMeta();

            meta.setDisplayName(StringUtils.toColor("&eBack"));
            back.setItemMeta(meta);

            return back;
        }
    }
}
