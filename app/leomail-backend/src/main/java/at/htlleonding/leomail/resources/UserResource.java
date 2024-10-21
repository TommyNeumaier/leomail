package at.htlleonding.leomail.resources;

import at.htlleonding.leomail.repositories.UserRepository;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("user")
public class UserResource {

    @Inject
    JsonWebToken jwt;

    @Inject
    UserRepository repository;

    @GET
    @Path("available-mail-addresses")
    @Authenticated
    public Response getAvailableMailAddresses(@QueryParam("pid") String pid) {
        return Response.ok(repository.getAvailableMailAddresses(jwt.getClaim("sub"), pid)).build();
    }
}
