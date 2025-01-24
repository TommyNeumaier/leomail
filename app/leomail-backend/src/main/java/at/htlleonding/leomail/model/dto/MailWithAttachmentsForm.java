package at.htlleonding.leomail.model.dto;

import org.jboss.resteasy.annotations.providers.multipart.PartType;
import jakarta.ws.rs.FormParam;
import java.io.InputStream;
import java.util.List;

public class MailWithAttachmentsForm {
    @FormParam("projectId")
    @PartType("text/plain")
    public String projectId;

    @FormParam("smtpInformation")
    @PartType("application/json")
    public String smtpInformation;

    @FormParam("attachments")
    @PartType("*/*")
    public List<InputStream> attachments;

    @FormParam("fileName")
    @PartType("text/plain")
    public List<String> fileNames;

    @FormParam("contentType")
    @PartType("text/plain")
    public List<String> contentTypes;

    @FormParam("size")
    @PartType("text/plain")
    public List<Long> sizes;
}
