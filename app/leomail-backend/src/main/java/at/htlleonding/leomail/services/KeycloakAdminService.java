package at.htlleonding.leomail.services;

import at.htlleonding.leomail.contracts.IKeycloakAdmin;
import at.htlleonding.leomail.contracts.IKeycloakToken;
import at.htlleonding.leomail.model.dto.auth.KeycloakUserMapperDTO;
import at.htlleonding.leomail.model.dto.template.KeycloakTokenResponse;
import at.htlleonding.leomail.model.dto.contacts.ContactSearchDTO;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@ApplicationScoped
public class KeycloakAdminService {

    private static final Logger LOGGER = Logger.getLogger(KeycloakAdminService.class);

    @Inject
    @RestClient
    IKeycloakToken keycloakTokenService;

    @Inject
    @RestClient
    IKeycloakAdmin keycloakAdminClient;

    @ConfigProperty(name = "quarkus.oidc.client-id")
    String clientId;

    @ConfigProperty(name = "quarkus.oidc.credentials.secret")
    String clientSecret;

    @ConfigProperty(name = "keycloak.realm")
    String realm;

    private Cache<String, String> tokenCache;

    @PostConstruct
    void init() {
        tokenCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .maximumSize(10)
                .build();
    }

    /**
     * Holt ein gültiges Admin-Token, entweder aus dem Cache oder durch Abrufen von Keycloak.
     *
     * @return Admin-Token als String
     */
    private String getAdminToken() {
        return tokenCache.get("adminToken", key -> {
            try {
                KeycloakTokenResponse tokenResponse = keycloakTokenService.serviceAccountLogin(
                        realm,
                        clientId,
                        clientSecret,
                        "client_credentials",
                        "openid"
                );
                LOGGER.info("Admin-Token erfolgreich abgerufen und im Cache gespeichert.");
                return "Bearer " + tokenResponse.access_token();
            } catch (Exception e) {
                LOGGER.error("Fehler beim Abrufen des Admin-Tokens", e);
                throw new RuntimeException("Fehler beim Abrufen des Admin-Tokens", e);
            }
        });
    }

    /**
     * Sucht Benutzer in Keycloak und konvertiert sie in ContactSearchDTO.
     *
     * @param searchTerm Suchbegriff
     * @param maxResults Maximale Anzahl der Ergebnisse
     * @return Liste der gefundenen Benutzer als ContactSearchDTO
     */
    public List<ContactSearchDTO> searchUserAsContactSearchDTO(String searchTerm, int maxResults) {
        try {
            String token = getAdminToken();
            List<KeycloakUserMapperDTO> keycloakUsers = keycloakAdminClient.searchUsers(token, realm, searchTerm, maxResults);
            LOGGER.info("Gefundene Keycloak-Benutzer: " + keycloakUsers);
            return keycloakUsers.stream()
                    .map(user -> new ContactSearchDTO(
                            user.id(),
                            user.firstName(),
                            user.lastName(),
                            user.email()
                    ))
                    .collect(Collectors.toList());
        } catch (WebApplicationException e) {
            int status = e.getResponse().getStatus();
            if (status == 404) {
                LOGGER.errorf("Realm oder Endpunkt nicht gefunden: Status %d", status);
            } else if (status == 401 || status == 403) {
                LOGGER.errorf("Nicht autorisiert: Status %d", status);
            } else {
                LOGGER.errorf("Fehler beim Suchen von Nutzern: Status %d", status);
            }
            throw new RuntimeException("Fehler beim Suchen von Nutzern", e);
        } catch (Exception e) {
            LOGGER.error("Unbekannter Fehler beim Suchen von Nutzern", e);
            throw new RuntimeException("Unbekannter Fehler beim Suchen von Nutzern", e);
        }
    }

    /**
     * Findet einen Benutzer anhand der Benutzer-ID und gibt ein ContactSearchDTO zurück.
     *
     * @param userId Benutzer-ID
     * @return ContactSearchDTO-Objekt oder null, wenn nicht gefunden
     */
    public ContactSearchDTO findUserAsContactSearchDTO(String userId) {
        try {
            String token = getAdminToken();
            KeycloakUserMapperDTO user = keycloakAdminClient.findUser(token, realm, userId);
            if (user == null) {
                return null;
            }
            return new ContactSearchDTO(
                    user.id(),
                    user.firstName(),
                    user.lastName(),
                    user.email()
            );
        } catch (WebApplicationException e) {
            int status = e.getResponse().getStatus();
            if (status == 404) {
                LOGGER.errorf("Realm oder Endpunkt nicht gefunden: Status %d", status);
            } else if (status == 401 || status == 403) {
                LOGGER.errorf("Nicht autorisiert: Status %d", status);
            } else {
                LOGGER.errorf("Fehler beim Suchen von Nutzern: Status %d", status);
            }
            throw new RuntimeException("Fehler beim Suchen von Nutzern", e);
        } catch (Exception e) {
            LOGGER.error("Unbekannter Fehler beim Suchen von Nutzern", e);
            throw new RuntimeException("Unbekannter Fehler beim Suchen von Nutzern", e);
        }
    }
}
