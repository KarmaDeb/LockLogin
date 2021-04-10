package ml.karmaconfigs.lockloginmodules.bungee;

import ml.karmaconfigs.lockloginmodules.Module;

/**
 * LockLogin general module utilities
 * for BungeeCord
 */
public interface ModuleUtil {

    /**
     * Check if the module is loaded
     *
     * @param module the module
     * @return if the module is loaded
     */
    static boolean isLoaded(final Module module) {
        if (module instanceof PluginModule) {
            return PluginModuleLoader.manager.isLoaded((PluginModule) module);
        } else {
            if (module instanceof AdvancedModule) {
                return AdvancedModuleLoader.manager.isLoaded((AdvancedModule) module);
            }
        }
        
        return false;
    }
}
