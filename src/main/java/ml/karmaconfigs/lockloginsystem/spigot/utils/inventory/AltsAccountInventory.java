package ml.karmaconfigs.lockloginsystem.spigot.utils.inventory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import ml.karmaconfigs.api.shared.StringUtils;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles.SkullCache;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.OfflineUser;
import org.bukkit.Bukkit;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            Inventory page = getBlankPage();

            for (OfflineUser player : players) {
                ItemStack item = getPlayerHead(player.getName());
                ItemMeta meta = item.getItemMeta();
                assert meta != null;

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

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                player.openInventory(pages.get(0));
                playerPage.put(player.getUniqueId(), 0);
            });
        });
    }

    /**
     * Initialize the infinite inventory page
     *
     * @param id    the id of the player that called the GUI
     * @param uuids the id of players to show in the GUI
     */
    public AltsAccountInventory(final UUID id, final HashSet<UUID> uuids) {
        player = plugin.getServer().getPlayer(id);

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            if (player != null && player.isOnline()) {
                Inventory page = getBlankPage();

                for (UUID uuid : uuids) {
                    OfflinePlayer player = plugin.getServer().getOfflinePlayer(uuid);

                    ItemStack item = getPlayerHead(player.getName());
                    ItemMeta meta = item.getItemMeta();
                    assert meta != null;

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

                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.openInventory(pages.get(0));
                    playerPage.put(player.getUniqueId(), 0);
                });
            }
        });
    }

    /**
     * Get a skull item with the specified owner
     *
     * @return a SkullItem
     */
    @SuppressWarnings("deprecation")
    private static ItemStack getSkull() {
        ItemStack skull;

        try {
            skull = new ItemStack(Material.PLAYER_HEAD, 1);
        } catch (Throwable ex) {
            skull = new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (byte) 3);
        }

        return skull;
    }

    @SuppressWarnings("deprecation")
    private static ItemStack getPlayerHead(String owner) {
        SkullCache cache = new SkullCache(owner);
        String value;
        if (cache.needsCache() || cache.getValue() == null) {
            value = getHeadValue(owner);

            if (value != null)
                cache.saveSkullValue(value);
        } else {
            value = cache.getValue();
        }
        if (value == null)
            value = "";

        ItemStack skull = getSkull();
        UUID hashAsId = new UUID(value.hashCode(), value.hashCode());
        return Bukkit.getUnsafe().modifyItemStack(skull,
                "{SkullOwner:{Id:\"" + hashAsId + "\",Properties:{textures:[{Value:\"" + value + "\"}]}}}"
        );
    }

    private static String getHeadValue(String name) {
        try {
            String result = getURLContent("https://api.mojang.com/users/profiles/minecraft/" + name);

            Gson g = new Gson();
            JsonObject obj = g.fromJson(result, JsonObject.class);

            String uid = obj.get("id").toString().replace("\"", "");
            String signature = getURLContent("https://sessionserver.mojang.com/session/minecraft/profile/" + uid);

            obj = g.fromJson(signature, JsonObject.class);

            String value = obj.getAsJsonArray("properties").get(0).getAsJsonObject().get("value").getAsString();
            String decoded = new String(Base64.getDecoder().decode(value));

            obj = g.fromJson(decoded, JsonObject.class);

            String skinURL = obj.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();

            byte[] skinByte = ("{\"textures\":{\"SKIN\":{\"url\":\"" + skinURL + "\"}}}").getBytes();

            return new String(Base64.getEncoder().encode(skinByte));
        } catch (Exception ignored) {
        }

        return null;
    }

    private static String getURLContent(String urlStr) {
        URL url;
        BufferedReader in = null;
        StringBuilder sb = new StringBuilder();
        try {
            url = new URL(urlStr);
            in = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
            String str;
            while ((str = in.readLine()) != null) {
                sb.append(str);
            }
        } catch (Throwable ignored) {
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Throwable ignored) {
            }
        }

        return sb.toString();
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
