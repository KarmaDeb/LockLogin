package ml.karmaconfigs.lockloginsystem.bungeecord.utils;

import ml.karmaconfigs.lockloginmodules.bungee.Module;
import ml.karmaconfigs.lockloginmodules.bungee.ModuleLoader;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.HashMap;
import java.util.HashSet;

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
        HashMap<Plugin, HashSet<Module>> loaded_modules = new HashMap<>();
        HashSet<Module> locklogin_modules = ModuleLoader.manager.getByPlugin(plugin);

        loaded_modules.put(plugin, locklogin_modules);

        for (Plugin plugin : plugin.getProxy().getPluginManager().getPlugins()) {
            if (!plugin.equals(LockLoginBungee.plugin)) {
                loaded_modules.put(plugin, ModuleLoader.manager.getByPlugin(plugin));
            }
        }

        int index = 0;
        for (Plugin plugin : loaded_modules.keySet()) {
            HashSet<Module> modules = loaded_modules.getOrDefault(plugin, new HashSet<>());

            //Format: <uuid>:{Owner=<Owner>,Name=<Name>,Author=<Author>,Version=<Version>,Description=<Description>,Enabled=<Status>,Updated=true,URL=<Update url>;Owner...;}

            for (Module module : modules) {
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

        return removeGhostSemiColon(serialized.append("}").toString());
    }

    private String removeGhostSemiColon(String text) {
        return text.replaceFirst("(?s)" + ";" + "(?!.*?" + ";" + ")", "");
    }
}
