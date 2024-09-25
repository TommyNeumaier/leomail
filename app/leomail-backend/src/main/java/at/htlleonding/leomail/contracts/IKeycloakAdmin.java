package at.htlleonding.leomail.contracts;

import at.htlleonding.leomail.model.dto.auth.KeycloakUserMapperDTO;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@RegisterRestClient(configKey = "keycloak-admin-api")
@Path("/admin/realms/{realm}")
public interface IKeycloakAdmin {

    @GET
    @Path("/users")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    List<KeycloakUserMapperDTO> searchUsers(
            @HeaderParam("Authorization") String authorization,
            @PathParam("realm") String realm,
            @QueryParam("search") String searchTerm,
            @QueryParam("max") int maxResults
    );

    @GET
    @Path("/users/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    KeycloakUserMapperDTO findUser(
            @HeaderParam("Authorization") String authorization,
            @PathParam("realm") String realm,
            @PathParam("userId") String userId
    );
}
