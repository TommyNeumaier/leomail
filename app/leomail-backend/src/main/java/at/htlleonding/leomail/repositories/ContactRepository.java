package at.htlleonding.leomail.repositories;

import at.htlleonding.leomail.entities.Contact;
import at.htlleonding.leomail.model.dto.contacts.ContactDTO;
import at.htlleonding.leomail.services.KeycloakAdminService;
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

    @Transactional
    public List<ContactDTO> searchContacts(String searchTerm) {
        CompletableFuture<List<Contact>> contactFuture = CompletableFuture.supplyAsync(() ->
                Contact.find("firstName like ?1 or lastName like ?1 or mailAddress like ?1", "%" + searchTerm + "%")
                        .page(0, 10)
                        .list(), managedExecutor
        );

        CompletableFuture<List<Object>> keycloakFuture = CompletableFuture.supplyAsync(() ->
                keycloakAdminService.searchUser(searchTerm, maxResults), managedExecutor
        );

        try {
            List<Contact> contacts = contactFuture.get();
            List<Object> keycloakUsers = keycloakFuture.get();

            List<ContactDTO> combinedResults = contacts.stream()
                    .map(contact -> new ContactDTO(contact.id, contact.firstName, contact.lastName, contact.mailAddress))
                    .collect(Collectors.toList());

            for (Object keycloakUser : keycloakUsers) {
                 Map<String, Object> userMap = (Map<String, Object>) keycloakUser;
                 String id = (String) userMap.get("id");
                 String firstName = (String) userMap.get("firstName");
                 String lastName = (String) userMap.get("lastName");
                 String mailAddress = (String) userMap.get("email");
            combinedResults.add(new ContactDTO(id, firstName, lastName, mailAddress));
             }

            return combinedResults;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error during parallel search", e);
        }
    }
}
