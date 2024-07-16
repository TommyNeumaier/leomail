package at.htlleonding.leomail.repositories;

import at.htlleonding.leomail.entities.Contact;
import at.htlleonding.leomail.entities.Group;
import at.htlleonding.leomail.entities.Template;
import at.htlleonding.leomail.model.SMTPInformation;
import at.htlleonding.leomail.services.GroupSplitter;
import at.htlleonding.leomail.services.TemplateBuilder;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.qute.EngineConfiguration;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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
        for (int i = 0; i < renderedTemplates.size(); i++) {
            sendMail(receivers.get(i).mailAddress, template.headline, renderedTemplates.get(i));
        }
    }

    private void sendMail(String recipient, String headline, String content) {
        mailer.send(Mail.withHtml(recipient, headline, content));
    }
}
