package at.htlleonding.leomail.services;

import at.htlleonding.leomail.entities.Contact;
import at.htlleonding.leomail.entities.Group;
import at.htlleonding.leomail.model.dto.contacts.ContactSearchDTO;
import at.htlleonding.leomail.repositories.ContactRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class GroupSplitter {

    private static final Logger LOGGER = Logger.getLogger(GroupSplitter.class);

    @Inject
    KeycloakAdminService keycloakAdminService;

    @Inject
    ContactRepository contactRepository;

    /**
     * Holt alle Kontakte basierend auf den angegebenen Gruppen und Kontakt-IDs.
     *
     * @param groups   Liste der Gruppen-IDs
     * @param contacts Liste der Kontakt-IDs
     * @return Liste aller relevanten Kontakte
     */
    public List<Contact> getAllContacts(List<String> groups, List<String> contacts) {
        List<Contact> contactList = contacts.stream()
                .map(contactId -> {
                    Contact contact = Contact.findById(contactId);
                    if (contact == null) {
                        ContactSearchDTO user = keycloakAdminService.findUserAsContactSearchDTO(contactId);
                        if (user != null) {
                            contact = contactRepository.saveKeycloakUserLocally(user);
                        } else {
                            LOGGER.warnf("Benutzer mit ID %s nicht in Keycloak gefunden.", contactId);
                        }
                    }
                    return contact;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Set<Contact> allContacts = new HashSet<>(contactList);

        for (String groupId : groups) {
            Group groupEntity = Group.findById(groupId);
            if (groupEntity != null && groupEntity.members != null) {
                for (Contact member : groupEntity.members) {
                    allContacts.add(member);
                }
            } else {
                LOGGER.warnf("Gruppe mit ID %s nicht gefunden oder hat keine Mitglieder.", groupId);
            }
        }

        return new ArrayList<>(allContacts);
    }
}