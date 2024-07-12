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
    @Path("/searchByUsername")
    public Response searchByUsername(@QueryParam("username") String username) {
        List<Object> users = keycloakService.searchUserByUsername(username);
        return Response.ok(users, MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/searchByPreferredName")
    public Response searchByPreferredName(@QueryParam("preferredName") String preferredName) {
        List<Object> users = keycloakService.searchUserByPreferredName(preferredName);
        return Response.ok(users, MediaType.APPLICATION_JSON).build();
    }
}
