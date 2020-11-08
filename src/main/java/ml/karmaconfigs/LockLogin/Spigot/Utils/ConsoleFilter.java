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
                return !npe.toLowerCase().contains("issued server command:") ? Filter.Result.NEUTRAL : ((
                        !npe.toLowerCase().contains("/login ")
                                && !npe.toLowerCase().contains("/register ")
                                && !npe.toLowerCase().contains("/2fa ")
                                && !npe.toLowerCase().contains("/resetfa ")
                                && !npe.toLowerCase().contains("/change ")
                                && !npe.toLowerCase().contains("/delaccount ")
                                && !npe.toLowerCase().contains("/unlog ")
                                && !npe.toLowerCase().contains("/reg ")
                                && !npe.toLowerCase().contains("/l ")
                                && !npe.toLowerCase().contains("/cpass ")
                                && !npe.toLowerCase().contains("/pin")
                                && !npe.toLowerCase().contains("/resetpin")
                                && !npe.toLowerCase().contains("/rpin")
                                && !npe.toLowerCase().contains("/delpin")
                                && !npe.toLowerCase().contains("/locklogin:login ")
                                && !npe.toLowerCase().contains("/locklogin:register ")
                                && !npe.toLowerCase().contains("/locklogin:2fa ")
                                && !npe.toLowerCase().contains("/locklogin:resetfa ")
                                && !npe.toLowerCase().contains("/locklogin:change ")
                                && !npe.toLowerCase().contains("/locklogin:delaccount ")
                                && !npe.toLowerCase().contains("/locklogin:unlog ")
                                && !npe.toLowerCase().contains("/locklogin:reg ")
                                && !npe.toLowerCase().contains("/locklogin:l ")
                                && !npe.toLowerCase().contains("/locklogin:cpass ")
                                && !npe.toLowerCase().contains("/locklogin:pin")
                                && !npe.toLowerCase().contains("/locklogin:resetpin")
                                && !npe.toLowerCase().contains("/locklogin:rpin")
                                && !npe.toLowerCase().contains("/locklogin:delpin")) ? Filter.Result.NEUTRAL : Filter.Result.DENY);
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
                return !npe.toLowerCase().contains("issued server command:") ? Filter.Result.NEUTRAL : ((
                        !npe.toLowerCase().contains("/login ")
                                && !npe.toLowerCase().contains("/register ")
                                && !npe.toLowerCase().contains("/2fa ")
                                && !npe.toLowerCase().contains("/resetfa ")
                                && !npe.toLowerCase().contains("/change ")
                                && !npe.toLowerCase().contains("/delaccount ")
                                && !npe.toLowerCase().contains("/unlog ")
                                && !npe.toLowerCase().contains("/reg ")
                                && !npe.toLowerCase().contains("/l ")
                                && !npe.toLowerCase().contains("/cpass ")
                                && !npe.toLowerCase().contains("/pin")
                                && !npe.toLowerCase().contains("/resetpin")
                                && !npe.toLowerCase().contains("/rpin")
                                && !npe.toLowerCase().contains("/delpin")
                                && !npe.toLowerCase().contains("/locklogin:login ")
                                && !npe.toLowerCase().contains("/locklogin:register ")
                                && !npe.toLowerCase().contains("/locklogin:2fa ")
                                && !npe.toLowerCase().contains("/locklogin:resetfa ")
                                && !npe.toLowerCase().contains("/locklogin:change ")
                                && !npe.toLowerCase().contains("/locklogin:delaccount ")
                                && !npe.toLowerCase().contains("/locklogin:unlog ")
                                && !npe.toLowerCase().contains("/locklogin:reg ")
                                && !npe.toLowerCase().contains("/locklogin:l ")
                                && !npe.toLowerCase().contains("/locklogin:cpass ")
                                && !npe.toLowerCase().contains("/locklogin:pin")
                                && !npe.toLowerCase().contains("/locklogin:resetpin")
                                && !npe.toLowerCase().contains("/locklogin:rpin")
                                && !npe.toLowerCase().contains("/locklogin:delpin")) ? Filter.Result.NEUTRAL : Filter.Result.DENY);
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
                return !npe.toLowerCase().contains("issued server command:") ? Filter.Result.NEUTRAL : ((
                        !npe.toLowerCase().contains("/login ")
                                && !npe.toLowerCase().contains("/register ")
                                && !npe.toLowerCase().contains("/2fa ")
                                && !npe.toLowerCase().contains("/resetfa ")
                                && !npe.toLowerCase().contains("/change ")
                                && !npe.toLowerCase().contains("/delaccount ")
                                && !npe.toLowerCase().contains("/unlog ")
                                && !npe.toLowerCase().contains("/reg ")
                                && !npe.toLowerCase().contains("/l ")
                                && !npe.toLowerCase().contains("/cpass ")
                                && !npe.toLowerCase().contains("/pin")
                                && !npe.toLowerCase().contains("/resetpin")
                                && !npe.toLowerCase().contains("/rpin")
                                && !npe.toLowerCase().contains("/delpin")
                                && !npe.toLowerCase().contains("/locklogin:login ")
                                && !npe.toLowerCase().contains("/locklogin:register ")
                                && !npe.toLowerCase().contains("/locklogin:2fa ")
                                && !npe.toLowerCase().contains("/locklogin:resetfa ")
                                && !npe.toLowerCase().contains("/locklogin:change ")
                                && !npe.toLowerCase().contains("/locklogin:delaccount ")
                                && !npe.toLowerCase().contains("/locklogin:unlog ")
                                && !npe.toLowerCase().contains("/locklogin:reg ")
                                && !npe.toLowerCase().contains("/locklogin:l ")
                                && !npe.toLowerCase().contains("/locklogin:cpass ")
                                && !npe.toLowerCase().contains("/locklogin:pin")
                                && !npe.toLowerCase().contains("/locklogin:resetpin")
                                && !npe.toLowerCase().contains("/locklogin:rpin")
                                && !npe.toLowerCase().contains("/locklogin:delpin")) ? Filter.Result.NEUTRAL : Filter.Result.DENY);
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
                return !npe.toLowerCase().contains("issued server command:") ? Filter.Result.NEUTRAL : ((
                        !npe.toLowerCase().contains("/login ")
                                && !npe.toLowerCase().contains("/register ")
                                && !npe.toLowerCase().contains("/2fa ")
                                && !npe.toLowerCase().contains("/resetfa ")
                                && !npe.toLowerCase().contains("/change ")
                                && !npe.toLowerCase().contains("/delaccount ")
                                && !npe.toLowerCase().contains("/unlog ")
                                && !npe.toLowerCase().contains("/reg ")
                                && !npe.toLowerCase().contains("/l ")
                                && !npe.toLowerCase().contains("/cpass ")
                                && !npe.toLowerCase().contains("/pin")
                                && !npe.toLowerCase().contains("/resetpin")
                                && !npe.toLowerCase().contains("/rpin")
                                && !npe.toLowerCase().contains("/delpin")
                                && !npe.toLowerCase().contains("/locklogin:login ")
                                && !npe.toLowerCase().contains("/locklogin:register ")
                                && !npe.toLowerCase().contains("/locklogin:2fa ")
                                && !npe.toLowerCase().contains("/locklogin:resetfa ")
                                && !npe.toLowerCase().contains("/locklogin:change ")
                                && !npe.toLowerCase().contains("/locklogin:delaccount ")
                                && !npe.toLowerCase().contains("/locklogin:unlog ")
                                && !npe.toLowerCase().contains("/locklogin:reg ")
                                && !npe.toLowerCase().contains("/locklogin:l ")
                                && !npe.toLowerCase().contains("/locklogin:cpass ")
                                && !npe.toLowerCase().contains("/locklogin:pin")
                                && !npe.toLowerCase().contains("/locklogin:resetpin")
                                && !npe.toLowerCase().contains("/locklogin:rpin")
                                && !npe.toLowerCase().contains("/locklogin:delpin")) ? Filter.Result.NEUTRAL : Filter.Result.DENY);
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
