package at.htlleonding.leomail.services;

import at.htlleonding.leomail.contracts.IKeycloak;
import at.htlleonding.leomail.entities.KeycloakContact;
import at.htlleonding.leomail.model.dto.contacts.KeycloakUserDTO;
import at.htlleonding.leomail.model.dto.template.KeycloakTokenResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
                .queryParam("max", Integer.MAX_VALUE)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .get();

        if (response.getStatus() != 200) {
            String responseBody = response.readEntity(String.class);
            throw new RuntimeException("Failed to search user: " + responseBody);
        }

        return response.readEntity(List.class);
    }

    public List<KeycloakUserDTO> synchroniseUsers() {
        String token = getAdminToken();
        Client client = ClientBuilder.newClient();

        // TODO: Make multiple requests to get all users
        List<KeycloakUserDTO> keycloakUserDTOs = new ArrayList<>();

        for (int i = 0; i < 60; i++) {
            System.out.println(i);
            Response response = client.target(keycloakUrl + "/admin/realms/" + realm + "/users")
                    .queryParam("max", "15000")
                    .queryParam("email", "@")
                    .queryParam("firstName", "")
                    .queryParam("lastName", "")
                    .queryParam("first", i * 250)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .get();

            if (response.getStatus() != 200) {
                String responseBody = response.readEntity(String.class);
                throw new RuntimeException("Failed to synchronise users: " + responseBody);
            }

            List users = response.readEntity(List.class);

            for (Object user : users) {
                KeycloakUserDTO keycloakUserDTO = new KeycloakUserDTO(
                        ((Map) user).get("id").toString(),
                        ((Map) user).get("firstName").toString(),
                        ((Map) user).get("lastName").toString(),
                        ((Map) user).get("email").toString()
                );
                keycloakUserDTOs.add(keycloakUserDTO);

                KeycloakContact contact = KeycloakContact.findById(keycloakUserDTO.id());
                if (contact == null) {
                    contact = new KeycloakContact(keycloakUserDTO.firstName(), keycloakUserDTO.lastName(), keycloakUserDTO.mailAddress(), keycloakUserDTO.id());
                    contact.persist();
                } else {
                    contact.firstName = keycloakUserDTO.firstName();
                    contact.lastName = keycloakUserDTO.lastName();
                    contact.mailAddress = keycloakUserDTO.mailAddress();
                }
            }
        }

        return keycloakUserDTOs;
    }
}