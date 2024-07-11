package at.htlleonding.leomail.repositories;

import at.htlleonding.leomail.entities.Template;
import at.htlleonding.leomail.entities.TemplateGreeting;
import at.htlleonding.leomail.entities.Account;
import at.htlleonding.leomail.model.dto.TemplateDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class TemplateRepository {

    public Set<Template> getAllTemplates() {
        return new HashSet<>(Template.listAll());
    }

    public Set<TemplateGreeting> getAllGreetings() {
        return new HashSet<>(TemplateGreeting.listAll());
    }

    @Transactional
    public Template addTemplate(TemplateDTO templateDTO) {
        Account account = Account.find("userName", templateDTO.accountName()).firstResult();
        if (account == null) {
            throw new IllegalArgumentException("Account with name " + templateDTO.accountName() + " not found");
        }

        TemplateGreeting greeting = TemplateGreeting.findById(templateDTO.greeting());
        if (greeting == null) {
            throw new IllegalArgumentException("Greeting with id " + templateDTO.greeting() + " not found");
        }

        Template template = new Template(templateDTO.name(), templateDTO.headline(), templateDTO.content(), account, greeting);
        template.persist();
        return template;
    }

    public Object sendMail(Long templateId, ) {
        return null;
    }
}
