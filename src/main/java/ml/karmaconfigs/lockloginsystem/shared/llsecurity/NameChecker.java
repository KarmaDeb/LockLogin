package ml.karmaconfigs.lockloginsystem.shared.llsecurity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Private GSA code
 * <p>
 * The use of this code
 * without GSA team authorization
 * will be a violation of
 * terms of use determined
 * in <a href="https://karmaconfigs.ml/license/"> here </a>
 */
public final class NameChecker {

    private final static Map<String, List<String>> nameIllegalChars = new HashMap<>();
    private final static Map<String, InvalidateReason> invalidationReason = new HashMap<>();
    private final String name;

    /**
     * Initialize the name checker for
     * a bungee player
     *
     * @param player the player
     */
    public NameChecker(String player) {
        this.name = player;
    }

    /**
     * Check the name
     */
    public final void check() {
        nameIllegalChars.remove(name);
        invalidationReason.remove(name);

        if (name.length() >= 3 && name.length() <= 16) {
            List<String> illegalChars = new ArrayList<>();
            for (int i = 0; i < name.length(); i++) {
                char character = name.charAt(i);
                if (Character.isSpaceChar(character)) {
                    if (!illegalChars.contains("space"))
                        illegalChars.add("space");
                } else {
                    if (!Character.isLetterOrDigit(character) && character != '_') {
                        if (!illegalChars.contains(String.valueOf(character)))
                            illegalChars.add(String.valueOf(character));
                    }
                }
            }

            if (!illegalChars.isEmpty()) {
                nameIllegalChars.put(name, illegalChars);
                invalidationReason.put(name, InvalidateReason.ILLEGAL);
            }
        } else {
            if (name.length() < 3) {
                invalidationReason.put(name, InvalidateReason.MIN);
            }
            if (name.length() > 16) {
                invalidationReason.put(name, InvalidateReason.MAX);
            }
        }
    }

    /**
     * Check if the player name is valid
     *
     * @return if the player name is valid
     */
    public final boolean isInvalid() {
        return nameIllegalChars.containsKey(name) || invalidationReason.containsKey(name);
    }

    /**
     * Get the illegal chars the player
     * used
     *
     * @return all the player name illegal characters
     */
    public final String getIllegalChars() {
        if (invalidationReason.get(name) != null) {
            switch (invalidationReason.get(name)) {
                case MAX:
                    return "Max name length limit (16)";
                case MIN:
                    return "Min name length limit (3)";
                case ILLEGAL:
                    List<String> illegal = new ArrayList<>();
                    for (String character : nameIllegalChars.get(name))
                        illegal.add(character.replace(",", "comma"));

                    return illegal.toString()
                            .replace("[", "\u00a7b")
                            .replace(",", "\u00a77{comma}\u00a7b")
                            .replace("]", "");
            }
        }

        return "\u00a7bNo illegal characters found\n\u00a7bThis may be an error, contact the staff!";
    }

    enum InvalidateReason {
        MIN, MAX, ILLEGAL
    }
}
