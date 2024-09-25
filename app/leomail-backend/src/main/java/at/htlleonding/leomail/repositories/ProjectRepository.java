package at.htlleonding.leomail.repositories;

import at.htlleonding.leomail.entities.Contact;
import at.htlleonding.leomail.entities.Project;
import at.htlleonding.leomail.model.dto.contacts.ContactSearchDTO;
import at.htlleonding.leomail.model.dto.project.ProjectAddDTO;
import at.htlleonding.leomail.model.dto.project.ProjectOverviewDTO;
import at.htlleonding.leomail.model.exceptions.ObjectContainsNullAttributesException;
import at.htlleonding.leomail.services.Utilities;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProjectRepository {

    @Inject
    EntityManager em;

    public List<ProjectOverviewDTO> getPersonalProjects(String contactId) {
        return em.createQuery("SELECT NEW at.htlleonding.leomail.model.dto.project.ProjectOverviewDTO(p.id, p.name) FROM Project p JOIN p.members m WHERE m.id = :contactId", ProjectOverviewDTO.class)
                .setParameter("contactId", contactId)
                .getResultList();
    }

    @Transactional
    public void addProject(ProjectAddDTO projectAddDTO, String creator) {
        if (projectAddDTO == null) {
            throw new ObjectContainsNullAttributesException(List.of("**ALL NULL**"));
        }

        List<String> nullFields = Utilities.listNullFields(projectAddDTO, List.of("id", "description", "mailInformation", "members"));
        if (!nullFields.isEmpty()) {
            throw new ObjectContainsNullAttributesException(nullFields);
        }

        List<Contact> members = em.createQuery("SELECT c FROM Contact c WHERE c.id IN :ids", Contact.class)
                .setParameter("ids", projectAddDTO.members().stream().map(ContactSearchDTO::id).collect(Collectors.toList()))
                .getResultList();

        for (ContactSearchDTO member : projectAddDTO.members()) {
            if(Contact.find("id", member.id()).firstResult() == null) {
                Contact newContact = new Contact(member.id(), member.firstName(), member.lastName(), member.mailAddress(), true);
                em.persist(newContact);
                members.add(newContact);
            }
            else {
                members.add(Contact.find("id", member.id()).firstResult());
            }
        }

        Contact creatorContact = em.find(Contact.class, creator);
        if(creatorContact == null) {
            throw new ObjectContainsNullAttributesException(List.of("creator"));
        }
        members.add(creatorContact);

        Project project = new Project(projectAddDTO.name(), projectAddDTO.description(), creatorContact, projectAddDTO.mailInformation().mailAddress(), projectAddDTO.mailInformation().password(), members);
        em.persist(project);
    }

    public String getProjectName(String pid, String uid) {
        return em.createQuery("SELECT p.name FROM Project p WHERE p.id = :pid AND :uid MEMBER OF p.members", String.class)
                .setParameter("pid", pid)
                .setParameter("uid", uid)
                .getSingleResult();
    }
}