package at.htlleonding.leomail.resources;

import at.htlleonding.leomail.entities.Template;
import at.htlleonding.leomail.entities.TemplateGreeting;
import at.htlleonding.leomail.model.dto.TemplateDTO;
import at.htlleonding.leomail.repositories.TemplateRepository;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

@Path("template")
public class TemplateResource {

    @Inject
    TemplateRepository templateRepository;

    @GET
    @Path("all")
    @Authenticated
    public Response getAllTemplates() {
        return Response.ok(templateRepository.getAllTemplates()).build();
    }

    @GET
    @Path("greetings")
    @Authenticated
    public Response getAllGreetings() {
        return Response.ok(templateRepository.getAllGreetings()).build();
    }

    @GET
    @Path("greeting")
    @Authenticated
    public Response getGreetingById(@QueryParam("gid") Long id) {
        return Response.ok(TemplateGreeting.findById(id)).build();
    }

    @POST
    @Transactional
    @Path("add")
    @Authenticated
    public Response addTemplate(TemplateDTO templateDTO) {
        return Response.ok(templateRepository.addTemplate(templateDTO)).build();
    }

    @GET
    @Path("getById")
    @Authenticated
    public Response getById(Long id) {
        return Response.ok(Template.findById(id)).build();
    }

    @DELETE
    @Transactional
    @Path("delete")
    @Authenticated
    public Response deleteById(@QueryParam("tid") Long id) {
        try {
            templateRepository.deleteById(id);
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok().build();
    }

    @POST
    @Transactional
    @Path("update")
    @Authenticated
    public Response updateTemplate(TemplateDTO templateDTO) {
        try {
            return Response.ok(templateRepository.updateTemplate(templateDTO)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}