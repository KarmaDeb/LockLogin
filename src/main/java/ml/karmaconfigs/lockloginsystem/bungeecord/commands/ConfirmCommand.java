package ml.karmaconfigs.lockloginsystem.bungeecord.commands;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.lockloginmodules.bungee.ModuleLoader;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.datafiles.IPStorager;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.datafiles.Mailer;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.User;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.net.InetAddress;

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

public final class ConfirmCommand extends Command implements LockLoginBungee, BungeeFiles {

    public ConfirmCommand() {
        super("confirm");
    }

    @Override
    public final void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            User user = new User(player);

            Mailer mailer = new Mailer();

            if (isValidEmailAddress(mailer.getEmail())) {
                if (args.length == 1) {
                    if (isValidEmailAddress(user.getEmail())) {
                        if (user.needsIpValidation()) {
                            String code = args[0];

                            if (user.validateCode(code)) {
                                TempModule module = new TempModule();

                                try {
                                    if (!ModuleLoader.manager.isLoaded(module)) {
                                        ModuleLoader loader = new ModuleLoader(module);
                                        loader.inject();
                                    }

                                    InetAddress ip = user.getIp();
                                    if (ip != null) {
                                        IPStorager storager = new IPStorager(module, ip);
                                        storager.saveLastIP(player.getUniqueId());
                                        user.Message(messages.Prefix() + messages.ipValidated());
                                    }
                                } catch (Throwable ignored) {
                                }
                            } else {
                                user.Message(messages.Prefix() + messages.invalidCode());
                            }
                        } else {
                            user.Message(messages.Prefix() + messages.noIpValidation());
                        }
                    } else {
                        user.Message(messages.Prefix() + messages.notEmail());
                    }
                } else {
                    user.Message(messages.Prefix() + messages.invalidCode());
                }
            } else {
                user.Message(messages.Prefix() + messages.emailDisabled());
            }
        } else {
            Console.send(plugin, "This command is for players only", Level.WARNING);
        }
    }

    private boolean isValidEmailAddress(final String email) {
        boolean result = true;
        try {
            InternetAddress address = new InternetAddress(email);
            address.validate();
        } catch (Throwable ex) {
            result = false;
        }
        return result;
    }
}
