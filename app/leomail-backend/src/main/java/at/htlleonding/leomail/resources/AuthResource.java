package at.htlleonding.leomail.resources;

import at.htlleonding.leomail.contracts.IKeycloak;
import at.htlleonding.leomail.entities.Contact;
import at.htlleonding.leomail.model.dto.authtest.JwtClaimTest;
import at.htlleonding.leomail.model.dto.authtest.JwtTest;
import at.htlleonding.leomail.model.dto.template.KeycloakTokenResponse;
import at.htlleonding.leomail.repositories.ContactRepository;
import at.htlleonding.leomail.repositories.UserRepository;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.annotations.jaxrs.FormParam;

import java.util.LinkedList;
import java.util.List;

@Path("/auth")
public class AuthResource {

    @Inject
    @RestClient
    IKeycloak IKeycloak;

    @Inject
    ContactRepository contactRepository;

    @ConfigProperty(name = "quarkus.oidc.client-id")
    String clientId;

    @ConfigProperty(name = "quarkus.oidc.credentials.secret")
    String clientSecret;

    @Inject
    JsonWebToken jwt;

    @Inject
    UserRepository userRepository;

    @GET
    @Path("jwt")
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJwt() {
        List<JwtClaimTest> claims = new LinkedList<>();
        jwt.getClaimNames().forEach((claim) -> {
            claims.add(new JwtClaimTest(claim, jwt.claim(claim).get().toString()));
        });
        return Response.ok(new JwtTest(jwt.getAudience(), claims, jwt.getExpirationTime(), jwt.getGroups(), jwt.getIssuedAtTime(), jwt.getIssuer(), jwt.getName(), jwt.getRawToken(), jwt.getSubject(), jwt.getTokenID())).build();
    }

    @POST
    @Path("/login")
    @PermitAll
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response login(
            @FormParam("username") String username,
            @FormParam("password") String password) {

        try {
            KeycloakTokenResponse tokenResponse = IKeycloak.login(
                    clientId,
                    clientSecret,
                    "password",
                    username,
                    password,
                    "openid");
            return Response.ok(tokenResponse).build();
        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    @POST
    @PermitAll
    @Path("/refresh")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response refreshToken(
            String refreshToken) {
        System.out.println("Hallo");
        try {
            System.out.println("hallo");
            Object tokenResponse = IKeycloak.refreshToken(
                    clientId,
                    clientSecret,
                    "refresh_token",
                    refreshToken
            );
            return Response.ok(tokenResponse).build();
        } catch (Exception e) {
            return Response.status(Response.Status.fromStatusCode(401)).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/validate")
    @Authenticated
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response validateToken() {
        long exp = Long.parseLong(jwt.getExpirationTime() + "000");
        long iat = jwt.getIssuedAtTime();

        if (exp > System.currentTimeMillis() && iat < System.currentTimeMillis()) {
            if(Contact.findById(jwt.getClaim("sub")) == null) {
                contactRepository.saveKeycloakUserLocally(jwt.getClaim("sub"), jwt.getClaim("given_name"), jwt.getClaim("family_name"), jwt.getClaim("email"));
            }
            return Response.ok(true).build();
        }  else
            return Response.status(Response.Status.UNAUTHORIZED).entity(false).build();
    }
}