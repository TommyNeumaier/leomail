package at.htlleonding.leomail.services;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class KeycloakService {

    private static final Logger logger = Logger.getLogger(KeycloakService.class.getName());

    @ConfigProperty(name = "keycloak.url")
    String keycloakUrl;

    @ConfigProperty(name = "keycloak.realm")
    String realm;

    @ConfigProperty(name = "keycloak.client-id")
    String clientId;

    @ConfigProperty(name = "keycloak.client-secret")
    String clientSecret;

    private String getAdminToken() {
        Client client = ClientBuilder.newClient();

        Form form = new Form();
        form.param("client_id", clientId);
        form.param("client_secret", clientSecret);
        form.param("grant_type", "client_credentials");

        logger.info("Requesting admin token from Keycloak...");
        logger.info("Form parameters: client_id=" + clientId + ", client_secret=" + clientSecret);

        Response response = client.target(keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token")
                .request(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(Entity.form(form));

        if (response.getStatus() != 200) {
            String responseBody = response.readEntity(String.class);
            logger.info("Response status: " + response.getStatus());
            logger.info("Response body: " + responseBody);
            throw new RuntimeException("Failed to retrieve admin token: " + responseBody);
        }

        TokenResponse tokenResponse = response.readEntity(TokenResponse.class);
        logger.info("Token retrieved: " + tokenResponse.access_token);
        return tokenResponse.access_token;
    }

    public List<Object> searchUser(String searchTerm) {
        String token = getAdminToken();
        Client client = ClientBuilder.newClient();

        Response response = client.target(keycloakUrl + "/admin/realms/" + realm + "/users")
                .queryParam("search", searchTerm)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .get();

        if (response.getStatus() != 200) {
            String responseBody = response.readEntity(String.class);
            throw new RuntimeException("Failed to search user: " + responseBody);
        }

        return response.readEntity(List.class);
    }

    public static class TokenResponse {
        public String access_token;
    }
}