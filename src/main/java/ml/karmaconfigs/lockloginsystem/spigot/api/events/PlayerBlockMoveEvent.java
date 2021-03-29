package ml.karmaconfigs.lockloginsystem.spigot.api.events;

import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.lockloginmodules.spigot.Module;
import ml.karmaconfigs.lockloginmodules.spigot.ModuleLoader;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.api.CancelReason;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
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
public class PlayerBlockMoveEvent extends Event implements SpigotFiles, LockLoginSpigot {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player target;

    private boolean cancelled = false;
    private CancelReason cancelledReason = CancelReason.UNKNOWN;

    /**
     * Initialize the player block move event
     *
     * @param player the player
     */
    public PlayerBlockMoveEvent(final Player player) {
        target = player;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Sets the cancellation state of this event. A cancelled event will not
     * be executed in the server, but will still pass to other plugins.
     *
     * @param cancel true if you wish to cancel this event
     */
    public final void setCancelled(boolean cancel, final CancelReason reason, final Module module) {
        if (ModuleLoader.manager.isLoaded(module)) {
            cancelled = cancel;
            cancelledReason = reason;

            logger.scheduleLog(Level.INFO, "Module " + module.name() + " [ " + module.version() + " ] cancelled the block player move event with the reason " + reason.name());
        } else {
            logger.scheduleLog(Level.GRAVE, "Module " + module.name() + "[ " + module.version() + " ] tried to cancel an even without being registered");
        }
    }

    /**
     * Get the target player
     *
     * @return the target player
     */
    public final Player getPlayer() {
        return target;
    }

    /**
     * Gets the cancellation state of this event. A cancelled event will not
     * be executed in the server, but will still pass to other plugins
     *
     * @return true if this event is cancelled
     */
    public final boolean isCancelled() {
        return cancelled;
    }

    /**
     * Get the cancel reason of the event
     *
     * @return the cancel reason
     */
    public final CancelReason getCancelReason() {
        return cancelledReason;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
