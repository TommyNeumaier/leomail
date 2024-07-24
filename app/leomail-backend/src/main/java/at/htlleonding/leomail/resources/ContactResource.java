package at.htlleonding.leomail.resources;

import at.htlleonding.leomail.model.dto.contacts.ContactDTO;
import at.htlleonding.leomail.model.exceptions.account.ContactExistsInKeycloakException;
import at.htlleonding.leomail.repositories.ContactRepository;
import io.quarkus.cache.CacheResult;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
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
    public Response searchContacts() {
        List<ContactDTO> results = contactRepository.searchContacts(null);
        return Response.ok(results).build();
    }

    @GET
    @Path("/search")
    @Transactional
    @CacheResult(cacheName = "contact-search")
    public Response searchContacts(@QueryParam("query") String searchTerm) {
        List<ContactDTO> results = contactRepository.searchContacts(searchTerm);
        return Response.ok(results).build();
    }

    @POST
    @Path("/add")
    @Transactional
    @Authenticated
    public Response addContact(ContactDTO contactDTO) {
        try {
            contactRepository.addContact(contactDTO);
            return Response.ok().build();
        } catch (ContactExistsInKeycloakException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/delete")
    @Transactional
    @Authenticated
    public Response deleteContact(@QueryParam("id") String id) {
        try {
            contactRepository.deleteContact(id);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}