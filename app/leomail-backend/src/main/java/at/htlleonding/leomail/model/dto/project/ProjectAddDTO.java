package at.htlleonding.leomail.model.dto.project;

import at.htlleonding.leomail.entities.Contact;

import java.io.Serializable;
import java.util.List;

public record ProjectAddDTO(String name, String description, MailAddressDTO mailInformation, List<Contact> members) implements Serializable {
}
