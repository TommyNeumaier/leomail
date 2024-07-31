package at.htlleonding.leomail.model.dto.groups;

import at.htlleonding.leomail.model.dto.contacts.ContactSearchDTO;
import at.htlleonding.leomail.model.dto.contacts.CreatorDTO;

import java.io.Serializable;
import java.util.List;

public record GroupDetailDTO(String id, String name, CreatorDTO createdBy, List<ContactSearchDTO> members)
implements Serializable {

    public GroupDetailDTO (String id, String name) {
        this(id, name, null, null);
    }

    public GroupDetailDTO (String id, String name, CreatorDTO createdBy) {
        this(id, name, createdBy, null);
    }
}
