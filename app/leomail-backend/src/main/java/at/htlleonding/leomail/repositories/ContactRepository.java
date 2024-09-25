package at.htlleonding.leomail.repositories;

import at.htlleonding.leomail.entities.Contact;
import at.htlleonding.leomail.model.dto.contacts.ContactSearchDTO;
import at.htlleonding.leomail.model.dto.contacts.ContactAddDTO;
import at.htlleonding.leomail.model.dto.contacts.ContactDetailDTO;
import at.htlleonding.leomail.model.exceptions.ObjectContainsNullAttributesException;
import at.htlleonding.leomail.model.exceptions.account.ContactExistsInKeycloakException;
import at.htlleonding.leomail.services.KeycloakAdminService;
import at.htlleonding.leomail.services.Utilities;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@ApplicationScoped
public class ContactRepository {

    private static final Logger LOGGER = Logger.getLogger(ContactRepository.class);

    @Inject
    KeycloakAdminService keycloakAdminService;

    @ConfigProperty(name = "leomail.user.search.max")
    int maxResults;

    /**
     * Sucht Kontakte basierend auf einem Suchbegriff.
     *
     * @param searchTerm    Suchbegriff
     * @param keycloakOnly  Wenn true, werden nur Keycloak-Nutzer gesucht
     * @param own           Die eigene Benutzer-ID, um diese auszuschließen
     * @return Liste der gefundenen Kontakte
     */
    public List<ContactSearchDTO> searchContacts(String searchTerm, boolean keycloakOnly, String own) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            searchTerm = "";
        }

        String finalSearchTerm = searchTerm.trim();

        CompletableFuture<List<ContactSearchDTO>> keycloakFuture = CompletableFuture.supplyAsync(() -> {
            try {
                List<ContactSearchDTO> users = keycloakAdminService.searchUserAsContactSearchDTO(finalSearchTerm, maxResults);
                LOGGER.info("Keycloak users: " + users);
                return users;
            } catch (RuntimeException e) {
                LOGGER.error("Fehler beim Suchen von Nutzern: " + e.getMessage(), e);
                return Collections.emptyList();
            }
        });

        try {
            List<ContactSearchDTO> list = keycloakFuture.get().stream()
                    .filter(contact -> !contact.id().equals(own))
                    .collect(Collectors.toList());

            if (!keycloakOnly) {
                List<Contact> contacts = Contact.find(
                        "firstName like ?1 or lastName like ?1 or mailAddress like ?1 and kcUser is false",
                        "%" + finalSearchTerm + "%"
                ).list();

                List<ContactSearchDTO> contactList = contacts.stream()
                        .map(contact -> new ContactSearchDTO(
                                contact.id,
                                contact.firstName,
                                contact.lastName,
                                contact.mailAddress
                        ))
                        .collect(Collectors.toList());

                list.addAll(contactList);
            }

            return list;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Thread interrupted while searching contacts", e);
            throw new RuntimeException("Thread interrupted while searching contacts", e);
        } catch (ExecutionException e) {
            LOGGER.error("Error executing Keycloak user search", e);
            throw new RuntimeException("Error executing Keycloak user search", e);
        }
    }


    /**
     * Fügt einen neuen Kontakt hinzu.
     *
     * @param contactDTO Daten des zu hinzufügenden Kontakts
     */
    @Transactional
    public void addContact(ContactAddDTO contactDTO) {
        // Überprüfung, ob der Kontakt bereits in Keycloak existiert
        List<ContactSearchDTO> existingUsers = keycloakAdminService.searchUserAsContactSearchDTO(contactDTO.mailAddress(), 1);
        if (!existingUsers.isEmpty()) {
            throw new ContactExistsInKeycloakException("Contact already exists in Keycloak");
        }

        // Überprüfung, ob die Mailadresse bereits in der lokalen Datenbank existiert
        long count = Contact.count("mailAddress", contactDTO.mailAddress());
        if (count > 0) {
            throw new ContactExistsInKeycloakException("Mail address already exists in database");
        }

        // Überprüfung auf null-Felder
        List<String> nullFields = Utilities.listNullFields(contactDTO, List.of("id", "suffixTitle", "prefixTitle", "positionAtCompany", "company"));
        if (!nullFields.isEmpty()) {
            throw new ObjectContainsNullAttributesException(nullFields);
        }

        // Setzen leerer Strings auf null
        Utilities.setEmptyStringsToNull(contactDTO);

        // Erstellung und Persistierung des neuen Kontakts
        Contact contact = new Contact(
                contactDTO.firstName().trim(),
                contactDTO.lastName().trim(),
                contactDTO.mailAddress().trim(),
                contactDTO.prefixTitle() != null ? contactDTO.prefixTitle().trim() : null,
                contactDTO.suffixTitle() != null ? contactDTO.suffixTitle().trim() : null,
                contactDTO.positionAtCompany() != null ? contactDTO.positionAtCompany().trim() : null,
                contactDTO.company() != null ? contactDTO.company().trim() : null,
                contactDTO.gender()
        );
        contact.persist();
        LOGGER.infof("Neuer Kontakt '%s %s' erfolgreich hinzugefügt.", contact.firstName, contact.lastName);
    }

    /**
     * Löscht einen Kontakt anhand der ID.
     *
     * @param id ID des zu löschenden Kontakts
     */
    @Transactional
    public void deleteContact(String id) {
        Contact contact = Contact.findById(id);
        if (contact == null) {
            throw new IllegalArgumentException("Contact not found");
        }
        contact.delete();
        LOGGER.infof("Kontakt mit ID %s erfolgreich gelöscht.", id);
    }

    /**
     * Holt die Details eines Kontakts anhand der ID.
     *
     * @param id ID des Kontakts
     * @return Details des Kontakts
     */
    public ContactDetailDTO getContact(String id) {
        Optional<Contact> contactOpt = Contact.findByIdOptional(id);
        if (contactOpt.isEmpty()) {
            ContactSearchDTO user = findUserAsContactSearchDTO(id);
            if (user == null) {
                throw new IllegalArgumentException("User not found in Keycloak");
            }
            return new ContactDetailDTO(
                    user.id(),
                    user.firstName(),
                    user.lastName(),
                    user.mailAddress(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    true
            );
        }

        Contact contact = contactOpt.get();
        return new ContactDetailDTO(
                contact.id,
                contact.firstName,
                contact.lastName,
                contact.mailAddress,
                contact.gender,
                contact.suffixTitle,
                contact.prefixTitle,
                contact.company,
                contact.positionAtCompany,
                contact.kcUser
        );
    }

    /**
     * Aktualisiert einen bestehenden Kontakt.
     *
     * @param contactDTO Daten des zu aktualisierenden Kontakts
     */
    @Transactional
    public void updateContact(ContactDetailDTO contactDTO) {
        Contact contact = Contact.findById(contactDTO.id());
        if (contact == null) {
            throw new IllegalArgumentException("Contact not found");
        }
        if (contact.kcUser) {
            throw new IllegalArgumentException("Cannot update Keycloak user");
        }
        contact.firstName = contactDTO.firstName().trim();
        contact.lastName = contactDTO.lastName().trim();
        contact.mailAddress = contactDTO.mailAddress().trim();
        contact.gender = contactDTO.gender();
        contact.suffixTitle = contactDTO.suffixTitle() != null ? contactDTO.suffixTitle().trim() : null;
        contact.prefixTitle = contactDTO.prefixTitle() != null ? contactDTO.prefixTitle().trim() : null;
        contact.company = contactDTO.company() != null ? contactDTO.company().trim() : null;
        contact.positionAtCompany = contactDTO.positionAtCompany() != null ? contactDTO.positionAtCompany().trim() : null;
        LOGGER.infof("Kontakt mit ID %s erfolgreich aktualisiert.", contactDTO.id());
    }

    /**
     * Speichert einen Keycloak-Benutzer lokal in der Datenbank.
     *
     * @param user ContactSearchDTO Objekt
     * @return Persistiertes Contact-Objekt
     */
    @Transactional
    public Contact saveKeycloakUserLocally(ContactSearchDTO user) {
        Contact contact = new Contact(user.id(), user.firstName(), user.lastName(), user.mailAddress(), true);
        contact.persist();
        LOGGER.infof("Keycloak-Benutzer '%s %s' erfolgreich lokal gespeichert.", user.firstName(), user.lastName());
        return contact;
    }

    /**
     * Findet einen Benutzer anhand der Benutzer-ID und gibt ein ContactSearchDTO zurück.
     *
     * @param userId Benutzer-ID
     * @return ContactSearchDTO-Objekt oder null, wenn nicht gefunden
     */
    public ContactSearchDTO findUserAsContactSearchDTO(String userId) {
        try {
            return keycloakAdminService.findUserAsContactSearchDTO(userId);
        } catch (RuntimeException e) {
            LOGGER.warnf("User with ID %s not found in Keycloak.", userId);
            return null;
        }
    }
}
