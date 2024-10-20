package at.htlleonding.leomail.repositories;

import at.htlleonding.leomail.entities.Contact;
import at.htlleonding.leomail.entities.NaturalContact;
import at.htlleonding.leomail.entities.Project;
import at.htlleonding.leomail.model.dto.contacts.NaturalContactSearchDTO;
import at.htlleonding.leomail.model.dto.project.MailAddressDTO;
import at.htlleonding.leomail.model.dto.project.ProjectAddDTO;
import at.htlleonding.leomail.model.dto.project.ProjectDetailDTO;
import at.htlleonding.leomail.model.dto.project.ProjectOverviewDTO;
import at.htlleonding.leomail.model.exceptions.ObjectContainsNullAttributesException;
import at.htlleonding.leomail.services.MailService;
import at.htlleonding.leomail.services.Utilities;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProjectRepository {

    @Inject
    EntityManager em;

    @Inject
    MailService mailService;

    /**
     * Retrieves personal projects for a given contact ID.
     *
     * @param contactId The ID of the contact.
     * @return A list of ProjectOverviewDTO.
     */
    public List<ProjectOverviewDTO> getPersonalProjects(String contactId) {
        return em.createQuery(
                        "SELECT NEW at.htlleonding.leomail.model.dto.project.ProjectOverviewDTO(p.id, p.name) " +
                                "FROM Project p JOIN p.members m WHERE m.id = :contactId", ProjectOverviewDTO.class)
                .setParameter("contactId", contactId)
                .getResultList();
    }

    /**
     * Adds a new project to the database.
     *
     * @param projectAddDTO The DTO containing project details.
     * @param creatorId     The ID of the creator contact.
     */
    @Transactional
    public void addProject(ProjectAddDTO projectAddDTO, String creatorId) {
        if (projectAddDTO == null) {
            throw new ObjectContainsNullAttributesException(List.of("**ALL NULL**"));
        }

        // Validate mandatory fields
        List<String> nullFields = Utilities.listNullFields(projectAddDTO, List.of("id", "description"));
        if (!nullFields.isEmpty()) {
            throw new ObjectContainsNullAttributesException(nullFields);
        }

        if (projectAddDTO.mailInformation() == null) {
            throw new IllegalArgumentException("Mail information must not be null.");
        }

        if (projectAddDTO.members() == null || projectAddDTO.members().isEmpty()) {
            throw new IllegalArgumentException("Project must have at least one member.");
        }

        // Optional Mail validation
        if (!mailService.verifyOutlookCredentials(
                projectAddDTO.mailInformation().mailAddress(),
                projectAddDTO.mailInformation().password())) {
            throw new IllegalArgumentException("Outlook credentials are invalid");
        }

        // Collect member IDs
        List<String> memberIds = projectAddDTO.members().stream()
                .map(NaturalContactSearchDTO::id)
                .collect(Collectors.toList());

        // Fetch existing contacts
        List<Contact> existingContacts = em.createQuery("SELECT c FROM Contact c WHERE c.id IN :ids", Contact.class)
                .setParameter("ids", memberIds)
                .getResultList();

        Map<String, Contact> existingContactsMap = existingContacts.stream()
                .collect(Collectors.toMap(Contact::getId, c -> c));

        List<NaturalContact> members = new ArrayList<>();

        for (NaturalContactSearchDTO memberDTO : projectAddDTO.members()) {
            Contact contact = existingContactsMap.get(memberDTO.id());
            if (contact == null) {
                throw new IllegalArgumentException("Contact with ID " + memberDTO.id() + " does not exist.");
            }
            if (!(contact instanceof NaturalContact)) {
                throw new IllegalArgumentException("Contact with ID " + memberDTO.id() + " is not a natural contact.");
            }
            NaturalContact naturalContact = (NaturalContact) contact;
            if (!naturalContact.kcUser) {
                throw new IllegalArgumentException("Contact with ID " + memberDTO.id() + " is not a Keycloak user.");
            }
            members.add(naturalContact);
        }

        // Fetch the creator contact
        Contact creatorContact = em.find(Contact.class, creatorId);
        if (creatorContact == null) {
            throw new IllegalArgumentException("Creator contact does not exist.");
        }
        if (!(creatorContact instanceof NaturalContact)) {
            throw new IllegalArgumentException("Creator contact is not a natural contact.");
        }
        NaturalContact naturalCreator = (NaturalContact) creatorContact;
        if (!naturalCreator.kcUser) {
            throw new IllegalArgumentException("Creator contact is not a Keycloak user.");
        }

        // Ensure the creator is in the members list
        if (!members.contains(naturalCreator)) {
            members.add(naturalCreator);
        }

        // Create and persist the new project
        Project project = new Project(
                projectAddDTO.name(),
                projectAddDTO.description(),
                naturalCreator,
                projectAddDTO.mailInformation().mailAddress(),
                projectAddDTO.mailInformation().password(),
                new ArrayList<>(members)
        );
        em.persist(project);
    }

    /**
     * Retrieves the name of a project by its ID.
     *
     * @param pid The project ID.
     * @return The name of the project.
     */
    public String getProjectName(String pid) {
        return em.createQuery("SELECT p.name FROM Project p WHERE p.id = :pid", String.class)
                .setParameter("pid", pid)
                .getSingleResult();
    }

    public ProjectDetailDTO getProject(String pid) {
        Project project = em.find(Project.class, pid);
        if (project == null) {
            throw new IllegalArgumentException("Project with ID " + pid + " does not exist.");
        }

        return new ProjectDetailDTO(
                project.id,
                project.name,
                project.description,
                new MailAddressDTO(project.mailAddress, null),
                project.members.stream()
                        .map(NaturalContactSearchDTO::fromNaturalContact)
                        .collect(Collectors.toList())
        );
    }

    public ProjectDetailDTO updateProject(ProjectDetailDTO projectDetailDTO) {
        if (projectDetailDTO == null) {
            throw new ObjectContainsNullAttributesException(List.of("**ALL NULL**"));
        }

        // Validate mandatory fields
        List<String> nullFields = Utilities.listNullFields(projectDetailDTO, List.of("id", "description"));
        if (!nullFields.isEmpty()) {
            throw new ObjectContainsNullAttributesException(nullFields);
        }

        if (projectDetailDTO.mailInformation() == null) {
            throw new IllegalArgumentException("Mail information must not be null.");
        }

        if (projectDetailDTO.members() == null || projectDetailDTO.members().isEmpty()) {
            throw new IllegalArgumentException("Project must have at least one member.");
        }

        // Optional Mail validation
        if (!mailService.verifyOutlookCredentials(
                projectDetailDTO.mailInformation().mailAddress(),
                projectDetailDTO.mailInformation().password())) {
            throw new IllegalArgumentException("Outlook credentials are invalid");
        }

        // Collect member IDs
        List<String> memberIds = projectDetailDTO.members().stream()
                .map(NaturalContactSearchDTO::id)
                .collect(Collectors.toList());

        // Fetch existing contacts
        List<Contact> existingContacts = em.createQuery("SELECT c FROM Contact c WHERE c.id IN :ids", Contact.class)
                .setParameter("ids", memberIds)
                .getResultList();

        Map<String, Contact> existingContactsMap = existingContacts.stream()
                .collect(Collectors.toMap(Contact::getId, c -> c));

        List<NaturalContact> members = new ArrayList<>();

        for (NaturalContactSearchDTO memberDTO : projectDetailDTO.members()) {
            Contact contact = existingContactsMap.get(memberDTO.id());
            if (contact == null) {
                throw new IllegalArgumentException("Contact with ID " + memberDTO.id() + " does not exist.");
            }
            if (!(contact instanceof NaturalContact)) {
                throw new IllegalArgumentException("Contact with ID " + memberDTO.id() + " is not a natural contact.");
            }
            NaturalContact naturalContact = (NaturalContact) contact;
            if (!naturalContact.kcUser) {
                throw new IllegalArgumentException("Contact with ID " + memberDTO.id() + " is not a Keycloak user.");
            }
            members.add(naturalContact);
        }

        // Fetch the creator contact
        Contact creatorContact = em.find(Contact.class, projectDetailDTO.id());
        if (creatorContact == null) {
            throw new IllegalArgumentException("Creator contact does not exist.");
        }
        if (!(creatorContact instanceof NaturalContact)) {
            throw new IllegalArgumentException("Creator contact is not a natural contact.");
        }

        // Ensure the creator is in the members list
        if (!members.contains(creatorContact)) {
            members.add((NaturalContact) creatorContact);
        }

        Project project = em.find(Project.class, projectDetailDTO.id());
        if (project == null) {
            throw new IllegalArgumentException("Project with ID " + projectDetailDTO.id() + " does not exist.");
        }

        project.name = projectDetailDTO.name();
        project.description = projectDetailDTO.description();
        project.mailAddress = projectDetailDTO.mailInformation().mailAddress();
        project.password = projectDetailDTO.mailInformation().password();
        project.members = new ArrayList<>(members);

        return projectDetailDTO;
    }

    public void deleteProject(String pid) {
        Project project = em.find(Project.class, pid);
        if (project == null) {
            throw new IllegalArgumentException("Project with ID " + pid + " does not exist.");
        }

        em.remove(project);
    }
}