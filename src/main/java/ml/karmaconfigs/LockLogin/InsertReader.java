package ml.karmaconfigs.LockLogin;

import java.util.HashMap;

/**
 * Private GSA code
 *
 * The use of this code
 * without GSA team authorization
 * will be a violation of
 * terms of use determined
 * in <a href="https://karmaconfigs.ml/license/"> here </a>
 */
public final class InsertReader {

    private final String[] data;

    /**
     * Initialize the insert info reader
     *
     * @param data the data
     */
    public InsertReader(String[] data) {
        this.data = data;
    }

    /**
     * Get a value from the path
     *
     * @param path the path
     * @return a String
     */
    public final Object get(String path) {
        HashMap<String, Object> values = new HashMap<>();
        for (String str : data) {
            String dataPath = str.split(":")[0];
            Object dataValue = str.replace(dataPath + ": ", "");
            values.put(dataPath, dataValue);
        }

        return values.getOrDefault(path, "");
    }
}
