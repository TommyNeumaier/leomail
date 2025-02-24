package at.htlleonding.leomail.resources;

import at.htlleonding.leomail.entities.Attachment;
import at.htlleonding.leomail.model.SMTPInformation;
import at.htlleonding.leomail.repositories.MailRepository;
import at.htlleonding.leomail.services.PermissionService;
import at.htlleonding.leomail.services.StorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.*;

import static org.jboss.resteasy.plugins.providers.jaxb.i18n.LogMessages.LOGGER;

@Path("mail")
public class MailResource {

    @Inject
    MailRepository repository;

    @Inject
    JsonWebToken jwt;

    @Inject
    StorageService storageService;

    @Inject
    PermissionService permissionService;

    /**
     * Sendet E-Mails basierend auf einer Vorlage, SMTP-Informationen und Anhängen.
     */
    @POST
    @Path("sendByTemplateWithAttachments")
    @Transactional
    @Authenticated
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendMailByTemplateWithAttachments(MultipartFormDataInput input) {
        try {
            Map<String, List<InputPart>> uploadForm = input.getFormDataMap();

            // Extrahieren von projectId
            String projectId = getValue(uploadForm, "projectId");
            if (projectId == null || projectId.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("projectId fehlt")
                        .build();
            }

            String smtpInfoJson = getValue(uploadForm, "smtpInformation");
            SMTPInformation smtpInformation;
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                smtpInformation = mapper.readValue(smtpInfoJson, SMTPInformation.class);
            } catch (Exception e) {
                LOGGER.error("Ungültiges SMTP-Information JSON: ", e);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Ungültiges SMTP-Information JSON: " + e.getMessage())
                        .build();
            }

            // Extrahieren der Anhänge und deren Metadaten
            List<InputPart> attachmentParts = uploadForm.get("attachments");
            List<String> fileNames = getValues(uploadForm, "fileName");
            List<String> contentTypes = getValues(uploadForm, "contentType");
            List<String> sizesStr = getValues(uploadForm, "size");

            if (attachmentParts == null) {
                attachmentParts = new ArrayList<>();
            }
            int numAttachments = attachmentParts.size();
            List<String> safeFileNames = new ArrayList<>();
            List<String> safeContentTypes = new ArrayList<>();
            List<String> safeSizesStr = new ArrayList<>();

            for (int i = 0; i < numAttachments; i++) {
                if (fileNames != null && fileNames.size() > i) {
                    safeFileNames.add(fileNames.get(i));
                } else {
                    safeFileNames.add(extractFileName(attachmentParts.get(i)));
                }
                if (contentTypes != null && contentTypes.size() > i) {
                    safeContentTypes.add(contentTypes.get(i));
                } else {
                    safeContentTypes.add("application/octet-stream");
                }
                if (sizesStr != null && sizesStr.size() > i) {
                    safeSizesStr.add(sizesStr.get(i));
                } else {
                    // Fallback: -1 signalisiert, dass die Größe unbekannt ist
                    safeSizesStr.add("-1");
                }
            }

            List<Attachment> attachments = new ArrayList<>();
            String userId = jwt.getClaim("sub");

            for (int i = 0; i < numAttachments; i++) {
                InputPart part = attachmentParts.get(i);
                String fileName = safeFileNames.get(i);
                String contentType = safeContentTypes.get(i);
                long size;
                try {
                    size = Long.parseLong(safeSizesStr.get(i));
                } catch (NumberFormatException e) {
                    LOGGER.error("Ungültige Größe für Anhang: " + safeSizesStr.get(i), e);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Ungültige Größe für Anhang: " + safeSizesStr.get(i))
                            .build();
                }

                try {
                    InputStream fileStream = part.getBody(InputStream.class, null);
                    LOGGER.infof("Uploading file: %s, Content-Type: %s, Size: %d bytes", fileName, contentType, size);
                    String objectName = storageService.uploadFile(fileStream, fileName, contentType, size);

                    Attachment attachment = new Attachment(null, fileName, objectName, contentType, size, userId);
                    attachments.add(attachment);
                } catch (Exception e) {
                    LOGGER.error("Fehler beim Hochladen der Anhänge: ", e);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Fehler beim Hochladen der Anhänge: " + e.getMessage())
                            .build();
                }
            }

            try {
                repository.sendMailsByTemplate(projectId, userId, smtpInformation, attachments);
                return Response.ok("Emails mit Anhängen erfolgreich gesendet").build();
            } catch (Exception e) {
                LOGGER.error("Fehler beim Senden der E-Mails: ", e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Fehler beim Senden der E-Mails: " + e.getMessage())
                        .build();
            }

        } catch (Exception e) {
            LOGGER.error("Allgemeiner Fehler beim Verarbeiten der Anfrage: ", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Allgemeiner Fehler beim Verarbeiten der Anfrage: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Hilfsmethode zum Extrahieren des Dateinamens aus dem Content-Disposition Header.
     */
    private String extractFileName(InputPart part) {
        try {
            String header = part.getHeaders().getFirst("Content-Disposition");
            if (header != null) {
                for (String content : header.split(";")) {
                    if (content.trim().startsWith("filename")) {
                        String[] nameParts = content.split("=");
                        if (nameParts.length > 1) {
                            return nameParts[1].trim().replaceAll("\"", "");
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Konnte Dateinamen nicht extrahieren.", e);
        }
        return "unknown";
    }

    /**
     * Hilfsmethode zum Extrahieren eines einzelnen Wertes aus den Formular-Daten.
     */
    private String getValue(Map<String, List<InputPart>> uploadForm, String key) throws IOException {
        List<InputPart> parts = uploadForm.get(key);
        if (parts != null && !parts.isEmpty()) {
            return parts.get(0).getBodyAsString();
        }
        return null;
    }

    /**
     * Hilfsmethode zum Extrahieren mehrerer Werte aus den Formular-Daten.
     */
    private List<String> getValues(Map<String, List<InputPart>> uploadForm, String key) throws IOException {
        List<InputPart> parts = uploadForm.get(key);
        if (parts != null && !parts.isEmpty()) {
            List<String> values = new ArrayList<>();
            for (InputPart part : parts) {
                values.add(part.getBodyAsString());
            }
            return values;
        }
        return null;
    }

    /**
     * Methode zum Abrufen eines Anhangs. Nur berechtigte Nutzer dürfen Zugriff haben.
     */
    @GET
    @Path("attachments/{id}")
    @Authenticated
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getAttachment(@PathParam("id") Long id) {
        Attachment attachment = Attachment.findById(id);
        if (attachment == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Anhang nicht gefunden").build();
        }

        String userId = jwt.getClaim("sub");
        if (!attachment.ownerId.equals(userId) && !permissionService.hasPermissionForAttachment(userId, attachment)) {
            return Response.status(Response.Status.FORBIDDEN).entity("Zugriff verweigert").build();
        }

        try {
            InputStream fileStream = storageService.downloadFile(attachment.filePath);
            LOGGER.warn(fileStream.available());

            byte[] buffer = fileStream.readNBytes(10);
            if (buffer.length == 0) {
                LOGGER.warn("Download-Anhang '%s' ist leer.".formatted(attachment.fileName));
            } else {
                LOGGER.infof("Download-Anhang '%s' enthält mindestens %d Bytes.", attachment.fileName, buffer.length);
            }

            InputStream downloadStream = new SequenceInputStream(new ByteArrayInputStream(buffer), fileStream);

            return Response.ok(downloadStream)
                    .header("Content-Disposition", "attachment; filename=\"" + attachment.fileName + "\"")
                    .header("Content-Length", attachment.size)
                    .build();
        } catch (Exception e) {
            LOGGER.error("Fehler beim Abrufen der Datei: ", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Fehler beim Abrufen der Datei: " + e.getMessage())
                    .build();
        }
    }
}
