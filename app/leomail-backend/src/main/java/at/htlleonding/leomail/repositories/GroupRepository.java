package at.htlleonding.leomail.repositories;

import at.htlleonding.leomail.entities.Contact;
import at.htlleonding.leomail.entities.Group;
import at.htlleonding.leomail.entities.Project;
import at.htlleonding.leomail.model.dto.contacts.ContactSearchDTO;
import at.htlleonding.leomail.model.dto.contacts.CreatorDTO;
import at.htlleonding.leomail.model.dto.groups.GroupDetailDTO;
import at.htlleonding.leomail.model.dto.groups.GroupOverviewDTO;
import at.htlleonding.leomail.model.exceptions.projects.ProjectNotExistsException;
import at.htlleonding.leomail.services.KeycloakAdminService;
import at.htlleonding.leomail.services.PermissionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class GroupRepository {

    private static final Logger LOGGER = Logger.getLogger(GroupRepository.class);

    @Inject
    EntityManager em;

    @Inject
    KeycloakAdminService keycloakAdminService;

    @Inject
    ContactRepository contactRepository;

    @Inject
    PermissionService permissionService;

    /**
     * Holt eine Übersicht der persönlichen Gruppen eines Benutzers innerhalb eines Projekts.
     *
     * @param projectId Projekt-ID
     * @param accountId Benutzer-ID
     * @return Liste der Gruppenübersichten
     */
    public List<GroupOverviewDTO> getPersonalGroups(String projectId, String accountId) {
        validateProjectAndAccount(projectId, accountId);

        return em.createQuery(
                        "SELECT NEW at.htlleonding.leomail.model.dto.groups.GroupOverviewDTO(g.id, g.name, CONCAT(g.createdBy.firstName, ' ', g.createdBy.lastName)) " +
                                "FROM Group g WHERE g.project.id = :projectId AND g.createdBy.id = :accountId",
                        GroupOverviewDTO.class)
                .setParameter("projectId", projectId)
                .setParameter("accountId", accountId)
                .getResultList();
    }

    /**
     * Holt die Detailinformationen einer bestimmten Gruppe.
     *
     * @param projectId Projekt-ID
     * @param accountId Benutzer-ID
     * @param groupId   Gruppen-ID
     * @return Detailinformationen der Gruppe
     */
    public GroupDetailDTO getGroupDetails(String projectId, String accountId, String groupId) {
        validateProjectAndAccount(projectId, accountId);
        validateGroupExistence(projectId, groupId);

        GroupDetailDTO dto = em.createQuery(
                        "SELECT NEW at.htlleonding.leomail.model.dto.groups.GroupDetailDTO(g.id, g.name, g.description) " +
                                "FROM Group g WHERE g.id = :groupId",
                        GroupDetailDTO.class)
                .setParameter("groupId", groupId)
                .getSingleResult();

        CreatorDTO createdBy = em.createQuery(
                        "SELECT NEW at.htlleonding.leomail.model.dto.contacts.CreatorDTO(g.createdBy.id, g.createdBy.firstName, g.createdBy.lastName, g.createdBy.mailAddress) " +
                                "FROM Group g WHERE g.id = :groupId",
                        CreatorDTO.class)
                .setParameter("groupId", groupId)
                .getSingleResult();

        Group group = Group.findById(groupId);
        List<ContactSearchDTO> members = group.members != null
                ? group.members.stream()
                .map(contact -> new ContactSearchDTO(contact.id, contact.firstName, contact.lastName, contact.mailAddress))
                .toList()
                : Collections.emptyList();

        return new GroupDetailDTO(dto.id(), dto.name(), dto.description(), createdBy, members);
    }

    /**
     * Erstellt eine neue Gruppe innerhalb eines Projekts.
     *
     * @param projectId Projekt-ID
     * @param accountId Benutzer-ID
     * @param description Gruppenbeschreibung
     * @param name        Gruppenname
     * @param members     Liste der Mitglieder als ContactSearchDTO
     */
    @Transactional
    public void createGroup(String projectId, String accountId, String description, String name, List<ContactSearchDTO> members) {
        validateProjectAndAccount(projectId, accountId);
        validateGroupName(name);
        if (members == null) members = List.of();

        List<Contact> memberList = fetchExistingContacts(members);
        addKeycloakUsers(memberList, members);

        Group newGroup = new Group(name, description, Contact.findById(accountId), Project.findById(projectId), memberList);
        em.persist(newGroup);
        LOGGER.infof("Gruppe '%s' erfolgreich erstellt.", name);
    }

    /**
     * Löscht eine bestehende Gruppe.
     *
     * @param projectId Projekt-ID
     * @param accountId Benutzer-ID
     * @param groupId   Gruppen-ID
     */
    @Transactional
    public void deleteGroup(String projectId, String accountId, String groupId) {
        validateProjectAndAccount(projectId, accountId);
        validateGroupId(groupId);

        em.createQuery("DELETE FROM Group g WHERE g.id = :groupId")
                .setParameter("groupId", groupId)
                .executeUpdate();
        LOGGER.infof("Gruppe mit ID %s erfolgreich gelöscht.", groupId);
    }

    /**
     * Aktualisiert die Informationen einer bestehenden Gruppe.
     *
     * @param projectId Projekt-ID
     * @param accountId Benutzer-ID
     * @param description Neue Gruppenbeschreibung
     * @param groupId   Gruppen-ID
     * @param name      Neuer Gruppenname
     * @param members   Neue Mitgliederliste als ContactSearchDTO
     */
    @Transactional
    public void updateGroup(String projectId, String accountId, String description, String groupId, String name, List<ContactSearchDTO> members) {
        validateProjectAndAccount(projectId, accountId);
        validateGroupId(groupId);
        validateGroupName(name);
        if (members == null) members = List.of();

        Group group = Group.findById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Gruppe mit ID " + groupId + " existiert nicht.");
        }

        group.name = name;
        group.description = description;

        List<Contact> memberList = fetchExistingContacts(members);
        addKeycloakUsers(memberList, members);
        group.members = memberList;
        LOGGER.infof("Gruppe mit ID %s erfolgreich aktualisiert.", groupId);
    }

    /**
     * Sucht Gruppen basierend auf einem Suchbegriff.
     *
     * @param searchTerm Suchbegriff
     * @param projectId  Projekt-ID
     * @param userId     Benutzer-ID
     * @return Liste der Gruppen als GroupDetailDTO
     */
    public List<GroupDetailDTO> searchGroups(String searchTerm, String projectId, String userId) {
        validateProjectAndAccount(projectId, userId);

        return em.createQuery(
                        "SELECT NEW at.htlleonding.leomail.model.dto.groups.GroupDetailDTO(g.id, g.name, g.description) " +
                                "FROM Group g WHERE g.project.id = :projectId AND g.name LIKE :searchTerm",
                        GroupDetailDTO.class)
                .setParameter("projectId", projectId)
                .setParameter("searchTerm", "%" + searchTerm + "%")
                .getResultList();
    }

    /**
     * Holt die Mitglieder einer bestimmten Gruppe.
     *
     * @param groupId   Gruppen-ID
     * @param projectId Projekt-ID
     * @param userId    Benutzer-ID
     * @return Liste der Gruppenmitglieder als ContactSearchDTO
     */
    public List<ContactSearchDTO> getGroupMembers(String groupId, String projectId, String userId) {
        validateGroupAndProject(projectId, userId, groupId);

        return em.createQuery(
                        "SELECT NEW at.htlleonding.leomail.model.dto.contacts.ContactSearchDTO(c.id, c.firstName, c.lastName, c.mailAddress) " +
                                "FROM Contact c JOIN c.groups g WHERE g.id = :groupId",
                        ContactSearchDTO.class)
                .setParameter("groupId", groupId)
                .getResultList();
    }

    /**
     * Hilfsmethode zur Validierung von Projekt- und Benutzerinformationen.
     *
     * @param projectId Projekt-ID
     * @param accountId Benutzer-ID
     */
    private void validateProjectAndAccount(String projectId, String accountId) {
        if (projectId == null || projectId.isBlank())
            throw new IllegalArgumentException("projectId darf nicht null oder leer sein.");
        if (accountId == null || accountId.isBlank())
            throw new IllegalArgumentException("accountId darf nicht null oder leer sein.");

        Long projectCount = em.createQuery(
                        "SELECT COUNT(p) FROM Project p WHERE p.id = :projectId", Long.class)
                .setParameter("projectId", projectId)
                .getSingleResult();

        if (projectCount == 0)
            throw new ProjectNotExistsException();

        if (!permissionService.hasPermission(projectId, accountId)) {
            throw new SecurityException("Benutzer hat keine Berechtigung, auf dieses Projekt zuzugreifen.");
        }
    }

    /**
     * Hilfsmethode zur Validierung der Existenz einer Gruppe.
     *
     * @param projectId Projekt-ID
     * @param groupId   Gruppen-ID
     */
    private void validateGroupExistence(String projectId, String groupId) {
        if (groupId == null || groupId.isBlank())
            throw new IllegalArgumentException("groupId darf nicht null oder leer sein.");

        Group group = Group.findById(groupId);
        if (group == null)
            throw new IllegalArgumentException("Gruppe mit ID " + groupId + " existiert nicht.");
    }

    /**
     * Hilfsmethode zur Validierung der Gruppen-ID.
     *
     * @param groupId Gruppen-ID
     */
    private void validateGroupId(String groupId) {
        if (groupId == null || groupId.isBlank())
            throw new IllegalArgumentException("groupId darf nicht null oder leer sein.");
    }

    /**
     * Hilfsmethode zur Validierung des Gruppennamens.
     *
     * @param name Gruppenname
     */
    private void validateGroupName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("name darf nicht null oder leer sein.");
    }

    /**
     * Validiert, ob das Projekt existiert, der Benutzer Berechtigungen hat, die Gruppe existiert und zur Projekt gehört.
     *
     * @param projectId Projekt-ID
     * @param userId    Benutzer-ID
     * @param groupId   Gruppen-ID
     */
    private void validateGroupAndProject(String projectId, String userId, String groupId) {
        // Validiert Projekt und Benutzerberechtigungen
        validateProjectAndAccount(projectId, userId);

        // Validiert Existenz der Gruppe
        validateGroupExistence(projectId, groupId);

        // Überprüft, ob die Gruppe zum angegebenen Projekt gehört
        Long groupProjectCount = em.createQuery(
                        "SELECT COUNT(g) FROM Group g WHERE g.id = :groupId AND g.project.id = :projectId",
                        Long.class)
                .setParameter("groupId", groupId)
                .setParameter("projectId", projectId)
                .getSingleResult();

        if (groupProjectCount == 0) {
            throw new IllegalArgumentException("Gruppe mit ID " + groupId + " gehört nicht zum Projekt mit ID " + projectId + ".");
        }
    }

    /**
     * Holt vorhandene Kontakte basierend auf einer Liste von ContactSearchDTO.
     *
     * @param members Liste der ContactSearchDTO
     * @return Liste der vorhandenen Kontakte
     */
    private List<Contact> fetchExistingContacts(List<ContactSearchDTO> members) {
        return em.createQuery(
                        "SELECT c FROM Contact c WHERE c.id IN :memberIds",
                        Contact.class)
                .setParameter("memberIds", members.stream().map(ContactSearchDTO::id).toList())
                .getResultList();
    }

    /**
     * Fügt Kontakte hinzu, die nicht bereits in der Datenbank existieren, indem sie aus Keycloak geladen werden.
     *
     * @param memberList Liste der bestehenden Kontakte
     * @param members    Liste der Mitglieder als ContactSearchDTO
     */
    private void addKeycloakUsers(List<Contact> memberList, List<ContactSearchDTO> members) {
        for (ContactSearchDTO member : members) {
            if (Contact.findById(member.id()) == null) {
                ContactSearchDTO userDTO = keycloakAdminService.findUserAsContactSearchDTO(member.id());
                if (userDTO != null) {
                    Contact contact = contactRepository.saveKeycloakUserLocally(userDTO);
                    memberList.add(contact);
                    LOGGER.infof("Keycloak-Benutzer mit ID %s erfolgreich lokal gespeichert.", member.id());
                } else {
                    LOGGER.warnf("Benutzer mit ID %s nicht in Keycloak gefunden und konnte nicht hinzugefügt werden.", member.id());
                }
            }
        }
    }
}