package at.htlleonding.leomail.repositories;

import at.htlleonding.leomail.entities.*;
import at.htlleonding.leomail.model.SMTPInformation;
import at.htlleonding.leomail.services.GroupSplitter;
import at.htlleonding.leomail.services.TemplateBuilder;
import at.htlleonding.leomail.services.EncryptionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

@ApplicationScoped
public class MailRepository {

    private static final Logger LOGGER = Logger.getLogger(MailRepository.class);

    @Inject
    TemplateBuilder templateBuilder;

    @Inject
    GroupSplitter groupSplitter;

    @Inject
    EncryptionService encryptionService;

    @Inject
    UserRepository userRepository;

    /**
     * Sendet E-Mails basierend auf einer Vorlage und SMTP-Informationen.
     *
     * @param projectId        Die Projekt-ID.
     * @param accountId        Die Benutzer-ID (E-Mail).
     * @param smtpInformation  Die SMTP-Informationen.
     */
    @Transactional
    public void sendMailsByTemplate(String projectId, String accountId, SMTPInformation smtpInformation) {
        LOGGER.infof("Starting to send mails by template. Project ID: %s, Account ID: %s, Scheduled At: %s",
                projectId, accountId, smtpInformation.scheduledAt());

        if (smtpInformation.scheduledAt() != null && smtpInformation.scheduledAt().isBefore(LocalDateTime.now())) {
            LOGGER.error("Scheduled time is in the past.");
            throw new IllegalArgumentException("Scheduled time is in the past");
        }

        if (projectId == null || projectId.trim().isEmpty()) {
            LOGGER.error("Project ID is null or empty.");
            throw new IllegalArgumentException("Project ID is required");
        }

        if (accountId == null || accountId.trim().isEmpty()) {
            LOGGER.error("Account ID is null or empty.");
            throw new IllegalArgumentException("Account ID is required");
        }

        Template template = Template.findById(smtpInformation.templateId());
        if (template == null) {
            LOGGER.errorf("Template with ID %s not found.", smtpInformation.templateId());
            throw new IllegalArgumentException("Template not found");
        }

        List<Contact> receivers = groupSplitter.getAllContacts(
                smtpInformation.receiver().groups().orElse(List.of()),
                smtpInformation.receiver().contacts().orElse(List.of())
        );

        LOGGER.infof("Total Receivers: %d, Valid Receivers with Email: %d",
                receivers.size(), receivers.size());

        if (receivers.isEmpty()) {
            LOGGER.error("No valid receivers with email addresses found.");
            throw new IllegalArgumentException("No valid receivers with email addresses found");
        }

        List<String> renderedTemplates = templateBuilder.renderTemplates(template.id, receivers, smtpInformation.personalized());
        NaturalContact sender = NaturalContact.find("id", accountId).firstResult();

        if (sender == null) {
            LOGGER.errorf("Sender with email %s not found.", accountId);
            throw new IllegalArgumentException("Sender not found");
        }

        SentTemplate usedTemplate = new SentTemplate(template, smtpInformation.scheduledAt(), Project.findById(projectId), sender);

        for (int i = 0; i < renderedTemplates.size(); i++) {
            SentMail sentMail = new SentMail(receivers.get(i), usedTemplate, renderedTemplates.get(i));
            usedTemplate.mails.add(sentMail);
            LOGGER.debugf("Created SentMail for Contact ID %s: %s", sentMail.contact.id, sentMail.actualContent);
        }

        usedTemplate.persist();

        if (smtpInformation.scheduledAt() == null) {
            sendMail(usedTemplate.id);
        }

        LOGGER.info("MailsByTemplate process completed successfully.");
    }

    /**
     * Sendet die Mails, die mit einem SentTemplate verbunden sind, anhand der Benutzer-SMTP-Daten.
     *
     * @param id Die ID des SentTemplate.
     */
    @Transactional
    public void sendMail(Long id) {
        LOGGER.infof("Sending mails for SentTemplate ID: %d", id);
        SentTemplate usedTemplate = SentTemplate.findById(id);

        if (usedTemplate == null) {
            LOGGER.errorf("SentTemplate with ID %d not found.", id);
            throw new IllegalArgumentException("SentTemplate not found");
        }

        if (usedTemplate.mails.isEmpty()) {
            LOGGER.errorf("No mails to send for SentTemplate ID: %d", id);
            throw new IllegalArgumentException("No mails to send");
        }

        if (usedTemplate.sentOn != null) {
            LOGGER.errorf("Template already sent on %s.", usedTemplate.sentOn);
            throw new IllegalArgumentException("Template already sent");
        }

        usedTemplate.sentOn = LocalDateTime.now();
        sendMail(usedTemplate.mails, usedTemplate.sentBy);
        LOGGER.infof("Mails for SentTemplate ID %d sent successfully.", id);
    }

    /**
     * Sendet eine Liste von SentMail-Objekten unter Verwendung der individuellen SMTP-Daten des Benutzers.
     *
     * @param sentMails Die Liste der zu sendenden Mails.
     * @param sender    Der Absender (NaturalContact).
     */
    @Transactional
    public void sendMail(List<SentMail> sentMails, Contact sender) {
        LOGGER.infof("Initiating sending of %d mails.", sentMails.size());

        for (SentMail sentMail : sentMails) {
            try {
                String senderEmail = sender instanceof NaturalContact ? ((NaturalContact) sender).mailAddress : ((CompanyContact) sender).mailAddress;
                String senderPassword = sender instanceof NaturalContact ? ((NaturalContact) sender).encryptedOutlookPassword : null;

                if (senderPassword == null) {
                    LOGGER.errorf("User %s hat kein Outlook-Passwort gespeichert.", senderEmail);
                    continue; // Überspringe, wenn kein Passwort gespeichert ist
                }

                String decryptedPassword = encryptionService.decrypt(senderPassword);

                if(sentMail.contact instanceof NaturalContact contact) {
                    sendEmail(senderEmail, decryptedPassword, contact.mailAddress, sentMail.usedTemplate.headline, sentMail.actualContent);
                } else if(sentMail.contact instanceof CompanyContact contact) {
                    sendEmail(senderEmail, decryptedPassword, contact.mailAddress, sentMail.usedTemplate.headline, sentMail.actualContent);
                }
                sentMail.sent = true;
            } catch (Exception e) {
                LOGGER.errorf("Error sending email ");
            }
        }
    }

    /**
     * Hilfsmethode zum Senden einer einzelnen E-Mail über Outlook SMTP.
     *
     * @param fromEmail Der Absender.
     * @param password  Das Passwort des Absenders.
     * @param toEmail   Der Empfänger.
     * @param subject   Der Betreff der E-Mail.
     * @param content   Der Inhalt der E-Mail.
     * @throws Exception Wenn beim Senden ein Fehler auftritt.
     */
    private void sendEmail(String fromEmail, String password, String toEmail, String subject, String content) throws Exception {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp-mail.outlook.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail.replace("students.htl-leonding.ac.at", "htblaleonding.onmicrosoft.com"), password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject(subject);
        message.setText(content);
        message.setHeader("Content-Type", "text/html");

        Transport.send(message);
    }

    /**
     * Holt die E-Mail-Adresse aus einem Kontakt.
     *
     * @param contact Der Kontakt.
     * @return Die E-Mail-Adresse oder null, wenn nicht vorhanden.
     */
    private String getMailAddress(Contact contact) {
        if (contact instanceof NaturalContact) {
            return ((NaturalContact) contact).mailAddress;
        } else if (contact instanceof CompanyContact) {
            return ((CompanyContact) contact).mailAddress;
        } else {
            return null;
        }
    }

    /**
     * Holt alle ungesendeten Mails.
     *
     * @return Eine Liste von ungesendeten Mails.
     */
    public List<SentMail> getAllUnsentMails() {
        return SentMail.list("sent = false");
    }
}