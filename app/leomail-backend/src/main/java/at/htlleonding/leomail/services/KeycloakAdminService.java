package at.htlleonding.leomail.services;

import at.htlleonding.leomail.contracts.IKeycloak;
import at.htlleonding.leomail.model.dto.contacts.ContactDTO;
import at.htlleonding.leomail.model.dto.template.KeycloakTokenResponse;
import at.htlleonding.leomail.model.exceptions.account.ContactExistsInKeycloakException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public List<Object> searchUser(String searchTerm, int maxSearchResults) {
        String token = getAdminToken();
        Client client = ClientBuilder.newClient();

        // TODO: Only get users with valid email addresses, valid firstnames, and valid lastnames
        Response response = client.target(keycloakUrl + "/admin/realms/" + realm + "/users")
                .queryParam("search", searchTerm != null ? searchTerm : "")
                .queryParam("max", maxSearchResults)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .get();

        if (response.getStatus() != 200) {
            String responseBody = response.readEntity(String.class);
            throw new RuntimeException("Failed to search user: " + responseBody);
        }

        List<Map<String, Object>> users = response.readEntity(List.class);
        return users.stream()
                .sorted((u1, u2) -> {
                    Long createdTimestamp1 = Long.parseLong(String.valueOf(u1.get("createdTimestamp")));
                    Long createdTimestamp2 = Long.parseLong(String.valueOf(u2.get("createdTimestamp")));
                    return createdTimestamp2.compareTo(createdTimestamp1);
                })
                .limit(maxSearchResults)
                .collect(Collectors.toList());
    }
}