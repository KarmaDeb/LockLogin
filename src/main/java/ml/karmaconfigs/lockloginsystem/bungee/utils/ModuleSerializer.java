package ml.karmaconfigs.lockloginsystem.bungee.utils;

import ml.karmaconfigs.lockloginmodules.bungee.AdvancedModule;
import ml.karmaconfigs.lockloginmodules.bungee.AdvancedModuleLoader;
import ml.karmaconfigs.lockloginmodules.bungee.PluginModule;
import ml.karmaconfigs.lockloginmodules.bungee.PluginModuleLoader;
import ml.karmaconfigs.lockloginsystem.bungee.LockLoginBungee;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

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
public final class ModuleSerializer implements LockLoginBungee {

    private final StringBuilder serialized = new StringBuilder();

    /**
     * Initialize the module serializer class
     *
     * @param issuer the serialization issuer
     */
    public ModuleSerializer(final ProxiedPlayer issuer) {
        serialized.append(issuer.getUniqueId()).append("{");
    }

    /**
     * Serialize the modules to string
     *
     * @return the serialized list of modules
     */
    public final String serialize() {
        HashMap<Plugin, HashSet<PluginModule>> loaded_modules = new HashMap<>();
        HashSet<PluginModule> locklogin_modules = PluginModuleLoader.manager.getByPlugin(plugin);

        loaded_modules.put(plugin, locklogin_modules);

        for (Plugin plugin : plugin.getProxy().getPluginManager().getPlugins()) {
            if (!plugin.equals(LockLoginBungee.plugin)) {
                loaded_modules.put(plugin, PluginModuleLoader.manager.getByPlugin(plugin));
            }
        }

        for (Plugin plugin : loaded_modules.keySet()) {
            HashSet<PluginModule> modules = loaded_modules.getOrDefault(plugin, new HashSet<>());

            for (PluginModule module : modules) {
                HashMap<Boolean, String> update_info = module.getUpdateInfo();
                boolean outdated = update_info.containsKey(true);

                serialized.append("Owner").append("=").append(module.owner().getDescription().getName()).append(",")
                        .append("Name").append("=").append(module.name()).append(",")
                        .append("Author").append("=").append(module.author()).append(",")
                        .append("Version").append("=").append(module.version()).append(",")
                        .append("Description").append("=").append(module.description()).append(",")
                        .append("Enabled").append("=").append(true).append(",")
                        .append("Outdated").append("=").append(outdated).append(",")
                        .append("URL").append("=").append(update_info.get(outdated)).append(";");
            }
        }

        AdvancedModuleLoader.manager.getModules().keySet().forEach(jarFile -> {
            AdvancedModule module = AdvancedModuleLoader.manager.getModules().getOrDefault(jarFile, null);

            if (module != null) {
                HashMap<Boolean, String> update_info = module.getUpdateInfo();
                boolean outdated = update_info.containsKey(true);

                serialized.append("Owner").append("=").append(jarFile.getName()).append(",")
                        .append("Name").append("=").append(module.name()).append(",")
                        .append("Author").append("=").append(module.author()).append(",")
                        .append("Version").append("=").append(module.version()).append(",")
                        .append("Description").append("=").append(module.description()).append(",")
                        .append("Enabled").append("=").append(AdvancedModuleLoader.manager.isLoaded(module)).append(",")
                        .append("Outdated").append("=").append(outdated).append(",")
                        .append("URL").append("=").append(update_info.get(outdated)).append(";");
            }
        });

        return removeGhostSemiColon(serialized.append("}").toString());
    }

    private String removeGhostSemiColon(String text) {
        return text.replaceFirst("(?s)" + ";" + "(?!.*?" + ";" + ")", "");
    }
}
