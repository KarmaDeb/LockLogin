package ml.karmaconfigs.lockloginapi.bukkit;

import ml.karmaconfigs.lockloginmodules.Module;
import ml.karmaconfigs.lockloginmodules.bukkit.ModuleUtil;
import ml.karmaconfigs.lockloginsystem.bukkit.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.files.SpigotFiles;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.user.OfflineUser;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * GNU LESSER GENERAL PUBLIC LICENSE
 * Version 2.1, February 1999
 * <p>
 * Copyright (C) 1991, 1999 Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * Everyone is permitted to copy and distribute verbatim copies
 * of this license document, but changing it is not allowed.
 * <p>
 * [This is the first released version of the Lesser GPL.  It also counts
 * as the successor of the GNU Library Public License, version 2, hence
 * the version number 2.1.]
 */
public class OfflineAPI implements SpigotFiles {

    private final Module module;
    private final String player;

    /**
     * Initialize LockLogin bungee's API
     *
     * @param loader the module that is calling
     *               the API method
     * @param player the player
     */
    public OfflineAPI(final Module loader, String player) {
        module = loader;
        if (ModuleUtil.isLoaded(loader)) {
            this.player = player;
        } else {
            this.player = null;
        }
    }

    /**
     * Get the offline player UUID
     *
     * @return the player UUID
     */
    public final String getUUID() {
        OfflineUser user = new OfflineUser("", player, true);
        return user.getUUID().getId();
    }

    /**
     * Check if the player has 2fa
     *
     * @return if the player has 2fa
     */
    public final boolean has2FA() {
        OfflineUser user = new OfflineUser("", player, true);
        return user.has2FA();
    }

    /**
     * Check if the player is registered
     *
     * @return if the player is registered
     */
    public final boolean isRegistered() {
        OfflineUser user = new OfflineUser("", player, true);
        return user.has2FA();
    }

    /**
     * Get the user token
     *
     * @return the player google auth token
     */
    public final String getToken() {
        OfflineUser user = new OfflineUser("", player, true);
        return user.getToken();
    }

    /**
     * Get the online player API
     *
     * @return the online version of PlayerAPI
     */
    @Nullable
    public final PlayerAPI getAPI() {
        Player player_instance = LockLoginSpigot.plugin.getServer().getPlayer(getUUID());

        if (player_instance != null) {
            return new PlayerAPI(module, player_instance);
        }

        return null;
    }
}
