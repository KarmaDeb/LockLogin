package ml.karmaconfigs.lockloginsystem.shared.alerts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Private GSA code
 *
 * The use of this code
 * without GSA team authorization
 * will be a violation of
 * terms of use determined
 * in <a href="https://karmaconfigs.ml/license/"> here </a>
 */
public final class GetWarningMSG {

    private String ALERT_LEVEL = "&4ERROR ";
    private String ALERT_MSG = "&cCould not make a connection with the alerts system";

    /**
     * Starts retrieving the info from the html file
     */
    public GetWarningMSG() {
        try {
            URL url = new URL("https://karmaconfigs.github.io/updates/LockLogin/alerts.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String word;
            List<String> lines = new ArrayList<>();
            while ((word = reader.readLine()) != null)
                if (!lines.contains(word)) {
                    lines.add(word);
                }
            reader.close();
            List<String> replaced = new ArrayList<>();
            for (String str : lines) {
                if (!replaced.contains(str)) {
                    replaced.add(str
                            .replace("[", "replace-one")
                            .replace("]", "replace-two")
                            .replace(",", "replace-comma")
                            .replace("_", "&"));
                }
            }
            this.ALERT_LEVEL = replaced.get(0)
                    .replace("replace-one", "[")
                    .replace("replace-two", "]")
                    .replace("replace-comma", ",");
            this.ALERT_MSG = replaced.get(1)
                    .replace("replace-one", "[")
                    .replace("replace-two", "]")
                    .replace("replace-comma", ",");
        } catch (IOException ignore) {
        }
    }

    /**
     * @return the alert level
     */
    public final String GetLevel() {
        return ALERT_LEVEL + ": ";
    }

    /**
     * @return the alert message
     */
    public final String GetMessage() {
        return ALERT_MSG;
    }
}
