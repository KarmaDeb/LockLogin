package ml.karmaconfigs.lockloginmodules.spigot;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public abstract class Module {

    @NotNull
    public abstract JavaPlugin owner();

    @NotNull
    public abstract String name();

    @NotNull
    public abstract String version();

    @NotNull
    public abstract String author();

    @NotNull
    public abstract String description();

    @NotNull
    @Deprecated
    public abstract String author_url();

    public final List<String> getDescription() {
        String description = "&7" + description();

        String last_color = "&7";
        int length = 0;
        StringBuilder desc_builder = new StringBuilder();
        for (int i = 0; i < description.length(); i++) {
            String current = String.valueOf(description.charAt(i));
            String next = "";
            if (i + 1 != description.length())
                next = String.valueOf(description.charAt(i + 1));

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
}
