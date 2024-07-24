package at.htlleonding.leomail.repositories;

import at.htlleonding.leomail.entities.*;
import at.htlleonding.leomail.model.dto.TemplateDTO;
import at.htlleonding.leomail.model.dto.UsedTemplateDTO;
import at.htlleonding.leomail.model.dto.template.SentMailDTO;
import at.htlleonding.leomail.model.dto.template.TemplateAccountInformationDTO;
import at.htlleonding.leomail.model.dto.template.TemplateDateInformationDTO;
import at.htlleonding.leomail.model.dto.template.TemplateMetaInformationDTO;
import at.htlleonding.leomail.model.dto.template.mail.TemplateMailContactInformationDTO;
import at.htlleonding.leomail.model.exceptions.account.ContactExistsInKeycloakException;
import at.htlleonding.leomail.model.exceptions.greeting.NonExistingGreetingException;
import at.htlleonding.leomail.model.exceptions.template.TemplateNameAlreadyExistsException;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
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
                .filter(template -> template.getClass() == Template.class)
                .map(template -> new TemplateDTO(template.id, template.name, template.headline, template.content, template.greeting.id, template.createdBy.id)).toList();
    }

    public Set<TemplateGreeting> getAllGreetings() {
        return new HashSet<>(TemplateGreeting.listAll(Sort.descending("id")));
    }

    @Transactional
    public TemplateDTO addTemplate(TemplateDTO templateDTO) {
        if (Template.find("name", templateDTO.name()).count() > 0) {
            throw new TemplateNameAlreadyExistsException();
        }

        Contact account = Contact.find("userName", templateDTO.accountName()).firstResult();
        if (account == null) {
            throw new ContactExistsInKeycloakException();
        }

        TemplateGreeting greeting = TemplateGreeting.findById(templateDTO.greeting());
        if (greeting == null) {
            throw new NonExistingGreetingException();
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
        template = new Template(templateDTO.id(), templateDTO.name(), templateDTO.headline(), templateDTO.content(), Contact.find("userName", templateDTO.accountName()).firstResult(), TemplateGreeting.findById(templateDTO.greeting()));
        em.merge(template);
        return templateDTO;
    }

    public List<UsedTemplateDTO> getUsedTemplates(boolean scheduled) {
        List<SentTemplate> usedTemplateList = SentTemplate.listAll();
        List<UsedTemplateDTO> dtoList = new ArrayList<>();

        for(SentTemplate template : usedTemplateList) {
            TemplateAccountInformationDTO accountInformation = new TemplateAccountInformationDTO(template.createdBy.id.trim(), template.sentBy.id.trim());
            TemplateDateInformationDTO dateInformation = new TemplateDateInformationDTO(template.created, template.sentOn, template.scheduledAt);
            TemplateMetaInformationDTO metaInformation = new TemplateMetaInformationDTO(template.name.trim(), template.headline.trim(), template.content.trim(), template.greeting.id);

            List<SentMailDTO> mailList = new ArrayList<>();
            for(SentMail mail : template.mails) {
                mailList.add(new SentMailDTO(new TemplateMailContactInformationDTO(mail.id, mail.contact.firstName.trim(), mail.contact.lastName.trim(), mail.contact.mailAddress.trim()), mail.actualContent.trim()));
            }
            SentMailDTO[] mails = mailList.toArray(new SentMailDTO[0]);

            dtoList.add(new UsedTemplateDTO(template.id, metaInformation, dateInformation, accountInformation, mails));
        }

        if(scheduled) {
            dtoList.removeIf(template -> template.keyDates().sentOn() != null);
        } else {
            dtoList.removeIf(template -> template.keyDates().scheduledAt() != null);
        }

        return dtoList;
    }
}
