package at.htlleonding.leomail.services;

import at.htlleonding.leomail.entities.Group;
import at.htlleonding.leomail.entities.Project;
import at.htlleonding.leomail.entities.Template;
import at.htlleonding.leomail.model.exceptions.projects.ProjectNotExistsException;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PermissionService {

    /**
     * Checks if the user has permission to access the project.
     * @return true if the user is a member of the project, false otherwise.
     */
    public boolean hasPermission(String projectId, String userId) {
        System.out.println(projectId);
        Project project = Project.findById(projectId);
        if (project == null) {
            throw new ProjectNotExistsException();
        }
        // Check if the user is a member of the project.
        return project.members.stream().anyMatch(contact -> contact.id.equals(userId));
    }

    /**
     * Checks if the user has permission to access the template.
     * @return true if the user has permission to the project AND the template belongs to the project.
     */
    public boolean hasPermission(String projectId, String userId, Long templateId) {
        Template template = Template.findById(templateId);
        if (template == null || !template.project.id.equals(projectId)) {
            return false;
        }
        // Check project permission for the user.
        return hasPermission(projectId, userId);
    }

    /**
     * Checks if the user has permission to access the project by the template id.
     * @return true if the user has permission to the project AND the template belongs to the project AND the user created the template.
     */
    public boolean hasPermission(Long templateId, String userId) {
        Template template = Template.findById(templateId);
        if (template == null) {
            return false;
        }
        // Ensure the user has permission to the project and is the creator of the template.
        return hasPermission(template.project.id, userId) && template.createdBy.id.equals(userId);
    }

    /**
     * Checks if the user has permission to access the group.
     * @return true if the user has permission to the project AND the group belongs to the project.
     */
    public boolean hasPermission(String projectId, String userId, String groupId) {
        Group group = Group.findById(groupId);
        if (group == null || !group.project.id.equals(projectId)) {
            return false;
        }
        // Check if the user is a member of the group and has permission to the project.
        return group.members.stream().anyMatch(contact -> contact.id.equals(userId))
                && hasPermission(projectId, userId);
    }
}