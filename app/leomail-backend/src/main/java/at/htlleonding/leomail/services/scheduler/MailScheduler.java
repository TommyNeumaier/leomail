package at.htlleonding.leomail.services.scheduler;

import at.htlleonding.leomail.entities.SentMail;
import at.htlleonding.leomail.entities.SentTemplate;
import at.htlleonding.leomail.repositories.MailRepository;
import io.quarkus.scheduler.Scheduled;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class MailScheduler {

    @Inject
    MailRepository mailRepository;

    private static final Logger LOGGER = Logger.getLogger(MailScheduler.class);

    @PostConstruct
    void init() {
        LOGGER.info("Initializing MailScheduler...");
        checkScheduledTemplates(); // Initial check at application startup
    }

    /**
     * Scheduled task to check and send emails for any scheduled templates.
     * This runs every 15 seconds and sends any emails that are due for sending.
     */
    @Scheduled(every = "15s")
    @Transactional
    public void checkScheduledTemplates() {
        LOGGER.info("Checking for scheduled email templates...");

        List<SentTemplate> usedTemplates = getAllScheduledUsedTemplates();
        if (!usedTemplates.isEmpty()) {
            LOGGER.infof("Found %d scheduled templates ready for sending.", usedTemplates.size());
        }

        for (SentTemplate usedTemplate : usedTemplates) {
            if ((usedTemplate.scheduledAt.isBefore(java.time.LocalDateTime.now()) || usedTemplate.scheduledAt.isEqual(java.time.LocalDateTime.now())) && usedTemplate.sentOn == null) {
                LOGGER.infof("Sending emails for template ID: %s, scheduled at: %s", usedTemplate.id, usedTemplate.scheduledAt);
                mailRepository.sendMail(usedTemplate.id);
            }
        }
    }

    /**
     * Scheduled task to check and resend any unsent mails.
     * This also runs every 15 seconds and attempts to send any pending/unsent mails.
     */
    @Scheduled(every = "15s")
    @Transactional
    public void checkUnsentMails() {
        LOGGER.info("Checking for unsent mails...");

        List<SentMail> unsentMails = mailRepository.getAllUnsentMails();
        if (!unsentMails.isEmpty()) {
            LOGGER.infof("Found %d unsent mails ready for sending.", unsentMails.size());
        }

        for(SentMail unsentMail : unsentMails) {
            LOGGER.infof("Resending email for mail ID: %s", unsentMail.id);
            mailRepository.sendMail(unsentMail.id);
        }
    }

    /**
     * Retrieves all email templates that are scheduled and ready to be sent.
     *
     * @return List of SentTemplates that are scheduled to be sent but not yet sent.
     */
    private List<SentTemplate> getAllScheduledUsedTemplates() {
        return SentTemplate.find("scheduledAt < ?1 and sentOn is null", java.time.LocalDateTime.now()).list();
    }
}