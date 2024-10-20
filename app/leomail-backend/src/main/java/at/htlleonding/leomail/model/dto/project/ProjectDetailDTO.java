package at.htlleonding.leomail.model.dto.project;

import at.htlleonding.leomail.model.dto.contacts.NaturalContactSearchDTO;

import java.io.Serializable;
import java.util.List;

public record ProjectDetailDTO(
        String id,
        String name,
        String description,
        MailAddressDTO mailInformation,
        List<NaturalContactSearchDTO> members
) implements Serializable {
}