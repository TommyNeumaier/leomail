package at.htlleonding.leomail.repositories;

import at.htlleonding.leomail.entities.*;
import at.htlleonding.leomail.model.dto.TemplateDTO;
import at.htlleonding.leomail.model.dto.UsedTemplateDTO;
import at.htlleonding.leomail.model.dto.template.SentMailDTO;
import at.htlleonding.leomail.model.dto.template.TemplateAccountInformationDTO;
import at.htlleonding.leomail.model.dto.template.TemplateDateInformationDTO;
import at.htlleonding.leomail.model.dto.template.TemplateMetaInformationDTO;
import at.htlleonding.leomail.model.dto.template.mail.TemplateMailContactInformationDTO;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
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
                .map(template -> new TemplateDTO(template.id, template.name, template.headline, template.content, template.greeting.id, template.createdBy.userName)).toList();
    }

    public Set<TemplateGreeting> getAllGreetings() {
        return new HashSet<>(TemplateGreeting.listAll(Sort.descending("id")));
    }

    @Transactional
    public TemplateDTO addTemplate(TemplateDTO templateDTO) {
        try {
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
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while persisting template: " + e.getMessage());
        }

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

    public List<UsedTemplateDTO> getUsedTemplates(boolean scheduled) {
        List<UsedTemplate> usedTemplateList = UsedTemplate.listAll();
        List<UsedTemplateDTO> dtoList = new ArrayList<>();

        for(UsedTemplate template : usedTemplateList) {
            TemplateAccountInformationDTO accountInformation = new TemplateAccountInformationDTO(template.createdBy.userName.trim(), template.sentBy.userName.trim());
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
