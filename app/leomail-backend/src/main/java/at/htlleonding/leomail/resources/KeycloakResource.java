package at.htlleonding.leomail.resources;

import at.htlleonding.leomail.services.KeycloakAdminService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/users")
public class KeycloakResource {

    @Inject
    KeycloakAdminService keycloakService;

    // TODO: Doesnt work atm because of services account permissions in the keycloak client configuration
   /* @GET
    @Path("/search")
    public Response searchUser(@QueryParam("searchTerm") String searchTerm) {
        List<Object> users = keycloakService.searchUser(searchTerm);
        return Response.ok(users, MediaType.APPLICATION_JSON).build();
    }*/

    @GET
    @Path("hello")
    public Response hello() {
        return Response.ok("Hello World").build();
    }
}
