package at.htlleonding.leomail.services;

import at.htlleonding.leomail.entities.NaturalContact;
import at.htlleonding.leomail.model.dto.contacts.NaturalContactSearchDTO;
import at.htlleonding.leomail.repositories.ContactRepository;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class KeycloakAdminService {

    private static final Logger LOGGER = Logger.getLogger(KeycloakAdminService.class);

    @Inject
    Keycloak keycloakClient;

    @ConfigProperty(name = "quarkus.keycloak.admin-client.realm")
    String realm;

    @Inject
    ContactRepository contactRepository;

    /**
     * Searches for users in Keycloak and converts them to NaturalContactSearchDTO.
     *
     * @param searchTerm Search term
     * @param maxResults Maximum number of results
     * @return List of found users as NaturalContactSearchDTO
     */
    public List<NaturalContactSearchDTO> searchUserAsNaturalContactSearchDTO(String searchTerm, int maxResults) {
        try {
            List<UserRepresentation> keycloakUsers = keycloakClient.realm(realm)
                    .users().search(searchTerm, 0, maxResults);
            LOGGER.info("Found Keycloak users: " + keycloakUsers);
            return keycloakUsers.stream()
                    .map(user -> new NaturalContactSearchDTO(
                            user.getId(),
                            user.getFirstName(),
                            user.getLastName(),
                            user.getEmail()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.error("Error searching for users", e);
            throw new RuntimeException("Error searching for users", e);
        }
    }

    /**
     * Finds a user by user ID and returns a NaturalContactSearchDTO.
     *
     * @param userId User ID
     * @return NaturalContactSearchDTO object or null if not found
     */
    public NaturalContactSearchDTO findUserAsNaturalContactSearchDTO(String userId) {
        try {
            UserRepresentation user = keycloakClient.realm(realm)
                    .users().get(userId).toRepresentation();
            if (user == null) {
                return null;
            }
            return new NaturalContactSearchDTO(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail()
            );
        } catch (Exception e) {
            LOGGER.error("Error finding user", e);
            throw new RuntimeException("Error finding user", e);
        }
    }

    /**
     * Saves all users from Keycloak to the application database.
     */
    @PostConstruct
    public void saveAllUsersToAppDB() {
        int first = 0;
        int max = 100;
        List<UserRepresentation> usersBatch;
        do {
            usersBatch = keycloakClient.realm(realm).users().list(first, max);
            for (UserRepresentation user : usersBatch) {
                saveOrUpdateKeycloakUser(user);
            }
            first += max;
        } while (!usersBatch.isEmpty());
        LOGGER.info("All Keycloak users have been saved to the application database.");
    }

    /**
     * Saves or updates a Keycloak user in the application database.
     *
     * @param user UserRepresentation to be saved or updated
     */
    public void saveOrUpdateKeycloakUser(UserRepresentation user) {
        // Validierung der E-Mail-Adresse
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            LOGGER.warnf("User '%s' hat keine gültige E-Mail-Adresse. Überspringe das Speichern.", user.getId());
            return; // Oder entscheiden Sie, ob Sie eine Ausnahme werfen möchten
        }

        // Überprüfen, ob der Benutzer bereits existiert
        NaturalContact existingContact = NaturalContact.findById(user.getId());
        if (existingContact != null) {
            // Aktualisieren vorhandener Kontakt
            existingContact.firstName = user.getFirstName() != null ? user.getFirstName() : "";
            existingContact.lastName = user.getLastName() != null ? user.getLastName() : "";
            existingContact.mailAddress = user.getEmail();
            existingContact.persist();
            LOGGER.infof("Updated existing Keycloak user '%s' in application database.", existingContact.id);
        } else {
            NaturalContact contact = new NaturalContact();
            contact.id = user.getId();
            contact.firstName = user.getFirstName() != null ? user.getFirstName() : "";
            contact.lastName = user.getLastName() != null ? user.getLastName() : "";
            contact.mailAddress = user.getEmail();
            contact.kcUser = true;

            // Optional: Weitere Felder setzen, z.B. Projektzuordnung
            // contact.project = Project.findById(projectId);

            contact.persist();
            LOGGER.infof("Saved new Keycloak user '%s' to application database.", contact.id);
        }
    }

}