package at.htlleonding.leomail.resources;

import at.htlleonding.leomail.services.KeycloakAdminService;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
@Path("/users")
public class UserResource {

    @Inject
    KeycloakAdminService keycloakService;

    @GET
    @Path("/search")
    public Response searchUser(@QueryParam("searchTerm") String searchTerm) {
        List<Object> users = keycloakService.searchUser(searchTerm);
        return Response.ok(users, MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/synchronise")
    @Transactional
    @TransactionConfiguration(timeout = Integer.MAX_VALUE)
    @Produces(MediaType.APPLICATION_JSON)
    public Response synchroniseUsers() {
        return Response.ok(keycloakService.synchroniseUsers()).build();
    }
}
