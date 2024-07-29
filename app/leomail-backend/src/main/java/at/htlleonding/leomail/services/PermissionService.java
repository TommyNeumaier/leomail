package at.htlleonding.leomail.services;

import at.htlleonding.leomail.entities.Project;
import at.htlleonding.leomail.entities.Template;
import at.htlleonding.leomail.model.exceptions.projects.ProjectNotExistsException;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Objects;

@ApplicationScoped
public class PermissionService {

    public boolean hasPermission(String projectId, String userId) {
        Project project = Project.findById(projectId);
        if (project == null) throw new ProjectNotExistsException();
        return Objects.requireNonNull(project).members.stream().anyMatch(contact -> contact.id.equals(userId));
    }

    /** Check if the user has permission to access the template
     *
     * @return true if the user has permission to the project AND the template belongs to the project, false otherwise
     * */

    public boolean hasPermission(String projectId, String userId, Long templateId) {
        Template template = Template.findById(templateId);
        if (template == null) return false;

        if (template.project.id.equals(projectId))
            return hasPermission(projectId, userId);
        return false;
    }

    /** Check if the user has permission to access the project by the template id
     *
     * @return true if the user has permission to the project AND the template belongs to the project, false otherwise
     * */

    public boolean hasPermission(Long templateId, String userId) {
        Template template = Template.findById(templateId);
        if (template == null) return false;
        return hasPermission(template.project.id, userId);
    }
}
