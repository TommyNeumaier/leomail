package at.htlleonding.leomail.repositories;

import at.htlleonding.leomail.entities.*;
import at.htlleonding.leomail.model.SMTPInformation;
import at.htlleonding.leomail.services.GroupSplitter;
import at.htlleonding.leomail.services.TemplateBuilder;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.qute.EngineConfiguration;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jose4j.jwk.Use;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@ApplicationScoped
public class MailRepository {

    @Inject
    TemplateBuilder templateBuilder;

    @Inject
    Mailer mailer;

    public void sendMailsByTemplate(SMTPInformation smtpInformation) {
        Template template = Template.findById(smtpInformation.templateId());
        List<Contact> receivers = GroupSplitter.getAllContacts(smtpInformation.receiver().groups().get(), smtpInformation.receiver().contacts().get());

        List<String> renderedTemplates = templateBuilder.renderTemplates(template.id, receivers, smtpInformation.personalized());
        UsedTemplate usedTemplate = new UsedTemplate(template, LocalDateTime.now(), Account.findById("IT200274"));
        UsedTemplate.persist(usedTemplate);
        for (int i = 0; i < renderedTemplates.size(); i++) {
            sendMail(receivers.get(i).mailAddress, template.headline, renderedTemplates.get(i), usedTemplate);
        }
    }

    private void sendMail(String recipient, String headline, String content, UsedTemplate usedTemplate) {
        mailer.send(Mail.withHtml(recipient, headline, content));
        SentMail sentMail = new SentMail(Contact.find("mailAddress", recipient).firstResult(), usedTemplate, content);
        SentMail.persist(sentMail);
    }
}
