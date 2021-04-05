package ml.karmaconfigs.lockloginsystem.bungeecord.api;

import ml.karmaconfigs.lockloginmodules.bungee.Module;
import ml.karmaconfigs.lockloginmodules.bungee.ModuleLoader;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.OfflineUser;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Utils;
import net.md_5.bungee.api.connection.ProxiedPlayer;
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
public class OfflineAPI implements BungeeFiles {

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
        if (ModuleLoader.manager.isLoaded(loader)) {
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
        if (config.isMySQL()) {
            Utils utils = new Utils();
            return utils.fetchUUID(player);
        } else {
            OfflineUser user = new OfflineUser("", player, true);
            return user.getUUID().toString();
        }
    }

    /**
     * Check if the player has 2fa
     *
     * @return if the player has 2fa
     */
    public final boolean has2FA() {
        if (config.isMySQL()) {
            Utils utils = new Utils();
            String player_uuid = utils.fetchUUID(player);

            utils = new Utils(player_uuid, player);

            return utils.has2fa();
        } else {
            OfflineUser user = new OfflineUser("", player, true);
            return user.has2FA();
        }
    }

    /**
     * Check if the player is registered
     *
     * @return if the player is registered
     */
    public final boolean isRegistered() {
        if (config.isMySQL()) {
            Utils utils = new Utils();
            String player_uuid = utils.fetchUUID(player);
            utils = new Utils(player_uuid, player);

            return utils.userExists() && utils.getPassword() != null && !utils.getPassword().isEmpty();
        } else {
            OfflineUser user = new OfflineUser("", player, true);
            return user.has2FA();
        }
    }

    /**
     * Get the user token
     *
     * @return the player google auth token
     */
    public final String getToken() {
        if (config.isMySQL()) {
            Utils utils = new Utils();
            String player_uuid = utils.fetchUUID(player);
            utils = new Utils(player_uuid, player);

            return utils.getToken();
        } else {
            OfflineUser user = new OfflineUser("", player, true);
            return user.getToken();
        }
    }

    /**
     * Get the online player API
     *
     * @return the online version of PlayerAPI
     */
    @Nullable
    public final PlayerAPI getOnline() {
        ProxiedPlayer player_instance = LockLoginBungee.plugin.getProxy().getPlayer(getUUID());

        if (player_instance != null)
            return new PlayerAPI(module, player_instance);

        return null;
    }
}
