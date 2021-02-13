package ml.karmaconfigs.lockloginsystem.spigot.utils.inventory;

import ml.karmaconfigs.api.spigot.StringUtils;
import ml.karmaconfigs.lockloginmodules.spigot.Module;
import ml.karmaconfigs.lockloginmodules.spigot.ModuleLoader;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import ml.karmaconfigs.lockloginsystem.spigot.utils.reader.BungeeModule;
import ml.karmaconfigs.lockloginsystem.spigot.utils.reader.BungeeModuleReader;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class ModuleListInventory implements InventoryHolder, LockLoginSpigot {

    private static final HashMap<UUID, Integer> playerPage = new HashMap<>();
    private static final HashMap<UUID, ModuleListInventory> inventories = new HashMap<>();
    private final ArrayList<Inventory> pages = new ArrayList<>();
    private final Player player;

    /**
     * Initialize the infinite inventory page
     *
     * @param user the player that called the inventory
     */
    public ModuleListInventory(final Player user) {
        player = user;
        Inventory page = getBlankPage();

        if (SpigotFiles.config.isBungeeCord()) {
            BungeeModuleReader reader = new BungeeModuleReader(null);

            HashMap<String, HashSet<BungeeModule>> loaded_modules = reader.getBungeeModules();

            String last_plugin = "LockLogin";
            for (String plugin : loaded_modules.keySet()) {
                HashSet<BungeeModule> modules = loaded_modules.getOrDefault(plugin, new HashSet<>());

                if (!last_plugin.equals(plugin)) {
                    page.addItem(new ItemStack(Material.AIR));
                    last_plugin = plugin;
                }

                for (BungeeModule module : modules) {
                    ItemStack item = new ItemStack(Material.CHEST, 1);
                    ItemMeta meta = item.getItemMeta();
                    assert meta != null;

                    meta.setDisplayName(StringUtils.toColor("&f" + module.getName() + " &7&o[ &b" + module.getVersion() + " &7&o]"));
                    List<String> lore = new ArrayList<>();
                    for (String str : module.getDescription()) {
                        lore.add(StringUtils.toColor(str));
                    }
                    lore.add(" ");
                    lore.add(StringUtils.toColor("&7Owner:&c ") + module.getAuthor());
                    lore.add(StringUtils.toColor("&7Plugin:&c ") + module.getOwner());
                    lore.add(StringUtils.toColor("&7Plugin enabled: " + String.valueOf(module.isEnabled()).replace("true", "&ayes").replace("false", "&cno")));
                    meta.setLore(lore);

                    item.setItemMeta(meta);

                    page.addItem(item);
                }
            }
        } else {
            HashMap<Plugin, HashSet<Module>> loaded_modules = new HashMap<>();
            HashSet<Module> locklogin_modules = ModuleLoader.manager.getByPlugin(plugin);
            loaded_modules.put(plugin, locklogin_modules);

            for (Plugin plugin : plugin.getServer().getPluginManager().getPlugins()) {
                if (!plugin.equals(LockLoginSpigot.plugin)) {
                    loaded_modules.put(plugin, ModuleLoader.manager.getByPlugin(plugin));
                }
            }

            Plugin last_plugin = plugin.getServer().getPluginManager().getPlugin("LockLogin");
            assert last_plugin != null;

            for (Plugin plugin : loaded_modules.keySet()) {
                HashSet<Module> modules = loaded_modules.getOrDefault(plugin, new HashSet<>());

                if (!last_plugin.equals(plugin)) {
                    page.addItem(new ItemStack(Material.AIR));
                    last_plugin = plugin;
                }

                for (Module module : modules) {
                    ItemStack item = new ItemStack(Material.CHEST, 1);
                    ItemMeta meta = item.getItemMeta();
                    assert meta != null;

                    meta.setDisplayName(StringUtils.toColor("&f" + module.name() + " &7&o[ &b" + module.version() + " &7&o]"));
                    List<String> lore = new ArrayList<>();
                    for (String str : module.getDescription()) {
                        lore.add(StringUtils.toColor(str));
                    }
                    lore.add(" ");
                    lore.add(StringUtils.toColor("&7Owner:&c ") + module.author());
                    lore.add(StringUtils.toColor("&7Plugin:&c ") + module.owner().getName());
                    lore.add(StringUtils.toColor("&7Plugin enabled: " + String.valueOf(module.owner().isEnabled()).replace("true", "&ayes").replace("false", "&cno")));
                    meta.setLore(lore);

                    item.setItemMeta(meta);

                    page.addItem(item);
                }
            }
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
        String title = StringUtils.toColor(StringUtils.toColor("&8&lModules"));
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
        static ModuleListInventory getInventory(final Player player) {
            return inventories.getOrDefault(player.getUniqueId(), null);
        }
    }

    public interface utils {

        @SuppressWarnings("deprecation")
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
            assert meta != null;

            meta.setDisplayName(StringUtils.toColor("&eNext"));
            next.setItemMeta(meta);

            return next;
        }

        @SuppressWarnings("deprecation")
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
            assert meta != null;

            meta.setDisplayName(StringUtils.toColor("&eBack"));
            back.setItemMeta(meta);

            return back;
        }
    }
}
