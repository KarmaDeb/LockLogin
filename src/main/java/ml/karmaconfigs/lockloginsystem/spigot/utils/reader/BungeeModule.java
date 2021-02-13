package ml.karmaconfigs.lockloginsystem.spigot.utils.reader;

import java.util.Arrays;
import java.util.List;

public final class BungeeModule {

    private final String owner;
    private final String name;
    private final String author;
    private final String version;
    private final boolean enabled;
    private final String description;

    /**
     * Initialize the bungee module class
     *
     * @param module_owner       the module owner
     * @param module_name        the module name
     * @param module_author      the module author
     * @param module_version     the module version
     * @param module_description the module description
     * @param module_status      the module status
     */
    public BungeeModule(final String module_owner, final String module_name, final String module_author, final String module_version, final String module_description, final boolean module_status) {
        owner = parse(module_owner);
        name = parse(module_name);
        author = parse(module_author);
        version = parse(module_version);
        description = parse(module_description);
        enabled = module_status;
    }

    /**
     * Parse the original string to include hidden values
     *
     * @param original the original value
     * @return the parsed value
     */
    private String parse(final String original) {
        return original.replace("{open}", "[")
                .replace("{close}", "]")
                .replace("{comma}", ",")
                .replace("{bracket_open}", "{")
                .replace("{bracket_close}", "}")
                .replace("{double_dot}", ":")
                .replace("{semi_colon}", ";")
                .replace("{equals}", "=");
    }

    /**
     * Get the module owner
     *
     * @return the bungee module owner
     */
    public final String getOwner() {
        return owner;
    }

    /**
     * Get the module name
     *
     * @return the bungee module name
     */
    public final String getName() {
        return name;
    }

    /**
     * Get the module author
     *
     * @return the bungee module author
     */
    public final String getAuthor() {
        return author;
    }

    /**
     * Get the module version
     *
     * @return the bungee module version
     */
    public final String getVersion() {
        return version;
    }

    /**
     * Get the module description
     *
     * @return the bungee module description
     */
    public final List<String> getDescription() {
        String desc = "&7" + description;

        String last_color = "&7";
        int length = 0;
        StringBuilder desc_builder = new StringBuilder();
        for (int i = 0; i < desc.length(); i++) {
            String current = String.valueOf(desc.charAt(i));
            String next = "";
            if (i + 1 != desc.length())
                next = String.valueOf(desc.charAt(i + 1));

            if (current.equals("&") || current.equals("\u00a7")) {
                if (!next.replaceAll("\\s", "").isEmpty()) {
                    String color = "&" + next;
                    if (isValidCode(color))
                        last_color = color;
                }
            }

            if (length == 24) {
                if (!next.replaceAll("\\s", "").isEmpty() && !current.replaceAll("\\s", "").isEmpty()) {
                    desc_builder.append(current).append("-").append("\n").append(last_color);
                } else {
                    desc_builder.append(current).append("\n").append(last_color);
                }
                length = -1;
            } else {
                desc_builder.append(current);
            }

            length++;
        }

        return Arrays.asList(desc_builder.toString().split("\n"));
    }

    private boolean isValidCode(final String color_code) {
        String val = color_code.replace("&", "").replace("\u00a7", "");

        switch (val) {
            case "1":
            case "2":
            case "3":
            case "4":
            case "5":
            case "6":
            case "7":
            case "8":
            case "9":
            case "0":
            case "a":
            case "b":
            case "c":
            case "d":
            case "e":
            case "f":
            case "k":
            case "l":
            case "m":
            case "n":
            case "o":
            case "r":
                return true;
            default:
                return false;
        }
    }

    /**
     * Get the module status
     *
     * @return the bungee module status
     */
    public final boolean isEnabled() {
        return enabled;
    }
}
