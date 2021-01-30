package ml.karmaconfigs.lockloginsystem.bungeecord.commands;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.lockloginmodules.bungee.ModuleLoader;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.datafiles.IPStorager;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.datafiles.Mailer;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.StartCheck;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.User;
import ml.karmaconfigs.lockloginsystem.shared.CheckType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.net.InetSocketAddress;

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

public final class RecoverCommand extends Command implements LockLoginBungee, BungeeFiles {

    public RecoverCommand() {
        super("recovery");
    }

    @Override
    public final void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            User user = new User(player);

            Mailer mailer = new Mailer();
            if (isValidEmailAddress(mailer.getEmail())) {
                if (args.length == 0) {
                    if (isValidEmailAddress(user.getEmail())) {
                        if (!user.isLogged() && !user.isTempLog()) {
                            user.sendRecoverEmail();
                        } else {
                            user.Message(messages.Prefix() + messages.cantRecovery());
                        }
                    } else {
                        user.Message(messages.Prefix() + messages.notEmail());
                    }
                } else {
                    if (args.length == 1) {
                        if (isValidEmailAddress(user.getEmail())) {
                            if (!user.isLogged() && !user.isTempLog()) {
                                if (user.hasPasswordRecovery()) {
                                    String code = args[0];

                                    if (user.validateCode(code)) {
                                        TempModule module = new TempModule();

                                        try {
                                            if (!ModuleLoader.manager.isLoaded(module)) {
                                                ModuleLoader loader = new ModuleLoader(module);
                                                loader.inject();
                                            }

                                            InetSocketAddress ip = player.getAddress();
                                            if (ip != null) {
                                                IPStorager storager = new IPStorager(module, ip.getAddress());
                                                storager.saveLastIP(player.getUniqueId());
                                                user.Message(messages.Prefix() + messages.recoveryValidated());
                                                user.setPassword(null);
                                                user.set2FA(false);
                                                user.removePin();

                                                user.setLogStatus(false);
                                                new StartCheck(player, CheckType.REGISTER);
                                            }
                                        } catch (Throwable ignored) {
                                        }
                                    } else {
                                        user.Message(messages.Prefix() + messages.invalidCode());
                                    }
                                } else {
                                    user.Message(messages.Prefix() + messages.noRecovery());
                                }
                            } else {
                                user.Message(messages.Prefix() + messages.cantRecovery());
                            }
                        } else {
                            user.Message(messages.Prefix() + messages.notEmail());
                        }
                    } else {
                        user.Message(messages.Prefix() + messages.invalidCode());
                    }
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
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    }
}
