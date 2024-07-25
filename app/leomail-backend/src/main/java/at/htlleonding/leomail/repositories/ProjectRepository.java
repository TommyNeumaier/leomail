package at.htlleonding.leomail.repositories;

import at.htlleonding.leomail.entities.Contact;
import at.htlleonding.leomail.entities.Project;
import at.htlleonding.leomail.model.dto.project.ProjectAddDTO;
import at.htlleonding.leomail.model.dto.project.ProjectOverviewDTO;
import at.htlleonding.leomail.model.exceptions.ObjectContainsNullAttributesException;
import at.htlleonding.leomail.services.Utilities;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;

@ApplicationScoped
public class ProjectRepository {

    @Inject
    EntityManager em;

    public List<ProjectOverviewDTO> getPersonalProjects(String contactId) {
        return em.createQuery("SELECT NEW at.htlleonding.leomail.model.dto.project.ProjectOverviewDTO(p.id, p.name) FROM Project p JOIN p.members m WHERE m.id = :contactId", ProjectOverviewDTO.class)
                .setParameter("contactId", contactId)
                .getResultList();
    }

    public ProjectAddDTO addProject(ProjectAddDTO projectAddDTO, String creator) {
        if(projectAddDTO == null) {
            throw new ObjectContainsNullAttributesException(List.of("**ALL NULL**"));
        }

        List<String> nullFields = Utilities.listNullFields(projectAddDTO, List.of("id", "description", "mailInformation", "members"));
        if(!nullFields.isEmpty()) {
            throw new ObjectContainsNullAttributesException(nullFields);
        }

        Project project = new Project(projectAddDTO.name(), projectAddDTO.description(), Contact.findById(creator), projectAddDTO.mailInformation().mailAddress(), projectAddDTO.mailInformation().password(), projectAddDTO.members());
        project.persist();
        return projectAddDTO;
    }
}
