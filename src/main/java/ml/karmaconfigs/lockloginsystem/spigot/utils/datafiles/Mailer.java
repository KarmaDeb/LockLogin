package ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles;

import ml.karmaconfigs.lockloginsystem.spigot.utils.files.FileManager;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;

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
                .replace("{server}", SpigotFiles.config.ServerName())
                .replace("{player}", name);
    }

    public final String getLoginLog(final String name) {
        return manager.getString("Subjects.LoginLog")
                .replace("{server}", SpigotFiles.config.ServerName())
                .replace("{player}", name);
    }
}
