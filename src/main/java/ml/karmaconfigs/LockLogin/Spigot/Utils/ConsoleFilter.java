package ml.karmaconfigs.LockLogin.Spigot.Utils;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.Message;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/*
GNU LESSER GENERAL PUBLIC LICENSE
                       Version 2.1, February 1999

 Copyright (C) 1991, 1999 Free Software Foundation, Inc.
 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 Everyone is permitted to copy and distribute verbatim copies
 of this license document, but changing it is not allowed.

[This is the first released version of the Lesser GPL.  It also counts
 as the successor of the GNU Library Public License, version 2, hence
 the version number 2.1.]
 */

public class ConsoleFilter implements Filter {

    private static final Set<String> SENSITIVE_COMMANDS =
            new HashSet<>(
                    Arrays.asList(
                            "/login",
                            "/register",
                            "/2fa",
                            "/resetfa",
                            "/change",
                            "/delaccount",
                            "/unlog",
                            "/reg",
                            "/l",
                            "/cpass",
                            "/pin",
                            "/resetpin",
                            "/rpin",
                            "/delpin",
                            "/locklogin:"
                    )
            );

    private boolean isSensitiveMessage(String msg) {
        if (msg == null) return false;

        msg = msg.toLowerCase();
        return msg.contains("issued server command:") && SENSITIVE_COMMANDS.stream().anyMatch(msg::contains);
    }

    public final Filter.Result filter(LogEvent record) {
        if (record != null) {
            Message message = record.getMessage();
            if (message != null && isSensitiveMessage(message.getFormattedMessage())) {
                return Result.DENY;
            }
        }
        return Result.NEUTRAL;
    }

    public final Filter.Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object... arg4) {
        return isSensitiveMessage(message) ? Filter.Result.DENY : Result.NEUTRAL;
    }

    public final Filter.Result filter(Logger arg0, Level arg1, Marker arg2, Object message, Throwable arg4) {
        return isSensitiveMessage(message.toString()) ? Filter.Result.DENY : Result.NEUTRAL;
    }

    public final Filter.Result filter(Logger arg0, Level arg1, Marker arg2, Message message, Throwable arg4) {
        return message != null && isSensitiveMessage(message.getFormattedMessage()) ? Filter.Result.DENY : Result.NEUTRAL;
    }

    public final Filter.Result getOnMatch() {
        return Filter.Result.NEUTRAL;
    }

    public final Filter.Result getOnMismatch() {
        return Filter.Result.NEUTRAL;
    }
}
