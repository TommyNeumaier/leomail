package at.htlleonding.leomail.repositories;

import at.htlleonding.leomail.entities.Template;
import at.htlleonding.leomail.entities.TemplateGreeting;
import at.htlleonding.leomail.model.dto.TemplateDTO;
import jakarta.enterprise.context.ApplicationScoped;

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

    public Template addTemplate(TemplateDTO templateDTO) {
        var template = new Template(templateDTO.name(), templateDTO.headline(), templateDTO.content(), templateDTO.accountName());
        Template.persist(template);
        return template;
    }
}
