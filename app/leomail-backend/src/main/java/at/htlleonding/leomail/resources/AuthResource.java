package at.htlleonding.leomail.resources;

import at.htlleonding.leomail.entities.Contact;
import at.htlleonding.leomail.entities.NaturalContact;
import at.htlleonding.leomail.model.dto.auth.JwtClaimTest;
import at.htlleonding.leomail.model.dto.auth.JwtTest;
import at.htlleonding.leomail.model.dto.auth.OutlookPasswordRequest;
import at.htlleonding.leomail.repositories.ContactRepository;
import at.htlleonding.leomail.repositories.UserRepository;
import at.htlleonding.leomail.services.EncryptionService;
import at.htlleonding.leomail.services.MailService;
import io.quarkus.oidc.client.OidcClient;
import io.quarkus.oidc.client.Tokens;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.jaxrs.FormParam;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Path("/auth")
public class AuthResource {

    private static final Logger LOGGER = Logger.getLogger(AuthResource.class);
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(AuthResource.class);

    @Inject
    ContactRepository contactRepository;

    @Inject
    EncryptionService encryptionService;

    @Inject
    SecurityIdentity identity;

    @Inject
    JsonWebToken jwt;

    @Inject
    MailService mailService;

    @Inject
    UserRepository userRepository;

    @Inject
    OidcClient oidcClient;

    @ConfigProperty(name = "quarkus.oidc.auth-server-url")
    String keycloakUrl;

    @ConfigProperty(name = "keycloak.realm")
    String realm;

    @ConfigProperty(name = "quarkus.oidc.client-id")
    String clientId;

    @ConfigProperty(name = "quarkus.oidc.credentials.secret")
    String clientSecret;

    /**
     * Endpoint to retrieve JWT details.
     *
     * @return JSON with JWT details
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
            LOGGER.error("Error retrieving JWT", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving JWT")
                    .build();
        }
    }

    @POST
    @Path("/login")
    @PermitAll
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@FormParam("username") String username, @FormParam("password") String password) {
        try {
            MultivaluedMap<String, String> formParams = new MultivaluedHashMap<>();
            formParams.add("grant_type", "password");
            formParams.add("client_id", clientId);
            formParams.add("username", username);
            formParams.add("password", password);
            formParams.add("scope", "openid");

            formParams.add("client_secret", clientSecret);

            Response response = ClientBuilder.newClient()
                    .target(String.format("%s/protocol/openid-connect/token", keycloakUrl))
                    .request(MediaType.APPLICATION_FORM_URLENCODED)
                    .post(Entity.form(formParams));

            if (response.getStatus() == 200) {
                return Response.ok(response.readEntity(String.class)).build();
            } else {
                System.out.println(response.readEntity(String.class));
                return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build();
            }
        } catch (Exception e) {
            LOGGER.errorf("Login failed for user: %s", username, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error during login").build();
        }
    }

    /**
     * Endpoint to refresh the token.
     *
     * @param refreshToken Refresh Token
     * @return New token response or error status
     */
    @POST
    @Path("/refresh")
    @PermitAll
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response refreshToken(
            @FormParam("refresh_token") String refreshToken) {
        try {
            Tokens tokens = oidcClient.refreshTokens(refreshToken).await().indefinitely();
            return Response.ok(tokens).build();
        } catch (Exception e) {
            LOGGER.error("Token refresh failed", e);
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Invalid refresh token")
                    .build();
        }
    }

    /**
     * Endpoint to validate the token.
     *
     * @return Boolean value indicating token validation
     */
    @GET
    @Path("/validate")
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateToken() {
        try {
            long exp = jwt.getExpirationTime() * 1000;
            long iat = jwt.getIssuedAtTime()* 1000;

            long currentTime = System.currentTimeMillis();

            if (exp > currentTime && iat < currentTime) {
                if (Contact.findById(jwt.getSubject()) == null) {
                    String givenName = jwt.getClaim("given_name");
                    String familyName = jwt.getClaim("family_name");
                    String email = jwt.getClaim("email");
                    System.out.println(jwt.getClaimNames());
                    if (email == null || email.isEmpty()) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Email is required in the token")
                                .build();
                    }
                    contactRepository.saveKeycloakUserLocally(jwt.getSubject(), givenName, familyName, email);
                }
                return Response.ok(true).build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).entity(false).build();
            }
        } catch (Exception e) {
            LOGGER.error("Token validation failed", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error during token validation")
                    .build();
        }
    }

    /**
     * Endpoint to retrieve the user profile.
     *
     * @return User profile or error status
     */
    @GET
    @Path("/profile")
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProfile() {
        try {
            return Response.ok(userRepository.getProfile(jwt.getSubject())).build();
        } catch (Exception e) {
            LOGGER.error("Error retrieving profile", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving profile")
                    .build();
        }
    }

    /*
     * Endpoint to retrieve the user roles
     *
     * @return User profile or error status
     */
    @GET
    @Path("/roles")
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRoles() {
        try {
            return Response.ok(identity.getRoles()).build();
        } catch (Exception e) {
            LOGGER.error("Error retrieving roles", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving roles")
                    .build();
        }
    }

    /**
     * Endpunkt zum Speichern und Verifizieren des Outlook-Passworts.
     *
     * @param request Die Outlook-Passwort-Anfrage.
     * @return Ein Response-Objekt mit dem Ergebnis.
     */
    @POST
    @Path("/save-outlook-password")
    @Transactional
    @Authenticated
    public Response saveOutlookPassword(OutlookPasswordRequest request) {
        try {
            // Verifiziere die SMTP-Anmeldedaten
            boolean isValid = mailService.verifyOutlookCredentials(request.email(), request.password());

            if (!isValid) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Ungültige Outlook-Anmeldedaten")
                        .build();
            }

            // Verschlüssele das Passwort
            String encryptedPassword = encryptionService.encrypt(request.password());

            // Finde den Benutzer und speichere das verschlüsselte Passwort
            NaturalContact user = userRepository.findByEmail(request.email());
            if (user == null) {
                user = userRepository.findByEmail(request.email().replace("htblaleonding.onmicrosoft.com", "students.htl-leonding.ac.at"));
                if(user == null) {
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("Benutzer nicht gefunden")
                            .build();
                }
            }

            user.encryptedOutlookPassword = encryptedPassword;
            userRepository.persist(user);

            return Response.ok("Outlook-Passwort erfolgreich gespeichert").build();

        } catch (Exception e) {
            LOGGER.error("Fehler beim Speichern des Outlook-Passworts", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Fehler beim Speichern des Outlook-Passworts")
                    .build();
        }
    }

    @GET
    @Path("/check-outlook-authorization")
    @Authenticated
    public Response checkOutlookAuthorization() {
        try {
            NaturalContact user = userRepository.findByEmail(jwt.getClaim("email"));
            if (user.encryptedOutlookPassword == null) {
                return Response.ok()
                        .entity(false)
                        .build();
            }

            return Response.ok(true).build();
        } catch (Exception e) {
            LOGGER.error("Fehler beim Überprüfen der Outlook-Autorisierung", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Fehler beim Überprüfen der Outlook-Autorisierung")
                    .build();
        }
    }
}