package at.htlleonding.leomail.resources;

import at.htlleonding.leomail.entities.Template;
import at.htlleonding.leomail.entities.TemplateGreeting;
import at.htlleonding.leomail.model.dto.TemplateDTO;
import at.htlleonding.leomail.model.exceptions.account.ContactExistsInKeycloakException;
import at.htlleonding.leomail.model.exceptions.greeting.NonExistingGreetingException;
import at.htlleonding.leomail.model.exceptions.template.TemplateNameAlreadyExistsException;
import at.htlleonding.leomail.repositories.TemplateRepository;
import at.htlleonding.leomail.services.PermissionService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("template")
public class TemplateResource {

    @Inject
    TemplateRepository templateRepository;

    @Inject
    JsonWebToken jwt;

    @Inject
    PermissionService permissionService;

    @GET
    @Path("/get")
    @Authenticated
    public Response getProjectTemplates(@QueryParam("pid") String projectId) {
        return Response.ok(templateRepository.getProjectTemplates(projectId)).build();
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
        try {
            return Response.ok(templateRepository.addTemplate(templateDTO, jwt.getClaim("sub"))).build();
        } catch (TemplateNameAlreadyExistsException excp) {
            return Response.status(409).entity("E-Template-01").build();
        } catch (ContactExistsInKeycloakException excp) {
            return Response.status(409).entity("E-Template-02").build();
        } catch (NonExistingGreetingException excp) {
            return Response.status(409).entity("E-Template-03").build();
        } catch (IllegalArgumentException e) {
            return Response.status(418).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("getById")
    @Authenticated
    public Response getById(Long id) {
        try {
            if(permissionService.hasPermission(id, jwt.getClaim("sub"))) {
                return Response.ok(Template.findById(id)).build();
            }
            return Response.status(403).build();
        } catch (IllegalArgumentException e) {
            return Response.status(418).build();
        }
    }

    @DELETE
    @Transactional
    @Path("delete")
    @Authenticated
    public Response deleteById(@QueryParam("tid") Long tid, @QueryParam("pid") String pid) {
        try {
            if (permissionService.hasPermission(pid, jwt.getClaim("sub"), tid)) {
                templateRepository.deleteById(tid);
            } else {
                return Response.status(403).build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(418).build();
        }
        return Response.ok().build();
    }

    @POST
    @Transactional
    @Path("update")
    @Authenticated
    public Response updateTemplate(TemplateDTO templateDTO) {
        try {
            if (permissionService.hasPermission(templateDTO.projectId(), jwt.getClaim("sub"), templateDTO.id())) {
                return Response.ok(templateRepository.updateTemplate(templateDTO)).build();
            } else {
                return Response.status(403).build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(418).build();
        }
    }

    @GET
    @Path("getUsedTemplates")
    @Authenticated
    public Response getScheduledUsedTemplates(@QueryParam("scheduled") boolean scheduled, @QueryParam("pid") String pid) {
        if(!permissionService.hasPermission(pid, jwt.getClaim("sub"))){
            return Response.status(403).build();
        }
        return Response.ok(templateRepository.getUsedTemplates(scheduled, pid)).build();
    }

    @GET
    @Path("getUsedTemplate")
    @Authenticated
    public Response getUsedTemplate(@QueryParam("tid") Long tid, @QueryParam("pid") String pid) {
        return Response.ok(templateRepository.getUsedTemplate(tid, pid, jwt.getClaim("sub"))).build();
    }
}