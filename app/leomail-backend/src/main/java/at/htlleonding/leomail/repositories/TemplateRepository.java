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
import at.htlleonding.leomail.services.PermissionService;
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
// TODO: Permission checks in this class are missing
public class TemplateRepository {

    @Inject
    EntityManager em;

    @Inject
    PermissionService permissionService;

    public List<TemplateDTO> getProjectTemplates(String projectId) {
        return em.createQuery("select t from Template t WHERE lower(:pid) = lower(t.project.id)", Template.class)
                .setParameter("pid", projectId)
                .getResultList()
                .stream()
                .filter(template -> template.getClass() == Template.class)
                .map(template -> new TemplateDTO(template.id, template.name, template.headline, template.content, template.greeting.id, template.createdBy.id, template.project.id)).toList();
    }

    public Set<TemplateGreeting> getAllGreetings() {
        return new HashSet<>(TemplateGreeting.listAll(Sort.descending("id")));
    }

    @Transactional
    public TemplateDTO addTemplate(TemplateDTO templateDTO, String creator) {
        if (Template.find("name", templateDTO.name()).count() > 0) {
            throw new TemplateNameAlreadyExistsException();
        }

        TemplateGreeting greeting = TemplateGreeting.findById(templateDTO.greeting());
        if (greeting == null) {
            throw new NonExistingGreetingException();
        }

        Template template = new Template(templateDTO.name(), templateDTO.headline(), templateDTO.content(), Contact.findById(creator), greeting, templateDTO.projectId());
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
        template.name = templateDTO.name();
        template.headline = templateDTO.headline();
        template.content = templateDTO.content();
        template.greeting = TemplateGreeting.findById(templateDTO.greeting());
        return templateDTO;
    }

    public List<UsedTemplateDTO> getUsedTemplates(boolean scheduled, String pid) {
        List<SentTemplate> usedTemplateList = SentTemplate.list("project.id", pid);
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

    public UsedTemplateDTO getUsedTemplate(Long templateId, String projectId, String accountId) {
        SentTemplate template = SentTemplate.findById(templateId);
        if (template == null) {
            throw new IllegalArgumentException("Template with id " + templateId + " not found");
        }

        if (!permissionService.hasPermission(projectId, accountId)) {
            throw new SecurityException("User has no permission to access this template");
        }

        TemplateAccountInformationDTO accountInformation = new TemplateAccountInformationDTO(template.createdBy.id.trim(), template.sentBy.id.trim());
        TemplateDateInformationDTO dateInformation = new TemplateDateInformationDTO(template.created, template.sentOn, template.scheduledAt);
        TemplateMetaInformationDTO metaInformation = new TemplateMetaInformationDTO(template.name.trim(), template.headline.trim(), template.content.trim(), template.greeting.id);

        List<SentMailDTO> mailList = new ArrayList<>();
        for(SentMail mail : template.mails) {
            mailList.add(new SentMailDTO(new TemplateMailContactInformationDTO(mail.id, mail.contact.firstName.trim(), mail.contact.lastName.trim(), mail.contact.mailAddress.trim()), mail.actualContent.trim()));
        }
        SentMailDTO[] mails = mailList.toArray(new SentMailDTO[0]);

        return new UsedTemplateDTO(template.id, metaInformation, dateInformation, accountInformation, mails);

    }
}
