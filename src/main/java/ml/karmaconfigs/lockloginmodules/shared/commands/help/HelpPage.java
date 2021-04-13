package ml.karmaconfigs.lockloginmodules.shared.commands.help;

import ml.karmaconfigs.lockloginmodules.shared.commands.CommandData;
import ml.karmaconfigs.lockloginmodules.shared.commands.LockLoginCommand;

import java.util.*;

/**
 * LockLogin help page
 */
public final class HelpPage {

    private final static Map<Integer, Set<String>> pages = new LinkedHashMap<>();

    private final int current_page;

    /**
     * Initialize the help page
     *
     * @param page the page
     */
    public HelpPage(final int page) {
        current_page = page;
    }

    /**
     * Scan for command data and fill pages
     */
    public final void scan() {
        Set<CommandData> tmpData = LockLoginCommand.getData();

        List<CommandData> datas = new ArrayList<>(tmpData);
        datas.sort(Comparator.comparing(o -> o.getArguments()[0]));

        StringBuilder builder = new StringBuilder();
        for (CommandData data : datas) {
            int lower_page = getLowerPage();
            Set<String> info = pages.getOrDefault(lower_page, new LinkedHashSet<>());

            builder.append("$");
            for (String arg : data.getArguments())
                builder.append(arg).append(" | ");

            builder.append(data.getDescription());

            info.add(builder.toString());
            pages.put(lower_page, info);
            builder = new StringBuilder();
        }
    }

    /**
     * Get the help pages
     *
     * @return the help pages
     */
    public final Set<String> getHelp() {
        return new LinkedHashSet<>(pages.getOrDefault(current_page, new LinkedHashSet<>()));
    }

    /**
     * Get all the available pages
     *
     * @return the help pages amount
     */
    public static int getPages() {
        return Math.min(pages.size() - 1, 0);
    }

    /**
     * Get the page with less info
     *
     * @return the lowest help page
     */
    private int getLowerPage() {
        int last_page = 0;
        for (int page : pages.keySet()) {
            Set<String> info = pages.getOrDefault(page, new LinkedHashSet<>());

            if (info.size() < 7) {
                last_page = page;
                break;
            }
        }

        return last_page;
    }
}
