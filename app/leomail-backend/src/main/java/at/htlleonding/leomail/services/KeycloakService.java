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

        Response response = client.target(keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token")
                .request(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(Entity.form(form));

        String responseBody = response.readEntity(String.class);
        logger.info("Response status: " + response.getStatus());
        logger.info("Response body: " + responseBody);

        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed to retrieve admin token: " + responseBody);
        }

        return response.readEntity(TokenResponse.class).access_token;
    }

    public List<Object> searchUserByUsername(String username) {
        String token = getAdminToken();
        Client client = ClientBuilder.newClient();

        Response response = client.target(keycloakUrl + "/admin/realms/" + realm + "/users")
                .queryParam("username", username)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .get();

        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed to search user by username: " + response.readEntity(String.class));
        }

        return response.readEntity(List.class);
    }

    public List<Object> searchUserByPreferredName(String preferredName) {
        String token = getAdminToken();
        Client client = ClientBuilder.newClient();

        Response response = client.target(keycloakUrl + "/admin/realms/" + realm + "/users")
                .queryParam("firstName", preferredName)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .get();

        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed to search user by preferred name: " + response.readEntity(String.class));
        }

        return response.readEntity(List.class);
    }

    public static class TokenResponse {
        public String access_token;
    }
}
