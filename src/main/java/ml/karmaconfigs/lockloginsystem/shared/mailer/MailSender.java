package ml.karmaconfigs.lockloginsystem.shared.mailer;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;

public interface MailSender {

    /**
     * Utility method to send simple HTML email
     *
     * @param session the email session
     * @param toEmail the email target
     * @param subject the email message subject
     * @param body    the email message body
     */
    static void sendEmail(Session session, String toEmail, String subject, String body) {
        try {
            MimeMessage msg = new MimeMessage(session);
            msg.addHeader("Content-type", "text/html; charset=UTF-8");
            msg.addHeader("format", "flowed");
            msg.addHeader("Content-Transfer-Encoding", "8bit");

            msg.setFrom(new InternetAddress("no_reply_locklogin@gmail.com", "NoReply-LL"));
            msg.setReplyTo(InternetAddress.parse("no_reply_locklogin@gmail.com", false));
            msg.setSubject(subject, "UTF-8");
            msg.setContent(body, "text/html");
            msg.setSentDate(new Date());

            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));

            Transport.send(msg);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }
}
