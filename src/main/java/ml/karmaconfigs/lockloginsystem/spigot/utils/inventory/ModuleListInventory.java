package ml.karmaconfigs.lockloginsystem.spigot.utils.inventory;

import ml.karmaconfigs.api.shared.StringUtils;
import ml.karmaconfigs.lockloginmodules.spigot.Module;
import ml.karmaconfigs.lockloginmodules.spigot.ModuleLoader;
import ml.karmaconfigs.lockloginsystem.shared.version.GetLatestVersion;
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

        int item_index = 0;
        if (SpigotFiles.config.isBungeeCord()) {
            BungeeModuleReader reader = new BungeeModuleReader(null);

            HashMap<String, HashSet<BungeeModule>> loaded_modules = reader.getBungeeModules();

            String last_plugin = "";
            for (String plugin : loaded_modules.keySet()) {
                if (last_plugin.replaceAll("\\s", "").isEmpty())
                    last_plugin = plugin;

                HashSet<BungeeModule> modules = loaded_modules.getOrDefault(plugin, new HashSet<>());

                if (!last_plugin.equals(plugin)) {
                    page.setItem(item_index, blackPane());
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
                    lore.add(StringUtils.toColor("&7Needs update: " + String.valueOf(module.isOutdated()).replace("true", "&ayes").replace("false", "&cno")));
                    if (module.isOutdated()) {
                        lore.add(StringUtils.toColor("&7Update from:"));
                        lore.add(StringUtils.toColor("&e" + module.getUpdateURL()));
                    }

                    meta.setLore(lore);
                    item.setItemMeta(meta);
                    page.addItem(item);

                    item_index++;
                    if (item_index > page.getSize())
                        item_index = 0;
                }
            }
        } else {
            HashSet<Module> locklogin_modules = ModuleLoader.manager.getByPlugin(plugin);

            //LockLogin always first
            {
                GetLatestVersion latest = new GetLatestVersion();

                int last_version_id = latest.GetLatest();
                int curr_version_id = LockLoginSpigot.versionID;

                boolean outdated = last_version_id > curr_version_id;
                String updateURL = "https://www.spigotmc.org/resources/gsa-locklogin.75156/";

                for (Module module : locklogin_modules) {
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
                    lore.add(StringUtils.toColor("&7Plugin enabled: " + String.valueOf(module.owner().getServer().getPluginManager().isPluginEnabled(module.owner())).replace("true", "&ayes").replace("false", "&cno")));
                    lore.add(StringUtils.toColor("&7Needs update: " + String.valueOf(outdated).replace("true", "&ayes").replace("false", "&cno")));
                    if (outdated) {
                        lore.add(StringUtils.toColor("&7Update from:"));
                        lore.add(StringUtils.toColor("&e" + updateURL));
                    }

                    meta.setLore(lore);
                    item.setItemMeta(meta);
                    page.addItem(item);

                    item_index++;
                }
            }

            HashMap<String, HashSet<Module>> loaded_modules = new HashMap<>();

            for (Plugin plugin : plugin.getServer().getPluginManager().getPlugins()) {
                if (!plugin.getName().equals("LockLogin")) {
                    if (!ModuleLoader.manager.getByPlugin(plugin).isEmpty()) {
                        loaded_modules.put(plugin.getName(), ModuleLoader.manager.getByPlugin(plugin));
                    }
                }
            }

            String last_plugin = "";
            HashMap<Boolean, String> update_info = new HashMap<>();
            boolean new_plugin = true;
            for (String plugin : loaded_modules.keySet()) {
                Iterator<Module> modules = loaded_modules.get(plugin).iterator();

                if (last_plugin.replaceAll("\\s", "").isEmpty()) {
                    last_plugin = plugin;
                    page.setItem(item_index, blackPane());
                }

                if (!last_plugin.equals(plugin)) {
                    last_plugin = plugin;
                    item_index++;
                    page.setItem(item_index, blackPane());
                    new_plugin = true;
                }

                while (modules.hasNext()) {
                    Module module = modules.next();

                    if (new_plugin) {
                        update_info = module.getUpdateInfo();
                        new_plugin = false;
                    }

                    boolean outdated = update_info.containsKey(true);
                    String updateURL = update_info.get(outdated);

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
                    lore.add(StringUtils.toColor("&7Plugin enabled: " + String.valueOf(module.owner().getServer().getPluginManager().isPluginEnabled(module.owner())).replace("true", "&ayes").replace("false", "&cno")));
                    lore.add(StringUtils.toColor("&7Needs update: " + String.valueOf(outdated).replace("true", "&ayes").replace("false", "&cno")));
                    if (outdated) {
                        lore.add(StringUtils.toColor("&7Update from:"));
                        lore.add(StringUtils.toColor("&e" + updateURL));
                    }

                    meta.setLore(lore);
                    item.setItemMeta(meta);
                    page.addItem(item);

                    item_index++;
                    if (item_index > page.getSize())
                        item_index = 0;
                }
            }
        }

        pages.add(page);
        playerPage.put(player.getUniqueId(), 0);
        inventories.put(player.getUniqueId(), this);
    }

    @SuppressWarnings("deprecation")
    private ItemStack blackPane() {
        ItemStack stack;
        try {
            stack = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);
        } catch (Throwable ex) {
            stack = new ItemStack(Material.valueOf("STAINED_GLASS_PANE"), 1, (byte) 15);
        }

        ItemMeta meta = stack.getItemMeta();
        assert meta != null;
        meta.setDisplayName(" ");

        stack.setItemMeta(meta);
        return stack;
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
