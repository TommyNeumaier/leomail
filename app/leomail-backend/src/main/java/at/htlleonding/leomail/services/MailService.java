package at.htlleonding.leomail.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import java.util.Properties;

@ApplicationScoped
public class MailService {

    /**
     * Verifies the Outlook credentials by attempting to connect to the SMTP server.
     *
     * @param email The user's Outlook email.
     * @param password The user's Outlook password.
     * @return true if the credentials are valid, false otherwise.
     */
    public boolean verifyOutlookCredentials(String email, String password) {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp-mail.outlook.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(properties);

        try {
            Transport transport = session.getTransport("smtp");
            transport.connect("smtp-mail.outlook.com", email, password);
            transport.close();

            return true;
        } catch (MessagingException e) {
            return false;
        }
    }
}