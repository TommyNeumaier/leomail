package at.htlleonding.leomail.repositories;

import at.htlleonding.leomail.entities.*;
import at.htlleonding.leomail.model.SMTPInformation;
import at.htlleonding.leomail.services.GroupSplitter;
import at.htlleonding.leomail.services.TemplateBuilder;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class MailRepository {

    @Inject
    TemplateBuilder templateBuilder;

    @Inject
    Mailer mailer;

    public void sendMailsByTemplate(SMTPInformation smtpInformation) {
        if (smtpInformation.scheduledAt() != null && smtpInformation.scheduledAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Scheduled time is in the past");
        }
        Template template = Template.findById(smtpInformation.templateId());
        List<Contact> receivers = GroupSplitter.getAllContacts(smtpInformation.receiver().groups().get(), smtpInformation.receiver().contacts().get());

        List<String> renderedTemplates = templateBuilder.renderTemplates(template.id, receivers, smtpInformation.personalized());
        UsedTemplate usedTemplate = new UsedTemplate(template, smtpInformation.scheduledAt(), Account.findById("IT200274"));
        for (int i = 0; i < renderedTemplates.size(); i++) {
            SentMail sentMail = new SentMail(receivers.get(i), usedTemplate, renderedTemplates.get(i));
            usedTemplate.mails.add(sentMail);
        }
        UsedTemplate.persist(usedTemplate);

        if(smtpInformation.scheduledAt() == null) {
            sendMail(usedTemplate.id);
        }
    }


    public void sendMail(Long id) {
        UsedTemplate usedTemplate = UsedTemplate.findById(id);

        if (usedTemplate.mails.isEmpty()) {
            throw new IllegalArgumentException("No mails to send");
        }

        if (usedTemplate.sentOn != null) {
            throw new IllegalArgumentException("UsedTemplate already sent");
        }

        usedTemplate.sentOn = LocalDateTime.now();

        System.out.println(usedTemplate.sentOn);
        for (SentMail sentMail : usedTemplate.mails) {
            mailer.send(Mail.withHtml(sentMail.contact.mailAddress, usedTemplate.headline, sentMail.actualContent));
        }
    }
}
