package ml.karmaconfigs.lockloginsystem.spigot.utils.reader;

import java.util.HashMap;
import java.util.HashSet;

public final class BungeeModuleReader {

    private final HashMap<String, HashSet<BungeeModule>> modules = new HashMap<>();

    private final String data;

    /**
     * Initialize the bungee module reader
     *
     * @param _data the data to read from
     */
    public BungeeModuleReader(final String _data) {
        data = _data;
    }

    public final void parse() {
        if (data.contains(";")) {
            String[] full_data = data.split(";");

            String owner = "";
            String name = "";
            String author = "";
            String version = "";
            String description = "";
            boolean enabled = true;
            boolean outdated = false;
            String updateURL = "";

            HashSet<BungeeModule> detected_modules = new HashSet<>();

            for (String module : full_data) {
                String[] prop_values = module.split(",");

                for (String properties : prop_values) {
                    String[] final_data = properties.split("=");

                    String property_name = final_data[0];
                    String property_value = final_data[1];

                    switch (property_name) {
                        case "Owner":
                            owner = property_value;
                            break;
                        case "Name":
                            name = property_value;
                            break;
                        case "Author":
                            author = property_value;
                            break;
                        case "Version":
                            version = property_value;
                            break;
                        case "Description":
                            description = property_value;
                            break;
                        case "Enabled":
                            enabled = Boolean.parseBoolean(property_value);
                            break;
                        case "Outdated":
                            outdated = Boolean.parseBoolean(property_value);
                            break;
                        case "URL":
                            updateURL = property_value;
                            break;
                        default:
                            break;
                    }
                }

                BungeeModule decoded_module = new BungeeModule(owner, name, author, version, description, enabled, outdated, updateURL);
                detected_modules.add(decoded_module);
            }

            for (BungeeModule detected : detected_modules) {
                String module_owner = detected.getName();

                HashSet<BungeeModule> stored_modules = modules.getOrDefault(module_owner, new HashSet<>());
                stored_modules.add(detected);
                modules.put(module_owner, stored_modules);
            }
        } else {
            String[] prop_values = data.split(",");

            String owner = "";
            String name = "";
            String author = "";
            String version = "";
            String description = "";
            boolean enabled = true;
            boolean outdated = false;
            String updateURL = "";

            for (String properties : prop_values) {
                String[] final_data = properties.split("=");

                String property_name = final_data[0];
                String property_value = final_data[1];

                switch (property_name) {
                    case "Owner":
                        owner = property_value;
                        break;
                    case "Name":
                        name = property_value;
                        break;
                    case "Author":
                        author = property_value;
                        break;
                    case "Version":
                        version = property_value;
                        break;
                    case "Description":
                        description = property_value;
                        break;
                    case "Enabled":
                        enabled = Boolean.parseBoolean(property_value);
                        break;
                    case "Outdated":
                        outdated = Boolean.parseBoolean(property_value);
                        break;
                    case "URL":
                        updateURL = property_value;
                        break;
                    default:
                        break;
                }
            }

            BungeeModule decoded_module = new BungeeModule(owner, name, author, version, description, enabled, outdated, updateURL);
            String module_owner = decoded_module.getName();

            HashSet<BungeeModule> stored_modules = modules.getOrDefault(module_owner, new HashSet<>());
            stored_modules.add(decoded_module);
            modules.put(module_owner, stored_modules);
        }
    }

    /**
     * Get a map of BungeeCord modules
     *
     * @return a map of bungeecord modules
     */
    public final HashMap<String, HashSet<BungeeModule>> getBungeeModules() {
        return modules;
    }
}
