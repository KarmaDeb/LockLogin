package ml.karmaconfigs.lockloginsystem.bungeecord.utils.datafiles;

import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.FileManager;

public final class Mailer {

    private final FileManager manager = new FileManager("mail.yml");

    public final String getEmail() {
        return manager.getString("Email");
    }

    public final String getPassword() {
        return manager.getString("Password");
    }

    public final boolean sendLoginEmail() {
        return manager.getBoolean("LoginEmail");
    }

    public final String getHost() {
        return manager.getString("SMTP.Host");
    }

    public final int getPort() {
        return manager.getInt("SMTP.Port");
    }

    public final boolean useTLS() {
        return manager.getBoolean("SMTP.TLS");
    }

    public final String getPasswordSubject(final String name) {
        return manager.getString("Subjects.PasswordRecovery")
                .replace("{server}", BungeeFiles.config.ServerName())
                .replace("{player}", name);
    }

    public final String getLoginLog(final String name) {
        return manager.getString("Subjects.LoginLog")
                .replace("{server}", BungeeFiles.config.ServerName())
                .replace("{player}", name);
    }
}
