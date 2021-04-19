package ml.karmaconfigs.lockloginmodules.shared.channel.channeling;

import ml.karmaconfigs.lockloginsystem.shared.llsecurity.PasswordUtils;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.crypto.CryptType;

public class ChannelKey {

    private final String key;

    private ChannelKey(String token) {
        PasswordUtils utils = new PasswordUtils(token);
        token = utils.hashToken(CryptType.SHA512);

        key = token;
    }

    public static ChannelKey fromString(final String keyName) {
        return new ChannelKey(keyName);
    }

    public final String getKey() {
        return key;
    }
}
