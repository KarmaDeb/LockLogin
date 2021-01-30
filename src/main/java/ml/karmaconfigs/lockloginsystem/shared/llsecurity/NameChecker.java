package ml.karmaconfigs.lockloginsystem.shared.llsecurity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    private final static HashMap<String, List<String>> nameIllegalChars = new HashMap<>();
    private final static HashMap<String, InvalidateReason> invalidationReason = new HashMap<>();
    private final String name;

    /**
     * Initialize the name checker for
     * a bungeecord player
     *
     * @param player the player
     */
    public NameChecker(String player) {
        this.name = player;
    }

    /**
     * Check if the player name is valid
     *
     * @return if the player name is valid
     */
    public final boolean isValid() {
        if (name.length() >= 3 && name.length() <= 16) {
            List<String> illegalChars = new ArrayList<>();
            for (int i = 0; i < name.length(); i++) {
                String letter = String.valueOf(name.charAt(i));
                if (!letter.matches(".*[aA-zZ].*") && !letter.matches(".*[0-9].*") && !letter.matches("_")) {
                    if (!illegalChars.contains(letter)) {
                        illegalChars.add(letter);
                    }
                }
            }

            if (!illegalChars.isEmpty()) {
                nameIllegalChars.put(name, illegalChars);
                invalidationReason.put(name, InvalidateReason.ILLEGAL);
                return false;
            } else {
                return true;
            }
        } else {
            if (name.length() < 3) {
                invalidationReason.put(name, InvalidateReason.MIN);
            }
            if (name.length() > 16) {
                invalidationReason.put(name, InvalidateReason.MAX);
            }
            return false;
        }
    }

    /**
     * Get the illegal chars the player
     * used
     *
     * @param name the user name
     * @return all the player name illegal characters
     */
    public final String getIllegalChars(String name) {
        if (invalidationReason.get(name) != null) {
            switch (invalidationReason.get(name)) {
                case MAX:
                    return "Max name length limit (16)";
                case MIN:
                    return "Min name length limit (3)";
                case ILLEGAL:
                    List<String> illegal = new ArrayList<>();
                    for (String str : nameIllegalChars.get(name)) {
                        if (str.equals(" ")) {
                            if (!illegal.contains("spaces")) {
                                illegal.add("spaces");
                            }
                        } else {
                            if (!illegal.contains(str)) {
                                illegal.add(str);
                            }
                        }
                    }

                    return illegal.toString()
                            .replace("[", "\u00a7b")
                            .replace(",", "{replace_comma_gray}\u00a7b")
                            .replace("]", "");
            }
        }
        return "\u00a7bNo illegal characters found\n\u00a7bThis may be an error, contact the staff!";
    }

    enum InvalidateReason {
        MIN, MAX, ILLEGAL
    }
}
