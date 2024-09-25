package at.htlleonding.leomail.resources;

import at.htlleonding.leomail.contracts.IKeycloak;
import at.htlleonding.leomail.entities.Contact;
import at.htlleonding.leomail.model.dto.auth.JwtClaimTest;
import at.htlleonding.leomail.model.dto.auth.JwtTest;
import at.htlleonding.leomail.model.dto.contacts.ContactSearchDTO;
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
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.jaxrs.FormParam;

import java.util.List;
import java.util.stream.Collectors;

@Path("/auth")
public class AuthResource {

    private static final Logger LOGGER = Logger.getLogger(AuthResource.class);

    @Inject
    @RestClient
    IKeycloak keycloakClient;

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

    /**
     * Endpunkt zur Abfrage des JWT.
     *
     * @return JSON mit JWT-Details
     */
    @GET
    @Path("/jwt")
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJwt() {
        try {
            List<JwtClaimTest> claims = jwt.getClaimNames().stream()
                    .map(claim -> new JwtClaimTest(claim, jwt.getClaim(claim)))
                    .collect(Collectors.toList());

            JwtTest jwtTest = new JwtTest(
                    jwt.getAudience(),
                    claims,
                    jwt.getExpirationTime(),
                    jwt.getGroups(),
                    jwt.getIssuedAtTime(),
                    jwt.getIssuer(),
                    jwt.getName(),
                    jwt.getRawToken(),
                    jwt.getSubject(),
                    jwt.getTokenID()
            );

            return Response.ok(jwtTest).build();
        } catch (Exception e) {
            LOGGER.error("Fehler beim Abrufen des JWT", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Fehler beim Abrufen des JWT")
                    .build();
        }
    }

    /**
     * Endpunkt zum Benutzer-Login.
     *
     * @param username Benutzername
     * @param password Passwort
     * @return Token-Antwort oder Fehlerstatus
     */
    @POST
    @Path("/login")
    @PermitAll
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(
            @FormParam("username") String username,
            @FormParam("password") String password) {

        try {
            KeycloakTokenResponse tokenResponse = keycloakClient.login(
                    clientId,
                    clientSecret,
                    "password",
                    username,
                    password,
                    "openid"
            );
            return Response.ok(tokenResponse).build();
        } catch (Exception e) {
            LOGGER.errorf("Login fehlgeschlagen für Benutzer: %s", username, e);
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Ungültige Anmeldedaten")
                    .build();
        }
    }

    /**
     * Endpunkt zum Aktualisieren des Tokens.
     *
     * @param refreshToken Refresh Token
     * @return Neue Token-Antwort oder Fehlerstatus
     */
    @POST
    @Path("/refresh")
    @PermitAll
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response refreshToken(
            @FormParam("refresh_token") String refreshToken) {
        try {
            Object tokenResponse = keycloakClient.refreshToken(
                    clientId,
                    clientSecret,
                    "refresh_token",
                    refreshToken
            );
            return Response.ok(tokenResponse).build();
        } catch (Exception e) {
            LOGGER.error("Token-Refresh fehlgeschlagen", e);
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Ungültiger Refresh-Token")
                    .build();
        }
    }

    /**
     * Endpunkt zur Validierung des Tokens.
     *
     * @return Boolean-Wert zur Token-Validierung
     */
    @GET
    @Path("/validate")
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateToken() {
        try {
            long exp = jwt.getExpirationTime();
            long iat = jwt.getIssuedAtTime() * 1000;

            if (exp > System.currentTimeMillis() && iat < System.currentTimeMillis()) {
                if (Contact.findById(jwt.getClaim("sub")) == null) {
                    String givenName = jwt.getClaim("given_name");
                    String familyName = jwt.getClaim("family_name");
                    String email = jwt.getClaim("mailAddress");
                    contactRepository.saveKeycloakUserLocally(new ContactSearchDTO(jwt.getClaim("sub"), givenName, familyName, email));
                }
                return Response.ok(true).build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).entity(false).build();
            }
        } catch (Exception e) {
            LOGGER.error("Token-Validierung fehlgeschlagen", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Fehler bei der Token-Validierung")
                    .build();
        }
    }

    /**
     * Endpunkt zur Abfrage des Benutzerprofils.
     *
     * @return Benutzerprofil oder Fehlerstatus
     */
    @GET
    @Path("/profile")
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProfile() {
        try {
            return Response.ok(userRepository.getProfile(jwt.getClaim("sub"))).build();
        } catch (Exception e) {
            LOGGER.error("Fehler beim Abrufen des Profils", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Fehler beim Abrufen des Profils")
                    .build();
        }
    }
}