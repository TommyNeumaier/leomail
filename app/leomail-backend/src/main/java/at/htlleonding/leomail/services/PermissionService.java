package at.htlleonding.leomail.services;

import at.htlleonding.leomail.entities.Group;
import at.htlleonding.leomail.entities.Project;
import at.htlleonding.leomail.model.exceptions.projects.ProjectNotExistsException;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PermissionService {

    /**
     * Checks if the project exists.
     * @param projectId ID of the project.
     * @return Project if it exists, otherwise throws an exception.
     */
    private Project getProjectOrThrow(String projectId) {
        Project project = Project.findById(projectId);
        if (project == null) {
            throw new ProjectNotExistsException();
        }
        return project;
    }

    /**
     * Checks if the user is a member of the given project.
     * @param project Entity of the project.
     * @param userId ID of the user.
     * @return true if the user is a member, false otherwise.
     */
    private boolean isProjectMember(Project project, String userId) {
        return project.members.stream().anyMatch(contact -> contact.id.equals(userId));
    }

    /**
     * Checks if the user has permission to access the project.
     * @param projectId ID of the project.
     * @param userId ID of the user.
     * @return true if the user is a member of the project, false otherwise.
     */
    public boolean hasPermission(String projectId, String userId) {
        Project project = getProjectOrThrow(projectId);
        return isProjectMember(project, userId);
    }

    /**
     * Checks if the user has permission to access the group.
     * @param projectId ID of the project.
     * @param userId ID of the user.
     * @param groupId ID of the group.
     * @return true if the user has permission to the project AND the group belongs to the project.
     */
    public boolean hasPermission(String projectId, String userId, String groupId) {
        Group group = Group.findById(groupId);
        if (group == null || !group.project.id.equals(projectId)) {
            return false;
        }
        return hasPermission(projectId, userId) && group.members.stream().anyMatch(contact -> contact.id.equals(userId));
    }
}