package ml.karmaconfigs.LockLogin.Spigot.Events;

import ml.karmaconfigs.LockLogin.IPStorage.IPStorager;
import ml.karmaconfigs.LockLogin.IpData;
import ml.karmaconfigs.LockLogin.Platform;
import ml.karmaconfigs.LockLogin.Security.Checker;
import ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot;
import ml.karmaconfigs.LockLogin.Spigot.Utils.BungeeListener;
import ml.karmaconfigs.LockLogin.Spigot.Utils.DataFiles.AllowedCommands;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.SpigotFiles;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Inventory.PinInventory;
import ml.karmaconfigs.LockLogin.Spigot.Utils.StringUtils;
import ml.karmaconfigs.LockLogin.Spigot.Utils.User.BungeeVerifier;
import ml.karmaconfigs.LockLogin.Spigot.Utils.User.User;
import ml.karmaconfigs.LockLogin.WarningLevel;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
                            out.Alert("Player &f( &b" + player.getUniqueId().toString() + " &f)&e have been kicked for a failed bungee proxy check", WarningLevel.WARNING);
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
                            out.Alert("Player &f( &b" + player.getUniqueId().toString() + " &f)&e failed a BungeeCord check, performing a second check", WarningLevel.WARNING);
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

                            if (alreadyIn.getAddress().getAddress().equals(e.getAddress())) {
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

                        if (e.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED) {
                            IPStorager storager = new IPStorager(e.getAddress());

                            if (config.MaxRegisters() != 0) {
                                try {
                                    if (storager.getStorage().size() > config.MaxRegisters()) {
                                        if (storager.notSet(e.getName())) {
                                            e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
                                            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, StringUtils.toColor("&eLockLogin\n\n" + messages.MaxRegisters()));
                                        }
                                    } else {
                                        storager.saveStorage(e.getName());
                                    }
                                } catch (NumberFormatException ignored) {}
                            }

                            if (e.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED) {
                                if (config.AccountsPerIp() != 0) {
                                    IpData data = new IpData(e.getAddress());

                                    data.fetch(Platform.SPIGOT);

                                    if (data.getConnections() + 1 > config.AccountsPerIp()) {
                                        e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, StringUtils.toColor("&eLockLogin\n\n" + messages.MaxIp()));
                                    } else {
                                        if (!plugin.getServer().getOfflinePlayer(id).isBanned()) {
                                            data.addIP();
                                        }
                                    }
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
        if (config.isBungeeCord()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    checkPlayer(e.getPlayer());
                }
            }.runTaskLater(plugin, 20 * 2);
        }
    }

    /**
     * Check if the inventory is
     * pin GUI
     *
     * @param title the title
     * @return a boolean
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
                e.setCancelled(from.getBlockX() != to.getBlockX()
                        || from.getBlockZ() != to.getBlockZ()
                        || !(from.getY() - to.getY() >= 0));
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
                if (view != null) {
                    if (view.getTitle() != null && !view.getTitle().isEmpty()) {
                        if (notPinGUI(view.getTitle())) {
                            inventory.open();
                        }
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
     * Get the main command from the cmd
     * even if it has :
     *
     * @param cmd the cmd
     * @return a String
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
            } catch (Throwable ignored) {}
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
     * @return a String
     */
    private String getCompleteCommand(String cmd) {
        if (cmd.contains(" ")) {
            return cmd.split(" ")[0].replace("/", "");
        } else {
            return cmd.replace("/", "");
        }
    }

    //The little brother of onCommandEvent, very well optimized :)
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
                        if (!cmd.equals("login") && !cmd.equals("l")) {
                            e.setCancelled(true);
                            user.Message(messages.Prefix() + messages.Login());
                        }
                    }
                }
            } else {
                if (user.isTempLog()) {
                    if (user.has2FA()) {
                        if (!cmd.equals("2fa")) {
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
                if (view != null) {
                    if (view.getTitle() != null && !view.getTitle().isEmpty()) {
                        if (notPinGUI(view.getTitle())) {
                            inventory.open();
                        }
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

    /*
    Wow, I was really killing the cpu with this event...
    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onCommandEvent(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        User user = new User(player);

        String arg = e.getMessage().split(" ")[0];

        AllowedCommands allowed = new AllowedCommands();


        if (!user.isLogged()) {
            if (!config.isBungeeCord()) {
                if (arg.contains(":")) {
                    String command = arg;
                    arg = arg.split(":")[0];
                    String[] args = e.getMessage().split(":");
                    if (args.length >= 1) {
                        if (arg.equals("/locklogin")) {
                            if (args[1].equals("register") || args[1].equals("reg")) {
                                if (user.isRegistered()) {
                                    e.setCancelled(true);
                                    user.Message(messages.Prefix() + messages.AlreadyRegistered());
                                }
                            } else {
                                if (!args[1].equals("login") && !args[1].equals("l")) {
                                    e.setCancelled(true);
                                    user.Message(messages.Prefix() + messages.Login());
                                }
                            }
                        } else {
                            if (user.isRegistered()) {
                                if (!allowed.isAllowed(command)) {
                                    if (user.isRegistered()) {
                                        user.Message(messages.Prefix() + messages.Login());
                                    } else {
                                        user.Message(messages.Prefix() + messages.Register());
                                    }
                                    e.setCancelled(true);
                                }
                            } else {
                                e.setCancelled(true);
                                user.Message(messages.Prefix() + messages.Register());
                            }
                        }
                    } else {
                        e.setCancelled(true);
                        if (user.isRegistered()) {
                            user.Message(messages.Prefix() + messages.Login());
                        } else {
                            user.Message(messages.Prefix() + messages.Register());
                        }
                    }
                } else {
                    if (arg.equals("/register") || arg.equals("/reg")) {
                        if (user.isRegistered()) {
                            e.setCancelled(true);
                            user.Message(messages.Prefix() + messages.AlreadyRegistered());
                        }
                    } else {
                        if (!arg.equals("/login") && !arg.equals("/l")) {
                            if (user.isRegistered()) {
                                if (!allowed.isAllowed(arg)) {
                                    if (user.isRegistered()) {
                                        user.Message(messages.Prefix() + messages.Login());
                                    } else {
                                        user.Message(messages.Prefix() + messages.Register());
                                    }
                                    e.setCancelled(true);
                                }
                            } else {
                                e.setCancelled(true);
                                user.Message(messages.Prefix() + messages.Register());
                            }
                        }
                    }
                }
            }
        } else {
            if (user.isTempLog()) {
                if (user.has2FA()) {
                    if (arg.contains(":")) {
                        String command = arg;
                        arg = arg.split(":")[0];
                        String[] args = e.getMessage().split(":");
                        if (args.length >= 1) {
                            if (arg.equals("/locklogin")) {
                                if (!args[1].equals("2fa")) {
                                    e.setCancelled(true);
                                    user.Message(messages.Prefix() + messages.gAuthAuthenticate());
                                }
                            } else {
                                if (!allowed.isAllowed(command)) {
                                    e.setCancelled(true);
                                    user.Message(messages.Prefix() + messages.gAuthAuthenticate());
                                }
                            }
                        } else {
                            e.setCancelled(true);
                            user.Message(messages.Prefix() + messages.gAuthAuthenticate());
                        }
                    } else {
                        if (!arg.equals("/2fa")) {
                            if (!allowed.isAllowed(arg)) {
                                e.setCancelled(true);
                                user.Message(messages.Prefix() + messages.gAuthAuthenticate());
                            }
                        }
                    }
                } else {
                    e.setCancelled(true);
                }
            }
        }
    }
     */

    /**
     * Check the player inventory
     *
     * @param player the player
     */
    private void checkInventory(Player player) {
        User user = new User(player);
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            if (player.getInventory() != null || player.getOpenInventory() != null) {
                if (!config.isBungeeCord()) {
                    if (!user.hasPin()) {
                        plugin.getServer().getScheduler().runTask(plugin, () -> {
                            if (player.getInventory() != null && player.getOpenInventory() != null) {
                                player.closeInventory();
                            }
                        });
                    } else {
                        if (notPinGUI(player.getOpenInventory().getTitle())) {
                            plugin.getServer().getScheduler().runTask(plugin, () -> {
                                if (player.getInventory() != null && player.getOpenInventory() != null) {
                                    player.closeInventory();
                                }
                            });
                        } else {
                            PinInventory inventory = new PinInventory(player);

                            if (inventory.isVerified()) {
                                plugin.getServer().getScheduler().runTask(plugin, () -> {
                                    if (player.getInventory() != null && player.getOpenInventory() != null) {
                                        player.closeInventory();
                                    }
                                });
                            }
                        }
                    }
                } else {
                    if (!BungeeListener.inventoryAccess.contains(player)) {
                        plugin.getServer().getScheduler().runTask(plugin, () -> {
                            if (player.getInventory() != null && player.getOpenInventory() != null) {
                                player.closeInventory();
                            }
                        });
                    } else {
                        if (notPinGUI(player.getOpenInventory().getTitle())) {
                            plugin.getServer().getScheduler().runTask(plugin, () -> {
                                if (player.getInventory() != null && player.getOpenInventory() != null) {
                                    player.closeInventory();
                                }
                            });
                        } else {
                            PinInventory inventory = new PinInventory(player);

                            if (inventory.isVerified()) {
                                plugin.getServer().getScheduler().runTask(plugin, () -> {
                                    if (player.getInventory() != null && player.getOpenInventory() != null) {
                                        player.closeInventory();
                                    }
                                });
                            }
                        }
                    }
                }
            }
        }, 5);
    }
}
