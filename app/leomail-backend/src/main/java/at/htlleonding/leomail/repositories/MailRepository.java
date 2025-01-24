package at.htlleonding.leomail.repositories;

import at.htlleonding.leomail.entities.*;
import at.htlleonding.leomail.model.FromMailDTO;
import at.htlleonding.leomail.model.MailType;
import at.htlleonding.leomail.model.SMTPInformation;
import at.htlleonding.leomail.model.SenderCredentials;
import at.htlleonding.leomail.services.*;
import jakarta.activation.DataHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import javax.sql.DataSource;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
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
    MailService mailService;

    @Inject
    AttachmentRepository attachmentRepository;

    @Inject
    StorageService storageService;

    /**
     * Sendet die Mails, die mit einem SentTemplate verbunden sind, anhand der Benutzer-SMTP-Daten.
     *
     * @param id Die ID des SentTemplate.
     */
    @Transactional
    public void sendMail(Long id, SenderCredentials senderCredentials, List<Attachment> attachments) {
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
        sendMail(usedTemplate.mails, senderCredentials, attachments);
        LOGGER.infof("Mails for SentTemplate ID %d sent successfully.", id);
    }

    @Transactional
    public void sendMail(List<SentMail> sentMails, SenderCredentials senderCredentials, List<Attachment> attachments) {
        LOGGER.infof("Initiating sending of %d mails.", sentMails.size());

        for (SentMail sentMail : sentMails) {
            try {
                String recipientEmail = getMailAddress(sentMail.contact);

                if (recipientEmail == null) {
                    LOGGER.errorf("Recipient email is missing for contact ID %s.", sentMail.contact.id);
                    continue;
                }

                sendEmail(senderCredentials.email, senderCredentials.password, recipientEmail,
                        sentMail.usedTemplate.template.headline, sentMail.actualContent, attachments);
                sentMail.sent = true;

            } catch (Exception e) {
                LOGGER.errorf("Error sending email to contact ID %s: %s", sentMail.contact.id, e.getMessage());
            }
        }
    }

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

        // Reconstruct FromMailDTO
        FromMailDTO fromMailDTO = new FromMailDTO(usedTemplate.mailType, usedTemplate.senderId);

        // Get sender's email and password
        SenderCredentials senderCredentials = getSenderCredentials(fromMailDTO, usedTemplate.senderId);

        // Verify the credentials
        boolean credentialsValid = mailService.verifyOutlookCredentials(senderCredentials.email, senderCredentials.password);
        if (!credentialsValid) {
            LOGGER.error("Invalid email credentials.");
            throw new IllegalArgumentException("Invalid email credentials.");
        }

        usedTemplate.sentOn = LocalDateTime.now();
        sendMail(usedTemplate.mails, senderCredentials, usedTemplate.attachments);
        LOGGER.infof("Mails for SentTemplate ID %d sent successfully.", id);
    }



    /**
     * Hilfsmethode zum Senden einer einzelnen E-Mail über Outlook SMTP mit Anhängen.
     *
     * @param fromEmail   Der Absender.
     * @param password    Das Passwort des Absenders.
     * @param toEmail     Der Empfänger.
     * @param subject     Der Betreff der E-Mail.
     * @param content     Der Inhalt der E-Mail (HTML).
     * @param attachments Liste der Anhänge als Attachment Objekte.
     * @throws Exception Wenn beim Senden ein Fehler auftritt.
     */
    private void sendEmail(String fromEmail, String password, String toEmail, String subject, String content, List<Attachment> attachments) throws Exception {
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
        message.setHeader("Content-Type", "text/html");

        // Erstellen des Multipart Objekts
        Multipart multipart = new MimeMultipart();

        // Hinzufügen des Textteils (HTML-Inhalt)
        MimeBodyPart textBodyPart = new MimeBodyPart();
        textBodyPart.setContent(content, "text/html; charset=utf-8");
        multipart.addBodyPart(textBodyPart);

        // Hinzufügen der Anhänge
        for (Attachment attachment : attachments) {
            MimeBodyPart attachmentBodyPart = new MimeBodyPart();
            InputStream is = storageService.downloadFile(attachment.filePath);
            ByteArrayDataSource source = new ByteArrayDataSource(is, attachment.contentType);
            attachmentBodyPart.setDataHandler(new DataHandler(source));
            attachmentBodyPart.setFileName(attachment.fileName);
            multipart.addBodyPart(attachmentBodyPart);
        }

        // Setzen des Multipart Inhalts in die Nachricht
        message.setContent(multipart);

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

    private SenderCredentials getSenderCredentials(FromMailDTO fromMailDTO, String uid) {
        String senderEmail;
        String senderPassword;

        if (fromMailDTO == null) {
            LOGGER.error("From information is missing.");
            throw new IllegalArgumentException("From information is required.");
        }

        MailType mailType = fromMailDTO.mailType();
        String senderId = Objects.equals(fromMailDTO.id(), "") ? uid : fromMailDTO.id();

        if (mailType == MailType.PROJECT) {
            Project project = Project.findById(senderId);
            if (project == null) {
                LOGGER.errorf("Project with ID %s not found.", senderId);
                throw new IllegalArgumentException("Project not found");
            }
            senderEmail = project.mailAddress;
            senderPassword = project.password;

            if (senderEmail == null || senderPassword == null) {
                LOGGER.error("Project email address or password is missing.");
                throw new IllegalArgumentException("Project email address or password is missing.");
            }

            senderPassword = encryptionService.decrypt(senderPassword);
        } else if (mailType == MailType.PERSONAL) {
            NaturalContact sender = NaturalContact.findById(uid);
            if (sender == null) {
                LOGGER.errorf("User with ID %s not found.", senderId);
                throw new IllegalArgumentException("User not found");
            }
            senderEmail = sender.mailAddress;
            senderPassword = sender.encryptedOutlookPassword;

            if (senderEmail == null || senderPassword == null) {
                LOGGER.error("User email address or password is missing.");
                throw new IllegalArgumentException("User email address or password is missing.");
            }

            senderPassword = encryptionService.decrypt(senderPassword);
        } else {
            LOGGER.error("Invalid mail type.");
            throw new IllegalArgumentException("Invalid mail type.");
        }

        return new SenderCredentials(senderEmail, senderPassword);
    }

    /**
     * Holt alle ungesendeten Mails.
     *
     * @return Eine Liste von ungesendeten Mails.
     */
    public List<SentMail> getAllUnsentMails() {
        return SentMail.list("sent = false");
    }

    /**
     * Sendet E-Mails basierend auf einer Vorlage, SMTP-Informationen und Anhängen.
     *
     * @param projectId    Die Projekt-ID.
     * @param accountId    Die Benutzer-ID (E-Mail).
     * @param smtpInformation Die SMTP-Informationen.
     * @param attachments  Liste der Anhänge.
     */
    @Transactional
    public void sendMailsByTemplate(String projectId, String accountId, SMTPInformation smtpInformation, List<Attachment> attachments) {
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
                smtpInformation.receiver().groups(),
                smtpInformation.receiver().contacts()
        );

        LOGGER.infof("Total Receivers: %d", receivers.size());

        if (receivers.isEmpty()) {
            LOGGER.error("No valid receivers with email addresses found.");
            throw new IllegalArgumentException("No valid receivers with email addresses found");
        }

        List<String> renderedTemplates = templateBuilder.renderTemplates(template.id, receivers, smtpInformation.personalized());

        SenderCredentials senderCredentials = getSenderCredentials(smtpInformation.from(), accountId);

        boolean credentialsValid = mailService.verifyOutlookCredentials(senderCredentials.email, senderCredentials.password);
        if (!credentialsValid) {
            LOGGER.error("Invalid email credentials.");
            throw new IllegalArgumentException("Invalid email credentials.");
        }

        SentTemplate usedTemplate = new SentTemplate(
                template,
                smtpInformation.scheduledAt(),
                Project.findById(projectId),
                smtpInformation.from().mailType(),
                smtpInformation.from().id()
        );

        for (Attachment attachment : attachments) {
            attachment.sentTemplate = usedTemplate;
            usedTemplate.attachments.add(attachment);
            attachmentRepository.persist(attachment);
            LOGGER.debugf("Attachment '%s' hinzugefügt zu SentTemplate ID %d.", attachment.fileName, usedTemplate.id);
        }

        for (int i = 0; i < renderedTemplates.size(); i++) {
            SentMail sentMail = new SentMail(receivers.get(i), usedTemplate, renderedTemplates.get(i));
            usedTemplate.mails.add(sentMail);
            LOGGER.debugf("Created SentMail for Contact ID %s: %s", sentMail.contact.id, sentMail.actualContent);
        }

        usedTemplate.persist();

        if (smtpInformation.scheduledAt() == null) {
            sendMail(usedTemplate.id, senderCredentials, usedTemplate.attachments);
        }

        LOGGER.info("MailsByTemplate process completed successfully.");
    }
}