package ml.karmaconfigs.lockloginsystem.bungeecord.utils;

import net.md_5.bungee.api.ChatColor;

import java.util.Random;

/**
 * Private GSA code
 *
 * The use of this code
 * without GSA team authorization
 * will be a violation of
 * terms of use determined
 * in <a href="https://karmaconfigs.ml/license/"> here </a>
 */
public interface StringUtils {

    static String toColor(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    static String stripColor(String text) {
        return ChatColor.stripColor(toColor(text));
    }

    static String randomString(int Size) {
        int leftLimit = 97;
        int rightLimit = 122;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .limit(Size)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    static boolean isNull(Object object) {
        if (object == null) {
            return true;
        } else {
            return object.toString().isEmpty();
        }
    }
}
