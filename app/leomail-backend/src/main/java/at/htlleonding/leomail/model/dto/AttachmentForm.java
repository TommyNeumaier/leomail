package at.htlleonding.leomail.model.dto;

import org.jboss.resteasy.annotations.providers.multipart.PartType;

import jakarta.ws.rs.FormParam;
import java.io.InputStream;

public class AttachmentForm {

    @FormParam("file")
    @PartType("application/octet-stream")
    public InputStream file;

    @FormParam("fileName")
    @PartType("text/plain")
    public String fileName;

    @FormParam("contentType")
    @PartType("text/plain")
    public String contentType;

    @FormParam("size")
    @PartType("text/plain")
    public long size;
}