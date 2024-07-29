package at.htlleonding.leomail.model.dto.project;

import at.htlleonding.leomail.model.dto.contacts.ContactSearchDTO;

import java.io.Serializable;
import java.util.List;

public record ProjectAddDTO(String name, String description, MailAddressDTO mailInformation, List<ContactSearchDTO> members) implements Serializable {
}
