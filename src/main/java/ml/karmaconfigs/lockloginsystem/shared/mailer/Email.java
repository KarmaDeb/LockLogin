package ml.karmaconfigs.lockloginsystem.shared.mailer;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;

public final class Email {

    private static String email_address;
    private static String email_password;
    private static String email_host = "";

    private static int email_port = 565;

    private static boolean isTLS = false;

    /**
     * Initialize the email sender class
     *
     * @param address  the email address to sent emails
     *                 from
     * @param password the email password to login
     */
    public Email(final String address, final String password) {
        email_address = address;
        email_password = password;
    }

    /**
     * Set the mail options
     *
     * @param mail_host the email host to sent from
     * @param mail_port the email port of host
     * @param tls       use TLS instead of SSL
     */
    public final void setOptions(final String mail_host, final int mail_port, final boolean tls) {
        email_host = mail_host;
        email_port = mail_port;
        isTLS = tls;
    }

    /**
     * Send the email
     *
     * @param target  the email target
     * @param subject the email message subject
     * @param html    the email message body
     */
    public final void sendMail(final String target, final String subject, final String player, final String server, final String code, final File html) throws Throwable {
        List<String> message_list = Files.readAllLines(html.toPath(), StandardCharsets.UTF_8);

        StringBuilder message_builder = new StringBuilder();
        for (String str : message_list)
            message_builder.append(str).append("\n");

        String message = message_builder.toString();
        message = message.replace("{user_name}", player);
        message = message.replace("{code}", code);
        message = message.replace("{server}", server);

        Properties properties = new Properties();

        properties.put("mail.smtp.host", email_host);
        properties.put("mail.smtp.port", String.valueOf(email_port));
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", String.valueOf(isTLS));

        Authenticator auth = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email_address, email_password);
            }
        };

        Session session = Session.getInstance(properties, auth);
        MailSender.sendEmail(session, target, subject, message);
    }
}
