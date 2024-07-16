package at.htlleonding.leomail.repositories;

import at.htlleonding.leomail.entities.Account;
import at.htlleonding.leomail.entities.Template;
import at.htlleonding.leomail.entities.TemplateGreeting;
import at.htlleonding.leomail.model.dto.TemplateDTO;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class TemplateRepository {

    @Inject
    EntityManager em;

    public List<TemplateDTO> getAllTemplates() {
        return em.createQuery("select t from Template t", Template.class)
                .getResultList()
                .stream()
                .map(template -> new TemplateDTO(template.id, template.name, template.headline, template.content, template.greeting.id, template.createdBy.userName)).toList();
    }

    public Set<TemplateGreeting> getAllGreetings() {
        return new HashSet<>(TemplateGreeting.listAll(Sort.descending("id")));
    }

    @Transactional
    public TemplateDTO addTemplate(TemplateDTO templateDTO) {
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
        return templateDTO;
    }

    @Transactional
    public void deleteById(Long id) {
        if(!Template.deleteById(id)) {
            throw new IllegalArgumentException("Template with id " + id + " not found");
        }
    }

    @Transactional
    public TemplateDTO updateTemplate(TemplateDTO templateDTO) {
        Template template = Template.findById(templateDTO.id());
        if (template == null) {
            throw new IllegalArgumentException("Template with id " + templateDTO.id() + " not found");
        }
        deleteById(templateDTO.id());
        template = new Template(templateDTO.id(), templateDTO.name(), templateDTO.headline(), templateDTO.content(), Account.find("userName", templateDTO.accountName()).firstResult(), TemplateGreeting.findById(templateDTO.greeting()));
        em.merge(template);
        return templateDTO;
    }
}
