package at.htlleonding.leomail.repositories;

import at.htlleonding.leomail.entities.Contact;
import at.htlleonding.leomail.model.dto.contacts.ContactAddDTO;
import at.htlleonding.leomail.model.dto.contacts.ContactDetailDTO;
import at.htlleonding.leomail.model.dto.contacts.ContactSearchDTO;
import at.htlleonding.leomail.model.exceptions.ObjectContainsNullAttributesException;
import at.htlleonding.leomail.model.exceptions.account.ContactExistsInKeycloakException;
import at.htlleonding.leomail.services.KeycloakAdminService;
import at.htlleonding.leomail.services.Utilities;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@ApplicationScoped
public class ContactRepository {

    @Inject
    KeycloakAdminService keycloakAdminService;

    @Inject
    ManagedExecutor managedExecutor;

    @ConfigProperty(name = "leomail.user.search.max")
    int maxResults;

    public List<ContactSearchDTO> searchContacts(String searchTerm, boolean keycloakOnly, String own) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            searchTerm = "";
        }

        String finalSearchTerm = searchTerm.trim();

        CompletableFuture<List<Object>> keycloakFuture = CompletableFuture.supplyAsync(() -> {
            List<Object> users = keycloakAdminService.searchUser(finalSearchTerm, maxResults);
            System.out.println("Keycloak users: " + users);
            return users;
        }, managedExecutor);

        try {
            List<ContactSearchDTO> list = new java.util.ArrayList<>(keycloakFuture.get().stream()
                    .map(user -> {
                        Map<String, Object> userMap = (Map<String, Object>) user;
                        String id = (String) userMap.get("id");
                        String firstName = (String) userMap.get("firstName");
                        String lastName = (String) userMap.get("lastName");
                        String mailAddress = (String) userMap.get("email");

                        if (id == null || firstName == null || lastName == null || mailAddress == null) {
                            return null;
                        }

                        return new ContactSearchDTO(id, firstName, lastName, mailAddress);
                    })
                    .filter(Objects::nonNull)
                    .filter(contact -> !contact.id().equals(own))
                    .toList());

            if (!keycloakOnly) {
                List<Contact> contacts = Contact.find("firstName like ?1 or lastName like ?1 or mailAddress like ?1 and kcUser is false", "%" + searchTerm + "%").list();
                List<ContactSearchDTO> contactList = contacts.stream()
                        .map(contact -> new ContactSearchDTO(contact.id, contact.firstName, contact.lastName, contact.mailAddress))
                        .toList();

                list.addAll(contactList);
            }

            return list;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void addContact(ContactAddDTO contactDTO) {
        if (!keycloakAdminService.searchUser(contactDTO.mailAddress(), 1).isEmpty())
            throw new ContactExistsInKeycloakException("in keycloak");

        if (Contact.find("mailAddress", contactDTO.mailAddress()).count() > 0)
            throw new ContactExistsInKeycloakException("mail already database entry");

        List<String> nullFields = Utilities.listNullFields(contactDTO, List.of("id", "suffixTitle", "prefixTitle", "positionAtCompany", "company"));
        if(!nullFields.isEmpty()) {
            throw new ObjectContainsNullAttributesException(nullFields);
        }

        Utilities.setEmptyStringsToNull(contactDTO);

        Contact contact = new Contact(contactDTO.firstName().trim(), contactDTO.lastName().trim(), contactDTO.mailAddress().trim(), contactDTO.prefixTitle() == null ? null : contactDTO.prefixTitle().trim(), contactDTO.suffixTitle() == null ? null : contactDTO.suffixTitle().trim(), contactDTO.positionAtCompany() == null ? null : contactDTO.positionAtCompany().trim(), contactDTO.company() == null ? null : contactDTO.company().trim(), contactDTO.gender());
        contact.persist();
    }

    public void deleteContact(String id) {
        Contact contact = Contact.findById(id);
        if (contact == null) {
            throw new IllegalArgumentException("Contact not found");
        }
        contact.delete();
    }

    public ContactDetailDTO getContact(String id) {
        Optional<Contact> contact = Contact.findByIdOptional(id);
        if (contact.isEmpty()) {
            Object user = keycloakAdminService.findUser(id);
            Map<String, Object> userMap = (Map<String, Object>) user;
            String firstName = (String) userMap.get("firstName");
            String lastName = (String) userMap.get("lastName");
            String mailAddress = (String) userMap.get("email");
            return new ContactDetailDTO(id, firstName, lastName, mailAddress, null, null, null, null, null, true);
        }
        return new ContactDetailDTO(contact.get().id, contact.get().firstName, contact.get().lastName, contact.get().mailAddress, contact.get().gender, contact.get().suffixTitle, contact.get().prefixTitle, contact.get().company, contact.get().positionAtCompany, contact.get().kcUser);
    }

    public void updateContact(ContactDetailDTO contactDTO) {
        Contact contact = Contact.findById(contactDTO.id());
        if (contact == null) {
            throw new IllegalArgumentException("Contact not found");
        }
        if (contact.kcUser) {
            throw new IllegalArgumentException("Cannot update keycloak user");
        }
        contact.firstName = contactDTO.firstName();
        contact.lastName = contactDTO.lastName();
        contact.mailAddress = contactDTO.mailAddress();
        contact.gender = contactDTO.gender();
        contact.suffixTitle = contactDTO.suffixTitle();
        contact.prefixTitle = contactDTO.prefixTitle();
        contact.company = contactDTO.company();
        contact.positionAtCompany = contactDTO.positionAtCompany();
    }

    @Transactional
    public Contact saveKeycloakUserLocally(String id, String firstName, String lastName, String mailAddress) {
        Contact contact = new Contact(id, firstName, lastName, mailAddress, true);
        contact.persist();
        return contact;
    }

    @Transactional
    public Contact saveKeycloakUserLocally(Object user) {
        Map<String, Object> userMap = (Map<String, Object>) user;
        String id = (String) userMap.get("id");
        String firstName = (String) userMap.get("firstName");
        String lastName = (String) userMap.get("lastName");
        String mailAddress = (String) userMap.get("email");
        Contact contact = new Contact(id, firstName, lastName, mailAddress, true);
        contact.persist();
        return contact;
    }
}