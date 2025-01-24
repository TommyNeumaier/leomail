package at.htlleonding.leomail.model.dto;

import jakarta.ws.rs.FormParam;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

import java.io.InputStream;

public record AttachmentDTO(
        @FormParam("attachments") @PartType("*/*") InputStream fileStream,
        @FormParam("fileName") @PartType("text/plain") String fileName,
        @FormParam("contentType") @PartType("text/plain") String contentType,
        @FormParam("size") @PartType("text/plain") Long size
) {}
