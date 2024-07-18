package at.htlleonding.leomail.resources;

import at.htlleonding.leomail.model.dto.KeycloakTokenIntrospectionResponse;
import at.htlleonding.leomail.model.dto.template.KeycloakTokenResponse;
import at.htlleonding.leomail.contracts.IKeycloak;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.annotations.jaxrs.FormParam;

@Path("/auth")
public class AuthResource {

    @Inject
    @RestClient
    IKeycloak IKeycloak;

    @ConfigProperty(name = "quarkus.oidc.client-id")
    String clientId;

    @ConfigProperty(name = "quarkus.oidc.credentials.secret")
    String clientSecret;

    @Inject
    JsonWebToken jwt;

    @POST
    @Path("/login")
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
                    password);
            return Response.ok(tokenResponse).build();
        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    @POST
    @Path("/refresh")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response refreshToken(
            @FormParam("refresh_token") String refreshToken) {

        try {
            KeycloakTokenResponse tokenResponse = IKeycloak.refreshToken(
                    clientId,
                    clientSecret,
                    "refresh_token",
                    refreshToken
            );
            System.out.println(tokenResponse);
            return Response.ok(tokenResponse).build();
        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    @GET
    @Path("/validate")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response validateToken() {
        try {
            KeycloakTokenIntrospectionResponse introspectionResponse = IKeycloak.introspectToken(
                    clientId,
                    clientSecret,
                    jwt.getRawToken()
            );
            if (introspectionResponse.active()) {
                return Response.ok().build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }
}