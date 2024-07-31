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

@ApplicationScoped
public class GroupRepository {

    @Inject
    EntityManager em;

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

        if (!permissionService.hasPermission(projectId, accountId, groupId)) {
            throw new SecurityException("User has no permission to access this group");
        }

        GroupDetailDTO dto = em.createQuery("SELECT NEW at.htlleonding.leomail.model.dto.groups.GroupDetailDTO(g.id, g.name) FROM Group g WHERE g.id = :groupId", GroupDetailDTO.class)
                .setParameter("groupId", groupId)
                .getSingleResult();

        CreatorDTO createdBy = em.createQuery("SELECT NEW at.htlleonding.leomail.model.dto.contacts.CreatorDTO(g.createdBy.id, g.createdBy.firstName, g.createdBy.lastName, g.createdBy.mailAddress) FROM Group g WHERE g.id = :groupId", CreatorDTO.class)
                .setParameter("groupId", dto.createdBy().id())
                .getSingleResult();

        List<ContactSearchDTO> members = em.createQuery("SELECT NEW at.htlleonding.leomail.model.dto.contacts.ContactSearchDTO(c.id, c.firstName, c.lastName, c.mailAddress) FROM Contact c JOIN c.groups g WHERE g.id = :groupId", ContactSearchDTO.class)
                .setParameter("groupId", groupId)
                .getResultList();

        return new GroupDetailDTO(dto.id(), dto.name(), createdBy, members);
    }

    public void createGroup(String projectId, String accountId, String name, List<ContactSearchDTO> members) {
        if (projectId == null || projectId.isBlank())
            throw new IllegalArgumentException("projectId must not be null");
        else if (accountId == null || accountId.isBlank())
            throw new IllegalArgumentException("accountId must not be null");
        else if (name == null || name.isBlank())
            throw new IllegalArgumentException("name must not be null");

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
        em.persist(new Group(name, Contact.findById(accountId), Project.findById(projectId), Collections.singletonList(memberList)));
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

        if (!permissionService.hasPermission(projectId, accountId, groupId)) {
            throw new SecurityException("User has no permission to access this group");
        }

        em.createQuery("DELETE FROM Group g WHERE g.id = :groupId")
                .setParameter("groupId", groupId)
                .executeUpdate();
    }

    public void updateGroup(String projectId, String accountId, String groupId, String name, List<ContactSearchDTO> members) {
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

        if (!permissionService.hasPermission(projectId, accountId, groupId)) {
            throw new SecurityException("User has no permission to access this group");
        }

        Group group = Group.findById(groupId);
        group.name = name;
        group.members = em.createQuery("SELECT c FROM Contact c WHERE c.id IN :memberIds", Contact.class)
                .setParameter("memberIds", members.stream().map(ContactSearchDTO::id).toList())
                .getResultList();
    }
}
