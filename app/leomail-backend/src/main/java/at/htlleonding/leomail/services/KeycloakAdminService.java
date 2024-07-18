package at.htlleonding.leomail.services;

import at.htlleonding.leomail.contracts.IKeycloak;
import at.htlleonding.leomail.model.dto.template.KeycloakTokenResponse;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@ApplicationScoped
public class KeycloakAdminService {

    @ConfigProperty(name = "quarkus.rest-client.\"keycloak-api\".url")
    String keycloakUrl;

    @ConfigProperty(name = "keycloak.realm")
    String realm;

    @ConfigProperty(name = "quarkus.oidc.client-id")
    String clientId;

    @ConfigProperty(name = "quarkus.oidc.credentials.secret")
    String clientSecret;

    @Inject
    @RestClient
    IKeycloak IKeycloak;

    private String getAdminToken() {
        KeycloakTokenResponse tokenResponse = IKeycloak.serviceAccountLogin(clientId, clientSecret, "client_credentials");
        return tokenResponse.access_token();
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

    public void synchroniseUsers() {
        String token = getAdminToken();
        Client client = ClientBuilder.newClient();

        Response response = client.target(keycloakUrl + "/admin/realms/" + realm + "/users")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .get();

        if (response.getStatus() != 200) {
            String responseBody = response.readEntity(String.class);
            throw new RuntimeException("Failed to synchronise users: " + responseBody);
        }

        List<Object> users = response.readEntity(List.class);
        System.out.println(users);

        // TODO: Implement user synchronisation
    }
}