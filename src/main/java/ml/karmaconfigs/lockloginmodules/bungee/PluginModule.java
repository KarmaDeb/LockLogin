package ml.karmaconfigs.lockloginmodules.bungee;

import ml.karmaconfigs.lockloginmodules.Module;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;


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
public abstract class PluginModule extends Module {

    /**
     * Get the module owner
     *
     * @return the module owner
     */
    @NotNull
    public abstract Plugin owner();
}
