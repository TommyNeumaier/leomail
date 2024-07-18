package at.htlleonding.leomail.services.scheduler;

import at.htlleonding.leomail.entities.SentMail;
import at.htlleonding.leomail.entities.UsedTemplate;
import at.htlleonding.leomail.repositories.MailRepository;
import io.quarkus.scheduler.Scheduled;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class MailScheduler {

    @Inject
    MailRepository mailRepository;

    @PostConstruct
    void init() {
        checkScheduledTemplates();
    }

    @Scheduled(every = "15s")
    @Transactional
    public void checkScheduledTemplates() {
        List<UsedTemplate> usedTemplates = getAllScheduledUsedTemplates();
        for (UsedTemplate usedTemplate : usedTemplates) {
            if ((usedTemplate.scheduledAt.isBefore(java.time.LocalDateTime.now()) || usedTemplate.scheduledAt.isEqual(java.time.LocalDateTime.now()) && usedTemplate.sentOn == null)) {
                mailRepository.sendMail(usedTemplate.id);
            }
        }
    }

    @Scheduled(every = "15s")
    @Transactional
    public void checkUnsentMails() {
        List<SentMail> sentMails = mailRepository.getAllUnsentMails();
        for (SentMail sentMail : sentMails) {
            mailRepository.sendMail(sentMail);
        }
    }


    private List<UsedTemplate> getAllScheduledUsedTemplates() {
        return UsedTemplate.find("scheduledAt < ?1 and sentOn is null", java.time.LocalDateTime.now()).list();
    }
}
