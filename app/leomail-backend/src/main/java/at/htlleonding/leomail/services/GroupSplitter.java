package at.htlleonding.leomail.services;

import at.htlleonding.leomail.entities.Contact;
import at.htlleonding.leomail.entities.Group;
import at.htlleonding.leomail.repositories.ContactRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.LinkedList;
import java.util.List;

@ApplicationScoped
public class GroupSplitter {

    @Inject
    KeycloakAdminService keycloakAdminService;

    @Inject
    ContactRepository contactRepository;

    public List<Contact> getAllContacts(List<Group> groups, List<String> contacts) {
        List<Contact> contactList = new LinkedList<>();
        for (String contactId : contacts) {
            Contact contact = Contact.findById(contactId);
            if (contact == null) {
                Object object = keycloakAdminService.findUser(contactId);

                if (object != null) {
                    contact = contactRepository.saveKeycloakUserLocally(object);
                }
            }
            contactList.add(contact);
        }

        List<Contact> allContacts = new LinkedList<>(contactList);
        for (Group group : groups) {
            for (Contact contact : group.members) {
                if (!allContacts.contains(contact)) {
                    allContacts.add(contact);
                }
            }
        }
        return allContacts;
    }
}
