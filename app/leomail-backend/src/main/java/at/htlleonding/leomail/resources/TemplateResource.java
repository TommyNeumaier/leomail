package at.htlleonding.leomail.resources;

import at.htlleonding.leomail.model.dto.TemplateDTO;
import at.htlleonding.leomail.repositories.TemplateRepository;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("template")
public class TemplateResource {

    @Inject
    TemplateRepository templateRepository;

    @GET
    @Path("all")
    public Response getAllTemplates() {
        return Response.ok(templateRepository.getAllTemplates()).build();
    }

    @GET
    @Path("greetings")
    public Response getAllGreetings() {
        return Response.ok(templateRepository.getAllGreetings()).build();
    }

    @POST
    @Transactional
    @Path("add")
    public Response addTemplate(TemplateDTO templateDTO) {
        return Response.ok(templateRepository.addTemplate(templateDTO)).build();
    }
}
