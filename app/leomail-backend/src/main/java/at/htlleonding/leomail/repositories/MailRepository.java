package at.htlleonding.leomail.repositories;

import at.htlleonding.leomail.entities.*;
import at.htlleonding.leomail.model.SMTPInformation;
import at.htlleonding.leomail.services.GroupSplitter;
import at.htlleonding.leomail.services.TemplateBuilder;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class MailRepository {

    private static final Logger LOGGER = Logger.getLogger(MailRepository.class);

    @Inject
    TemplateBuilder templateBuilder;

    @Inject
    Mailer mailer;

    @Inject
    GroupSplitter groupSplitter;

    /**
     * Sends mails based on a template and SMTP information.
     */
    @Transactional
    public void sendMailsByTemplate(String projectId, String accountId, SMTPInformation smtpInformation) {
        LOGGER.infof("Starting to send mails by template. Project ID: %s, Account ID: %s, Scheduled At: %s",
                projectId, accountId, smtpInformation.scheduledAt());

        if (smtpInformation.scheduledAt() != null && smtpInformation.scheduledAt().isBefore(LocalDateTime.now())) {
            LOGGER.error("Scheduled time is in the past.");
            throw new IllegalArgumentException("Scheduled time is in the past");
        }

        Template template = Template.findById(smtpInformation.templateId());
        if (template == null) {
            LOGGER.errorf("Template with ID %d not found.", smtpInformation.templateId());
            throw new IllegalArgumentException("Template not found");
        }

        List<Contact> receivers = groupSplitter.getAllContacts(
                smtpInformation.receiver().groups().orElse(List.of()),
                smtpInformation.receiver().contacts().orElse(List.of())
        );

        // Filter out contacts without a mail address
        List<Contact> validReceivers = receivers.stream()
                .filter(contact -> getMailAddress(contact) != null)
                .collect(Collectors.toList());

        LOGGER.infof("Total Receivers: %d, Valid Receivers with Email: %d",
                receivers.size(), validReceivers.size());

        if (validReceivers.isEmpty()) {
            LOGGER.error("No valid receivers with email addresses found.");
            throw new IllegalArgumentException("No valid receivers with email addresses found");
        }

        List<String> renderedTemplates = templateBuilder.renderTemplates(template.id, validReceivers, smtpInformation.personalized());
        SentTemplate usedTemplate = new SentTemplate(template, smtpInformation.scheduledAt(), Project.findById(projectId), Contact.findById(accountId));

        for (int i = 0; i < renderedTemplates.size(); i++) {
            SentMail sentMail = new SentMail(validReceivers.get(i), usedTemplate, renderedTemplates.get(i));
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
     * Sends the mails associated with a SentTemplate by its ID.
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
        sendMail(usedTemplate.mails);
        LOGGER.infof("Mails for SentTemplate ID %d sent successfully.", id);
    }

    /**
     * Sends a list of SentMail objects.
     */
    @Transactional
    public void sendMail(List<SentMail> sentMails) {
        LOGGER.infof("Initiating sending of %d mails.", sentMails.size());
        Multi.createFrom().iterable(sentMails)
                .onItem()
                .transform(sentMail -> {
                    String mailAddress = getMailAddress(sentMail.contact);
                    if (mailAddress == null) {
                        LOGGER.warnf("Contact with ID %s does not have an email address. Skipping.", sentMail.contact.id);
                        return null;
                    }

                    sentMail.sent = true;
                    LOGGER.debugf("Preparing mail for %s with content: %s", mailAddress, sentMail.actualContent);
                    return Mail.withHtml(mailAddress, sentMail.usedTemplate.headline, sentMail.actualContent);
                })
                .filter(mail -> mail != null)
                .onFailure().invoke(e -> LOGGER.error("Failed to send an email.", e))
                .subscribe().with(mail -> {
                    try {
                        mailer.send(mail);
                        LOGGER.debugf("Mail sent to %s successfully.", mail.getTo());
                    } catch (Exception e) {
                        LOGGER.errorf("Error sending email to %s: %s", mail.getTo(), e.getMessage());
                    }
                });
    }

    /**
     * Retrieves all unsent mails.
     */
    public List<SentMail> getAllUnsentMails() {
        return SentMail.list("sent = false");
    }

    // Helper method to get the mail address from a contact
    private String getMailAddress(Contact contact) {
        if (contact instanceof NaturalContact naturalContact) {
            return naturalContact.mailAddress;
        } else if (contact instanceof CompanyContact companyContact) {
            return companyContact.mailAddress;
        } else {
            return null;
        }
    }
}