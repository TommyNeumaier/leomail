package at.htlleonding.leomail.repositories;

import at.htlleonding.leomail.entities.Contact;
import at.htlleonding.leomail.model.dto.contacts.ContactAddDTO;
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

import java.util.List;
import java.util.Map;
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

    public List<ContactSearchDTO> searchContacts(String searchTerm) {
        CompletableFuture<List<Contact>> contactFuture = CompletableFuture.supplyAsync(() ->
                Contact.find("firstName like ?1 or lastName like ?1 or mailAddress like ?1", "%" + searchTerm + "%")
                        .page(0, maxResults)
                        .list(), managedExecutor
        );

        CompletableFuture<List<Object>> keycloakFuture = CompletableFuture.supplyAsync(() ->
                keycloakAdminService.searchUser(searchTerm, maxResults), managedExecutor
        );

        try {
            List<Contact> contacts = contactFuture.get();
            List<Object> keycloakUsers = keycloakFuture.get();

            List<ContactSearchDTO> combinedResults = contacts.stream()
                    .map(contact -> new ContactSearchDTO(contact.id, contact.firstName, contact.lastName, contact.mailAddress))
                    .collect(Collectors.toList());

            for (Object keycloakUser : keycloakUsers) {
                 Map<String, Object> userMap = (Map<String, Object>) keycloakUser;
                 String id = (String) userMap.get("id");
                 String firstName = (String) userMap.get("firstName");
                 String lastName = (String) userMap.get("lastName");
                 String mailAddress = (String) userMap.get("email");
            combinedResults.add(new ContactSearchDTO(id, firstName, lastName, mailAddress));
             }

            return combinedResults;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error during parallel search", e);
        }
    }

    public void addContact(ContactAddDTO contactDTO) {
        if (!keycloakAdminService.searchUser(contactDTO.mailAddress(), 1).isEmpty())
            throw new ContactExistsInKeycloakException("in keycloak");

        if (Contact.find("mailAddress", contactDTO.mailAddress()).count() > 0)
            throw new ContactExistsInKeycloakException("mail already database entry");

        List<String> nullFields = Utilities.listNullFields(contactDTO, List.of("id"));
        if(!nullFields.isEmpty()) {
            throw new ObjectContainsNullAttributesException(nullFields);
        }

        Contact contact = new Contact(contactDTO.firstName(), contactDTO.lastName(), contactDTO.mailAddress(), contactDTO.prefixTitle(), contactDTO.suffixTitle(), contactDTO.company(), contactDTO.positionAtCompany(), contactDTO.gender());
        contact.persist();
    }

    public void deleteContact(String id) {
        Contact contact = Contact.findById(id);
        if (contact == null) {
            throw new IllegalArgumentException("Contact not found");
        }
        contact.delete();
    }
}