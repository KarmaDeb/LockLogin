package ml.karmaconfigs.lockloginsystem.spigot.events;

import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.api.shared.StringUtils;
import ml.karmaconfigs.api.spigot.Console;
import ml.karmaconfigs.lockloginsystem.shared.ipstorage.BFSystem;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.Checker;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.BungeeListener;
import ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles.AllowedCommands;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import ml.karmaconfigs.lockloginsystem.spigot.utils.inventory.PinInventory;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.BungeeVerifier;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.User;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.UUID;

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

public final class BlockedEvents implements Listener, LockLoginSpigot, SpigotFiles {

    private final static HashSet<InetAddress> botVerified = new HashSet<>();
    private final static HashSet<Location> netherWasHere = new HashSet<>();

    /**
     * Execute an UUID check for the player
     *
     * @param player the player
     */
    private void secondCheck(Player player) {
        new BukkitRunnable() {
            int back = 5;

            @Override
            public void run() {
                if (player != null) {
                    if (player.isOnline()) {
                        User user = new User(player);
                        BungeeVerifier verifier = new BungeeVerifier(player.getUniqueId());

                        if (back != 0) {
                            if (verifier.isVerified()) {
                                cancel();
                            } else {
                                back--;
                            }
                        } else {
                            user.Kick(config.BungeeProxy());
                            Console.send(plugin, "Player {0} have been kicked for a failed bungee proxy check", Level.INFO, player.getUniqueId());
                        }
                    } else {
                        cancel();
                    }
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    /**
     * Execute an UUID check for the player
     *
     * @param player the player
     */
    private void checkPlayer(Player player) {
        new BukkitRunnable() {
            int back = 5;

            @Override
            public void run() {
                if (player != null) {
                    if (player.isOnline()) {
                        BungeeVerifier verifier = new BungeeVerifier(player.getUniqueId());

                        if (back != 0) {
                            if (verifier.isVerified()) {
                                cancel();
                            } else {
                                back--;
                            }
                        } else {
                            Console.send(plugin, "Player {0} have been kicked for a failed bungee proxy check", Level.INFO, player.getUniqueId());
                            secondCheck(player);
                            cancel();
                        }
                    } else {
                        cancel();
                    }
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void playerServerListEvent(ServerListPingEvent e) {
        botVerified.add(e.getAddress());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void playerPreLoginEvent(AsyncPlayerPreLoginEvent e) {
        if (!config.isBungeeCord()) {
            if (e.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED) {
                BFSystem bf_prevention = new BFSystem(e.getAddress());
                if (bf_prevention.isBlocked() && config.BFMaxTries() > 0) {
                    e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
                    e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, StringUtils.toColor("&eLockLogin\n\n" + messages.ipBlocked(bf_prevention.getBlockLeft())));
                } else {
                    if (config.AntiBot()) {
                        if (!botVerified.contains(e.getAddress())) {
                            e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
                            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, StringUtils.toColor("&eLockLogin\n\n" + messages.AntiBot()));
                        }
                    }

                    if (e.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED) {
                        UUID id = e.getUniqueId();

                        if (plugin.getServer().getPlayer(id) != null) {
                            if (config.AllowSameIp()) {
                                Player alreadyIn = plugin.getServer().getPlayer(id);

                                if (alreadyIn != null && alreadyIn.getAddress() != null && alreadyIn.getAddress().getAddress().equals(e.getAddress())) {
                                    User user = new User(alreadyIn);
                                    user.setLogStatus(false);
                                    plugin.getServer().getScheduler().runTask(plugin, () -> user.Kick("&eLockLogin\n\n" + "&aYou've joined from another location with the same IP, if that's not you, contact the staff now" +
                                            "\n&aLockLogin will keep your account blocked (need of /login)"));

                                    e.allow();
                                } else {
                                    e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
                                    e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, StringUtils.toColor("&eLockLogin\n\n" + messages.AlreadyPlaying()));
                                }
                            } else {
                                e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
                                e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, StringUtils.toColor("&eLockLogin\n\n" + messages.AlreadyPlaying()));
                            }
                        }

                        if (e.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED) {
                            if (config.CheckNames()) {
                                if (!Checker.isValid(e.getName())) {
                                    e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
                                    e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, StringUtils.toColor("&eLockLogin\n\n" +
                                            messages.IllegalName(Checker.getIllegalChars(e.getName()))));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void playerJoinEvent(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (config.isBungeeCord()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    checkPlayer(player);
                }
            }.runTaskLater(plugin, 20 * 2);
        }

        if (isNether(player)) {
            netherWasHere.add(player.getLocation());
            player.getLocation().getBlock().setType(Material.AIR);

            new BukkitRunnable() {
                @Override
                public void run() {
                    User user = new User(player);
                    if (user.isLogged() && !user.isTempLog() || !player.isOnline()) {
                        plugin.getServer().getScheduler().runTask(plugin, () -> {
                            boolean restorePortal = true;
                            for (Entity near : player.getLocation().getBlock().getWorld().getNearbyEntities(player.getLocation().getBlock().getLocation(), 1, 1, 1)) {
                                if (near instanceof Player) {
                                    Player near_player = (Player) near;
                                    if (near_player.hasMetadata("LockLoginUser")) {
                                        User near_user = new User(near_player);
                                        if (!near_user.isLogged() || near_user.isTempLog())
                                            restorePortal = false;
                                    }
                                }
                            }

                            if (restorePortal) {
                                player.getLocation().getBlock().setType(Material.FIRE);
                                player.setFireTicks(0);
                                netherWasHere.remove(player.getLocation());
                            }
                        });
                        cancel();
                    }
                }
            }.runTaskTimerAsynchronously(plugin, 0, 1);
        }
    }

    /**
     * Check if the inventory is
     * pin GUI
     *
     * @param title the title
     * @return if the title does not match with
     * LockLogin pinner
     */
    private boolean notPinGUI(String title) {
        String check = "LockLogin pinner";

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < title.length(); i++) {
            String letter = String.valueOf(title.charAt(i));
            if (letter.matches(".*[aA-zZ].*") || letter.matches(".*[0-9].*") || letter.matches(" ")) {
                builder.append(letter);
            }
        }

        title = builder.toString();

        builder = new StringBuilder();
        for (int i = 0; i < check.length(); i++) {
            String letter = String.valueOf(check.charAt(i));
            if (letter.matches(".*[aA-zZ].*") || letter.matches(".*[0-9].*") || letter.matches(" ") && !letter.matches("&" + ".*[aA-zZ]") && !letter.matches("&" + ".*[0-9].*")) {
                builder.append(letter);
            }
        }
        check = builder.toString();

        return !title.contains(check);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void menuClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        User user = new User(player);

        if (!config.isBungeeCord()) {
            if (user.isTempLog() && user.hasPin()) {
                PinInventory inventory = new PinInventory(player);

                if (!inventory.isVerified()) {
                    plugin.getServer().getScheduler().runTaskLater(plugin, inventory::open, 5);
                }
            }
        } else {
            if (BungeeListener.inventoryAccess.contains(player)) {
                PinInventory inventory = new PinInventory(player);

                if (!inventory.isVerified()) {
                    plugin.getServer().getScheduler().runTaskLater(plugin, inventory::open, 5);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void menuInteract(InventoryInteractEvent e) {
        Player player = (Player) e.getWhoClicked();
        User user = new User(player);

        if (!user.isLogged() || user.isTempLog()) {
            checkInventory(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void menuClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        User user = new User(player);

        if (!user.isLogged() || user.isTempLog()) {
            checkInventory(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onItemDrop(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        User user = new User(player);

        if (!user.isLogged() || user.isTempLog()) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("deprecation")
    public final void onItemPickup(PlayerPickupItemEvent e) {
        Player player = e.getPlayer();
        User user = new User(player);

        if (!user.isLogged() || user.isTempLog()) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void playerMove(PlayerMoveEvent e) {
        if (!e.isCancelled()) {
            Player player = e.getPlayer();
            User user = new User(player);

            Location from = e.getFrom();
            Location to = e.getTo();

            if (!user.isLogged() || user.isTempLog()) {
                if (to != null) {
                    e.setCancelled(from.getBlockX() != to.getBlockX()
                            || from.getBlockZ() != to.getBlockZ()
                            || !(from.getY() - to.getY() >= 0));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void playerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        User user = new User(player);

        if (!user.isLogged() || user.isTempLog()) {
            if (!e.getAction().equals(Action.PHYSICAL)) {
                e.setCancelled(true);
                checkInventory(player);
            } else {
                if (e.getClickedBlock() != null && !e.getClickedBlock().getType().equals(Material.AIR)) {
                    if (e.getClickedBlock().getType().name().contains("PLATE")) {
                        e.getClickedBlock().setMetadata("DisableRedstone", new FixedMetadataValue(plugin, plugin));
                    } else {
                        e.setCancelled(true);
                    }
                }
            }
        } else {
            if (e.getAction().equals(Action.PHYSICAL)) {
                if (e.getClickedBlock() != null && !e.getClickedBlock().getType().equals(Material.AIR)) {
                    if (e.getClickedBlock().hasMetadata("DisableRedstone")) {
                        e.getClickedBlock().removeMetadata("DisableRedstone", plugin);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onRedstoneSignal(BlockRedstoneEvent e) {
        Block block = e.getBlock();
        if (block.hasMetadata("DisableRedstone")) {
            e.setNewCurrent(0);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void playerInteractToEntity(PlayerInteractEntityEvent e) {
        Player player = e.getPlayer();
        User user = new User(player);

        if (!user.isLogged() || user.isTempLog()) {
            e.setCancelled(true);
            checkInventory(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void playerInteractWithEntity(PlayerInteractAtEntityEvent e) {
        Player player = e.getPlayer();
        User user = new User(player);

        if (!user.isLogged() || user.isTempLog()) {
            e.setCancelled(true);
            checkInventory(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void playerDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            Player player = (Player) e.getDamager();
            if (player.hasMetadata("LockLoginUser")) {
                User user = new User(player);

                if (!user.isLogged() || user.isTempLog()) {
                    e.setCancelled(true);
                    if (user.isRegistered()) {
                        user.Message(messages.Prefix() + messages.Login());
                    } else {
                        user.Message(messages.Prefix() + messages.Register());
                    }
                }
            }
        }
        if (e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();
            User user = new User(player);

            if (e.getDamager() instanceof Player) {
                Player attacker = (Player) e.getDamager();
                User attackerUser = new User(attacker);

                if (player.hasMetadata("LockLoginUser")) {
                    if (attacker.hasMetadata("LockLoginUser")) {
                        if (!user.isLogged() || user.isTempLog()) {
                            e.setCancelled(true);
                            attackerUser.Message(messages.Prefix() + messages.NotVerified(player));
                        }
                    }
                }
            } else {
                if (player.hasMetadata("LockLoginUser")) {
                    if (e.getDamager() instanceof Monster) {
                        if (!user.isLogged() || user.isTempLog()) {
                            e.setCancelled(true);
                            if (!e.getDamager().isDead()) {
                                e.getDamager().getWorld().playEffect(e.getDamager().getLocation(), Effect.EXTINGUISH, 10);
                                e.getDamager().getWorld().playEffect(e.getDamager().getLocation(), Effect.SMOKE, 10);
                                e.getDamager().remove();
                            }
                        }
                    }
                }
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onPlayerDamageNotByEntity(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();
            if (player.hasMetadata("LockLoginUser")) {
                User user = new User(player);

                if (!user.isLogged() || user.isTempLog()) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void blockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        User user = new User(player);

        if (!user.isLogged() || user.isTempLog()) {
            e.setCancelled(true);
            checkInventory(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onChatEvent(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();

        if (!config.isBungeeCord()) {
            User user = new User(player);

            if (!user.isLogged() || user.isTempLog()) {
                e.setCancelled(true);
                if (!user.isLogged()) {
                    if (user.isRegistered()) {
                        user.Message(messages.Prefix() + messages.Login());
                    } else {
                        user.Message(messages.Prefix() + messages.Register());
                    }
                }
                if (user.isTempLog()) {
                    e.setCancelled(true);
                    if (user.has2FA()) {
                        user.Message(messages.Prefix() + messages.gAuthAuthenticate());
                    }
                }
            }
        } else {
            BungeeVerifier verifier = new BungeeVerifier(player.getUniqueId());

            if (BungeeListener.inventoryAccess.contains(player)) {
                PinInventory inventory = new PinInventory(player);

                InventoryView view = player.getOpenInventory();
                if (!view.getTitle().isEmpty()) {
                    if (notPinGUI(view.getTitle())) {
                        inventory.open();
                    }
                }
            }
            if (!verifier.isVerified()) {
                e.setCancelled(true);
            }
        }
    }

    /**
     * Get the main command from the cmd
     * even if it has :
     *
     * @param cmd the cmd
     * @return a command ignoring ":" prefix
     */
    private String getCommand(String cmd) {
        if (cmd.contains(":")) {
            try {
                String[] cmdData = cmd.split(":");

                if (cmdData[0] != null && !cmdData[0].isEmpty()) {
                    if (cmdData[1] != null && !cmdData[1].isEmpty()) {
                        return cmdData[1];
                    }
                }
            } catch (Throwable ignored) {
            }
            return cmd.split(" ")[0].replace("/", "");
        } else {
            if (cmd.contains(" ")) {
                return cmd.split(" ")[0].replace("/", "");
            } else {
                return cmd.replace("/", "");
            }
        }
    }

    /**
     * Get the complete main command
     * including ':'
     *
     * @param cmd the cmd
     * @return a command including ":" prefix
     */
    private String getCompleteCommand(String cmd) {
        if (cmd.contains(" ")) {
            return cmd.split(" ")[0].replace("/", "");
        } else {
            return cmd.replace("/", "");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onCommand(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();

        if (!config.isBungeeCord()) {
            User user = new User(player);

            AllowedCommands allowed = new AllowedCommands();

            String cmd = getCommand(e.getMessage());

            if (!user.isLogged()) {
                if (!user.isRegistered()) {
                    if (!cmd.equals("register") && !cmd.equals("reg")) {
                        e.setCancelled(true);
                        user.Message(messages.Prefix() + messages.Register());
                    }
                } else {
                    if (!allowed.isAllowed(getCompleteCommand(e.getMessage()))) {
                        if (!cmd.equals("login") && !cmd.equals("l") && !cmd.equals("recovery")) {
                            e.setCancelled(true);
                            user.Message(messages.Prefix() + messages.Login());
                        }
                    }
                }
            } else {
                if (user.isTempLog()) {
                    if (user.has2FA() || user.hasPin()) {
                        if (!cmd.equals("2fa") && !cmd.equals("recovery")) {
                            e.setCancelled(true);
                            user.Message(messages.Prefix() + messages.gAuthAuthenticate());
                        }
                    }
                }
            }
        } else {
            BungeeVerifier verifier = new BungeeVerifier(player.getUniqueId());

            if (BungeeListener.inventoryAccess.contains(player)) {
                PinInventory inventory = new PinInventory(player);

                InventoryView view = player.getOpenInventory();
                if (!view.getTitle().isEmpty()) {
                    if (notPinGUI(view.getTitle())) {
                        inventory.open();
                    }
                } else {
                    inventory.open();
                }
            }
            if (!verifier.isVerified()) {
                e.setCancelled(true);
            }
        }
    }

    /**
     * Check the player inventory
     *
     * @param player the player
     */
    private void checkInventory(Player player) {
        User user = new User(player);
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            if (!config.isBungeeCord()) {
                if (!user.hasPin()) {
                    plugin.getServer().getScheduler().runTask(plugin, player::closeInventory);
                } else {
                    if (notPinGUI(player.getOpenInventory().getTitle())) {
                        plugin.getServer().getScheduler().runTask(plugin, player::closeInventory);
                    } else {
                        PinInventory inventory = new PinInventory(player);

                        if (inventory.isVerified()) {
                            plugin.getServer().getScheduler().runTask(plugin, player::closeInventory);
                        }
                    }
                }
            } else {
                if (!BungeeListener.inventoryAccess.contains(player)) {
                    plugin.getServer().getScheduler().runTask(plugin, player::closeInventory);
                } else {
                    if (notPinGUI(player.getOpenInventory().getTitle())) {
                        plugin.getServer().getScheduler().runTask(plugin, player::closeInventory);
                    } else {
                        PinInventory inventory = new PinInventory(player);

                        if (inventory.isVerified()) {
                            plugin.getServer().getScheduler().runTask(plugin, player::closeInventory);
                        }
                    }
                }
            }
        }, 5);
    }

    private boolean isNether(final Player player) {
        boolean isNether = player.getLocation().add(player.getLocation().getX() > 0 ? 0.5 : -0.5, 0.0, player.getLocation().getZ() > 0 ? 0.5 : -0.5).getBlock().getType().equals(getPortal());

        if (!isNether) {
            for (Location location : netherWasHere) {
                if (location.distance(player.getLocation()) < 3)
                    isNether = true;
            }
        }

        return isNether;
    }

    private Material getPortal() {
        try {
            return Material.getMaterial("NETHER_PORTAL");
        } catch (Throwable ex) {
            return Material.getMaterial("PORTAL");
        }
    }
}