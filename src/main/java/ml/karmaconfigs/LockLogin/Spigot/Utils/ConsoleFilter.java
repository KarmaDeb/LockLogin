package ml.karmaconfigs.LockLogin.Spigot.Utils;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.Message;

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

    public final Filter.Result filter(LogEvent record) {
        try {
            if (record != null && record.getMessage() != null) {
                String npe = record.getMessage().getFormattedMessage().toLowerCase();
                return !npe.contains("issued server command:") ? Filter.Result.NEUTRAL : ((
                        !npe.contains("/login ")
                                && !npe.contains("/register ")
                                && !npe.contains("/2fa ")
                                && !npe.contains("/resetfa ")
                                && !npe.contains("/change ")
                                && !npe.contains("/delaccount ")
                                && !npe.contains("/unlog ")
                                && !npe.contains("/reg ")
                                && !npe.contains("/l ")
                                && !npe.contains("/cpass ")
                                && !npe.contains("/pin")
                                && !npe.contains("/resetpin")
                                && !npe.contains("/rpin")
                                && !npe.contains("/delpin")
                                && !npe.contains("/locklogin:login ")
                                && !npe.contains("/locklogin:register ")
                                && !npe.contains("/locklogin:2fa ")
                                && !npe.contains("/locklogin:resetfa ")
                                && !npe.contains("/locklogin:change ")
                                && !npe.contains("/locklogin:delaccount ")
                                && !npe.contains("/locklogin:unlog ")
                                && !npe.contains("/locklogin:reg ")
                                && !npe.contains("/locklogin:l ")
                                && !npe.contains("/locklogin:cpass ")
                                && !npe.contains("/locklogin:pin")
                                && !npe.contains("/locklogin:resetpin")
                                && !npe.contains("/locklogin:rpin")
                                && !npe.contains("/locklogin:delpin")) ? Filter.Result.NEUTRAL : Filter.Result.DENY);
            } else {
                return Filter.Result.NEUTRAL;
            }
        } catch (NullPointerException var3) {
            return Filter.Result.NEUTRAL;
        }
    }

    public final Filter.Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object... arg4) {
        try {
            if (message == null) {
                return Filter.Result.NEUTRAL;
            } else {
                String npe = message.toLowerCase();
                return !npe.contains("issued server command:") ? Filter.Result.NEUTRAL : ((
                        !npe.contains("/login ")
                                && !npe.contains("/register ")
                                && !npe.contains("/2fa ")
                                && !npe.contains("/resetfa ")
                                && !npe.contains("/change ")
                                && !npe.contains("/delaccount ")
                                && !npe.contains("/unlog ")
                                && !npe.contains("/reg ")
                                && !npe.contains("/l ")
                                && !npe.contains("/cpass ")
                                && !npe.contains("/pin")
                                && !npe.contains("/resetpin")
                                && !npe.contains("/rpin")
                                && !npe.contains("/delpin")
                                && !npe.contains("/locklogin:login ")
                                && !npe.contains("/locklogin:register ")
                                && !npe.contains("/locklogin:2fa ")
                                && !npe.contains("/locklogin:resetfa ")
                                && !npe.contains("/locklogin:change ")
                                && !npe.contains("/locklogin:delaccount ")
                                && !npe.contains("/locklogin:unlog ")
                                && !npe.contains("/locklogin:reg ")
                                && !npe.contains("/locklogin:l ")
                                && !npe.contains("/locklogin:cpass ")
                                && !npe.contains("/locklogin:pin")
                                && !npe.contains("/locklogin:resetpin")
                                && !npe.contains("/locklogin:rpin")
                                && !npe.contains("/locklogin:delpin")) ? Filter.Result.NEUTRAL : Filter.Result.DENY);
            }
        } catch (NullPointerException var7) {
            return Filter.Result.NEUTRAL;
        }
    }

    public final Filter.Result filter(Logger arg0, Level arg1, Marker arg2, Object message, Throwable arg4) {
        try {
            if (message == null) {
                return Filter.Result.NEUTRAL;
            } else {
                String npe = message.toString().toLowerCase();
                return !npe.contains("issued server command:") ? Filter.Result.NEUTRAL : ((
                        !npe.contains("/login ")
                                && !npe.contains("/register ")
                                && !npe.contains("/2fa ")
                                && !npe.contains("/resetfa ")
                                && !npe.contains("/change ")
                                && !npe.contains("/delaccount ")
                                && !npe.contains("/unlog ")
                                && !npe.contains("/reg ")
                                && !npe.contains("/l ")
                                && !npe.contains("/cpass ")
                                && !npe.contains("/pin")
                                && !npe.contains("/resetpin")
                                && !npe.contains("/rpin")
                                && !npe.contains("/delpin")
                                && !npe.contains("/locklogin:login ")
                                && !npe.contains("/locklogin:register ")
                                && !npe.contains("/locklogin:2fa ")
                                && !npe.contains("/locklogin:resetfa ")
                                && !npe.contains("/locklogin:change ")
                                && !npe.contains("/locklogin:delaccount ")
                                && !npe.contains("/locklogin:unlog ")
                                && !npe.contains("/locklogin:reg ")
                                && !npe.contains("/locklogin:l ")
                                && !npe.contains("/locklogin:cpass ")
                                && !npe.contains("/locklogin:pin")
                                && !npe.contains("/locklogin:resetpin")
                                && !npe.contains("/locklogin:rpin")
                                && !npe.contains("/locklogin:delpin")) ? Filter.Result.NEUTRAL : Filter.Result.DENY);
            }
        } catch (NullPointerException var7) {
            return Filter.Result.NEUTRAL;
        }
    }

    public final Filter.Result filter(Logger arg0, Level arg1, Marker arg2, Message message, Throwable arg4) {
        try {
            if (message == null) {
                return Filter.Result.NEUTRAL;
            } else {
                String npe = message.getFormattedMessage().toLowerCase();
                return !npe.contains("issued server command:") ? Filter.Result.NEUTRAL : ((
                        !npe.contains("/login ")
                                && !npe.contains("/register ")
                                && !npe.contains("/2fa ")
                                && !npe.contains("/resetfa ")
                                && !npe.contains("/change ")
                                && !npe.contains("/delaccount ")
                                && !npe.contains("/unlog ")
                                && !npe.contains("/reg ")
                                && !npe.contains("/l ")
                                && !npe.contains("/cpass ")
                                && !npe.contains("/pin")
                                && !npe.contains("/resetpin")
                                && !npe.contains("/rpin")
                                && !npe.contains("/delpin")
                                && !npe.contains("/locklogin:login ")
                                && !npe.contains("/locklogin:register ")
                                && !npe.contains("/locklogin:2fa ")
                                && !npe.contains("/locklogin:resetfa ")
                                && !npe.contains("/locklogin:change ")
                                && !npe.contains("/locklogin:delaccount ")
                                && !npe.contains("/locklogin:unlog ")
                                && !npe.contains("/locklogin:reg ")
                                && !npe.contains("/locklogin:l ")
                                && !npe.contains("/locklogin:cpass ")
                                && !npe.contains("/locklogin:pin")
                                && !npe.contains("/locklogin:resetpin")
                                && !npe.contains("/locklogin:rpin")
                                && !npe.contains("/locklogin:delpin")) ? Filter.Result.NEUTRAL : Filter.Result.DENY);
            }
        } catch (NullPointerException var7) {
            return Filter.Result.NEUTRAL;
        }
    }

    public final Filter.Result getOnMatch() {
        return Filter.Result.NEUTRAL;
    }

    public final Filter.Result getOnMismatch() {
        return Filter.Result.NEUTRAL;
    }
}
