package at.htlleonding.leomail.resources;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("mail")
public class MailResource {

    @Inject
    Mailer mailer;

    @GET
    @Path("send")
    public void sendMail() {
        mailer.send(Mail.withText("ls@tommyneumaier.at", "Test", "Hello World"));
    }
}
