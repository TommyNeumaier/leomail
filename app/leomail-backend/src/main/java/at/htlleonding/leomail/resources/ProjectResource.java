package at.htlleonding.leomail.resources;

import at.htlleonding.leomail.repositories.ProjectRepository;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
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
}
