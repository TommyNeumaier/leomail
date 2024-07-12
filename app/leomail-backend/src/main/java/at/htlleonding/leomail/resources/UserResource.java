package at.htlleonding.leomail.resources;

import at.htlleonding.leomail.services.KeycloakService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
@Path("/users")
public class UserResource {

    @Inject
    KeycloakService keycloakService;

    @GET
    @Path("/search")
    public Response searchByUsername(@QueryParam("searchTerm") String searchTerm) {
        List<Object> users = keycloakService.searchUser(searchTerm);
        return Response.ok(users, MediaType.APPLICATION_JSON).build();
    }
}