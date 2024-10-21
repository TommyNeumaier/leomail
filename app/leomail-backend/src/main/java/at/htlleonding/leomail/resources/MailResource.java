package at.htlleonding.leomail.resources;

import at.htlleonding.leomail.model.SMTPInformation;
import at.htlleonding.leomail.repositories.MailRepository;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("mail")
public class MailResource {

    @Inject
    MailRepository repository;

    @Inject
    JsonWebToken jwt;

    @POST
    @Path("sendByTemplate")
    @Transactional
    @Authenticated
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response sendMailByTemplate(@QueryParam("pid") String projectId, SMTPInformation smtpInformation) {
        repository.sendMailsByTemplate(projectId, jwt.getClaim("sub"), smtpInformation);
        return Response.ok("Emails sent successfully").build();
    }
}