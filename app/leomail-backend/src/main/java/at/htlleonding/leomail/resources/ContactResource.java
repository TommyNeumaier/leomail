package at.htlleonding.leomail.resources;

import at.htlleonding.leomail.model.dto.contacts.ContactDTO;
import at.htlleonding.leomail.repositories.ContactRepository;
import at.htlleonding.leomail.services.KeycloakAdminService;
import io.quarkus.cache.CacheResult;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
@Path("/users")
public class ContactResource {

    @Inject
    ContactRepository contactRepository;

    @GET
    @Path("/search")
    @Transactional
    @CacheResult(cacheName = "contact-search")
    public Response searchContacts(@QueryParam("query") String searchTerm) {
        List<ContactDTO> results = contactRepository.searchContacts(searchTerm);
        return Response.ok(results).build();
    }
}
