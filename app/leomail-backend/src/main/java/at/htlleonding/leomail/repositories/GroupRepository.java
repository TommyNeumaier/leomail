package at.htlleonding.leomail.repositories;

import at.htlleonding.leomail.entities.Contact;
import at.htlleonding.leomail.entities.Group;
import at.htlleonding.leomail.entities.Project;
import at.htlleonding.leomail.model.dto.contacts.ContactSearchDTO;
import at.htlleonding.leomail.model.dto.contacts.CreatorDTO;
import at.htlleonding.leomail.model.dto.groups.GroupDetailDTO;
import at.htlleonding.leomail.model.dto.groups.GroupOverviewDTO;
import at.htlleonding.leomail.model.exceptions.projects.ProjectNotExistsException;
import at.htlleonding.leomail.services.PermissionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.Collections;
import java.util.List;

import static io.quarkus.hibernate.orm.panache.PanacheEntityBase.find;

@ApplicationScoped
public class GroupRepository {

    @Inject
    EntityManager em;

    @Inject
    ContactRepository contactRepository;

    @Inject
    PermissionService permissionService;

    public List<GroupOverviewDTO> getPersonalGroups(String projectId, String accountId) {
        if (projectId == null || projectId.isBlank())
            throw new IllegalArgumentException("projectId must not be null");
        else if (accountId == null || accountId.isBlank())
            throw new IllegalArgumentException("accountId must not be null");

        if (em.createQuery("SELECT COUNT(p) FROM Project p WHERE p.id = :projectId", Long.class)
                .setParameter("projectId", projectId)
                .getSingleResult() == 0)
            throw new ProjectNotExistsException();

        if (!permissionService.hasPermission(projectId, accountId)) {
            throw new SecurityException("User has no permission to access this project");
        }

        return em.createQuery("SELECT NEW at.htlleonding.leomail.model.dto.groups.GroupOverviewDTO(g.id, g.name, concat(g.createdBy.firstName, ' ', g.createdBy.lastName)) FROM Group g WHERE g.project.id = :projectId AND g.createdBy.id = :accountId", GroupOverviewDTO.class)
                .setParameter("projectId", projectId)
                .setParameter("accountId", accountId)
                .getResultList();
    }

    public GroupDetailDTO getGroupDetails(String projectId, String accountId, String groupId) {
        if (groupId == null || groupId.isBlank())
            throw new IllegalArgumentException("groupId must not be null");

        if (em.createQuery("SELECT COUNT(p) FROM Project p WHERE p.id = :projectId", Long.class)
                .setParameter("projectId", projectId)
                .getSingleResult() == 0)
            throw new ProjectNotExistsException();

        if (!permissionService.hasPermission(projectId, accountId)) {
            throw new SecurityException("User has no permission to access this group");
        }

        GroupDetailDTO dto = em.createQuery("SELECT NEW at.htlleonding.leomail.model.dto.groups.GroupDetailDTO(g.id, g.name, g.description) FROM Group g WHERE g.id = :groupId", GroupDetailDTO.class)
                .setParameter("groupId", groupId)
                .getSingleResult();

        CreatorDTO createdBy = em.createQuery("SELECT NEW at.htlleonding.leomail.model.dto.contacts.CreatorDTO(g.createdBy.id, g.createdBy.firstName, g.createdBy.lastName, g.createdBy.mailAddress) FROM Group g WHERE g.id = :groupId", CreatorDTO.class)
                .setParameter("groupId", groupId)
                .getSingleResult();

        Group group = Group.findById(groupId);
        List<ContactSearchDTO> members = group.members != null ? group.members.stream().map(contact -> new ContactSearchDTO(contact.id, contact.firstName, contact.lastName, contact.mailAddress)).toList() : Collections.emptyList();
        return new GroupDetailDTO(dto.id(), dto.name(), dto.description(), createdBy, members);
    }

    public void createGroup(String projectId, String accountId, String description, String name, List<ContactSearchDTO> members) {
        if (projectId == null || projectId.isBlank())
            throw new IllegalArgumentException("projectId must not be null");
        else if (accountId == null || accountId.isBlank())
            throw new IllegalArgumentException("accountId must not be null");
        else if (name == null || name.isBlank())
            throw new IllegalArgumentException("name must not be null");
        if(members == null) members = List.of();

        if (em.createQuery("SELECT COUNT(p) FROM Project p WHERE p.id = :projectId", Long.class)
                .setParameter("projectId", projectId)
                .getSingleResult() == 0)
            throw new ProjectNotExistsException();

        if (!permissionService.hasPermission(projectId, accountId)) {
            throw new SecurityException("User has no permission to access this project");
        }

        List<Contact> memberList = em.createQuery("SELECT c FROM Contact c WHERE c.id IN :memberIds", Contact.class)
                .setParameter("memberIds", members.stream().map(ContactSearchDTO::id).toList())
                .getResultList();

        addKeycloakUsers(memberList, members);

        em.persist(new Group(name, description, Contact.findById(accountId), Project.findById(projectId), memberList));
    }

    private void addKeycloakUsers(List<Contact> memberList, List<ContactSearchDTO> members) {
        members.forEach(member -> {
            if (Contact.findById(member.id()) == null) {
                Contact contact = contactRepository.saveKeycloakUserLocally(member.id(), member.firstName(), member.lastName(), member.mailAddress());
                memberList.add(contact);
            }
        });
    }

    public void deleteGroup(String projectId, String accountId, String groupId) {
        if (projectId == null || projectId.isBlank())
            throw new IllegalArgumentException("projectId must not be null");
        else if (accountId == null || accountId.isBlank())
            throw new IllegalArgumentException("accountId must not be null");
        else if (groupId == null || groupId.isBlank())
            throw new IllegalArgumentException("groupId must not be null");

        if (em.createQuery("SELECT COUNT(p) FROM Project p WHERE p.id = :projectId", Long.class)
                .setParameter("projectId", projectId)
                .getSingleResult() == 0)
            throw new ProjectNotExistsException();

        if (!permissionService.hasPermission(projectId, accountId)) {
            throw new SecurityException("User has no permission to access this group");
        }

        em.createQuery("DELETE FROM Group g WHERE g.id = :groupId")
                .setParameter("groupId", groupId)
                .executeUpdate();
    }

    public void updateGroup(String projectId, String accountId, String description, String groupId, String name, List<ContactSearchDTO> members) {
        if (projectId == null || projectId.isBlank())
            throw new IllegalArgumentException("projectId must not be null");
        else if (accountId == null || accountId.isBlank())
            throw new IllegalArgumentException("accountId must not be null");
        else if (groupId == null || groupId.isBlank())
            throw new IllegalArgumentException("groupId must not be null");
        else if (name == null || name.isBlank())
            throw new IllegalArgumentException("name must not be null");

        if (em.createQuery("SELECT COUNT(p) FROM Project p WHERE p.id = :projectId", Long.class)
                .setParameter("projectId", projectId)
                .getSingleResult() == 0)
            throw new ProjectNotExistsException();

        if (!permissionService.hasPermission(projectId, accountId)) {
            throw new SecurityException("User has no permission to access this group");
        }

        Group group = Group.findById(groupId);
        group.name = name;
        group.description = description;
        group.members = em.createQuery("SELECT c FROM Contact c WHERE c.id IN :memberIds", Contact.class)
                .setParameter("memberIds", members != null ? members.stream().map(ContactSearchDTO::id).toList() : List.of())
                .getResultList();
        group.members.add(Contact.findById(accountId));

        addKeycloakUsers(group.members, members != null ? members : List.of());
    }

    public List<GroupDetailDTO> searchGroups(String searchTerm, String projectId, String userId) {
        return em.createQuery("SELECT NEW at.htlleonding.leomail.model.dto.groups.GroupDetailDTO(g.id, g.name, g.description) FROM Group g WHERE g.project.id = :projectId AND g.name LIKE :searchTerm", GroupDetailDTO.class)
                .setParameter("projectId", projectId)
                .setParameter("searchTerm", "%" + searchTerm + "%")
                .getResultList();
    }

    public List<ContactSearchDTO> getGroupMembers(String groupId, String projectId, String userId) {
        if (groupId == null || groupId.isBlank()) {
            throw new IllegalArgumentException("groupId must not be null");
        }

        if (projectId == null || projectId.isBlank()) {
            throw new IllegalArgumentException("projectId must not be null");
        }

        if (!permissionService.hasPermission(projectId, userId)) {
            throw new SecurityException("User has no permission to access this group");
        }

        Long projectCount = em.createQuery(
                        "SELECT COUNT(p) FROM Project p WHERE p.id = :projectId", Long.class)
                .setParameter("projectId", projectId)
                .getSingleResult();

        if (projectCount == 0) {
            throw new ProjectNotExistsException();
        }

        Group group = Group.findById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Group with id " + groupId + " does not exist.");
        }

        return em.createQuery(
                        "SELECT NEW at.htlleonding.leomail.model.dto.contacts.ContactSearchDTO(c.id, c.firstName, c.lastName, c.mailAddress) " +
                                "FROM Contact c WHERE :group in c.groups", ContactSearchDTO.class)
                .setParameter("group", group)
                .getResultList();
    }


}
