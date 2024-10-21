package at.htlleonding.leomail.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public record SMTPInformation(Receiver receiver, Long templateId, boolean personalized, LocalDateTime scheduledAt, String fromMail) implements Serializable {
}
