package at.htlleonding.leomail.services;

import at.htlleonding.leomail.entities.UsedTemplate;
import at.htlleonding.leomail.repositories.MailRepository;
import at.htlleonding.leomail.repositories.TemplateRepository;
import io.quarkus.scheduler.Scheduled;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jose4j.jwk.Use;

import java.util.List;

@ApplicationScoped
public class MailScheduler {

    @Inject
    MailRepository mailRepository;

    @PostConstruct
    void init() {
        checkScheduledMails();
    }

    @Scheduled(every = "15s")
    @Transactional
    public void checkScheduledMails() {
        List<UsedTemplate> usedTemplates = getAllScheduledUsedTemplates();
        for (UsedTemplate usedTemplate : usedTemplates) {
            if (usedTemplate.scheduledAt.isBefore(java.time.LocalDateTime.now()) || usedTemplate.scheduledAt.isEqual(java.time.LocalDateTime.now())) {
                mailRepository.sendMail(usedTemplate.id);
            }
        }
    }

    private List<UsedTemplate> getAllScheduledUsedTemplates() {
        return UsedTemplate.find("scheduledAt < ?1 and sentOn is null", java.time.LocalDateTime.now()).list();
    }
}