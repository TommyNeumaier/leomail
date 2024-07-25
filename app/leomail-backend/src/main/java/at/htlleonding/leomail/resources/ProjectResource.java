package at.htlleonding.leomail.resources;

import at.htlleonding.leomail.model.dto.project.ProjectAddDTO;
import at.htlleonding.leomail.repositories.ProjectRepository;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/project")
public class ProjectResource {

    @Inject
    JsonWebToken jwt;

    @Inject
    ProjectRepository projectRepository;

    @GET
    @Path("get/personal")
    @Authenticated
    @Produces("application/json")
    public Response getPersonalProjects() {
        return Response.ok(projectRepository.getPersonalProjects(jwt.getClaim("sub"))).build();
    }

    @POST
    @Transactional
    @Path("add")
    @Authenticated
    public Response addProject(ProjectAddDTO project) {
        projectRepository.addProject(project, jwt.getClaim("sub"));
        return Response.ok().build();
    }
}
