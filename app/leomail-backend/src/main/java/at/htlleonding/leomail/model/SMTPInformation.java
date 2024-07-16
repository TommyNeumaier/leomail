package at.htlleonding.leomail.model;

import java.io.Serializable;

public record SMTPInformation(Receiver receiver, Long templateId, boolean personalized) implements Serializable {
}
