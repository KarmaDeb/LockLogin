package ml.karmaconfigs.lockloginmodules;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * LockLogin basic module
 */
public abstract class Module {

    /**
     * Get the module name
     *
     * @return the module name
     */
    @NotNull
    public abstract String name();

    /**
     * Get the module version
     *
     * @return the module version
     */
    @NotNull
    public abstract String version();

    /**
     * Get the module auth
     *
     * @return the module author
     */
    @NotNull
    public abstract String author();

    /**
     * Get the module description
     * as string
     *
     * @return the module description as string
     */
    @NotNull
    public abstract String description();

    /**
     * Get module update url
     *
     * @return the module update url
     */
    @NotNull
    public abstract String update_url();

    /**
     * Get the module update info
     *
     * @return the module update information
     */
    public final HashMap<Boolean, String> getUpdateInfo() {
        HashMap<Boolean, String> update_info = new HashMap<>();
        try {
            URL custom_url = new URL(update_url());
            InputStream input = custom_url.openStream();
            Scanner scanner = new Scanner(input);

            List<String> lines = new ArrayList<>();
            while (scanner.hasNext()) {
                lines.add(scanner.next());
            }

            String latest = lines.get(0);
            String update_url = lines.get(1);

            boolean outdated = latest != null && !latest.equalsIgnoreCase(version());
            update_info.put(outdated, update_url);
        } catch (Throwable ignored) {}

        return update_info;
    }

    /**
     * Get the module description
     *
     * @return the module description
     */
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
