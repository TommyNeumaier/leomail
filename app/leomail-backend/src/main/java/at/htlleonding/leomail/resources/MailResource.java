package at.htlleonding.leomail.resources;

import at.htlleonding.leomail.model.SMTPInformation;
import at.htlleonding.leomail.repositories.MailRepository;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("mail")
public class MailResource {

    @Inject
    MailRepository repository;

    @POST
    @Path("sendByTemplate")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response sendMailByTemplate(SMTPInformation smtpInformation) {
        repository.sendMailsByTemplate(smtpInformation);
        return Response.ok("Emails sent successfully").build();
    }
}