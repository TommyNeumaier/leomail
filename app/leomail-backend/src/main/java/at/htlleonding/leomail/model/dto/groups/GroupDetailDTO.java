package at.htlleonding.leomail.model.dto.groups;

import at.htlleonding.leomail.model.dto.contacts.ContactSearchDTO;
import at.htlleonding.leomail.model.dto.contacts.CreatorDTO;

import java.io.Serializable;
import java.util.List;

public record GroupDetailDTO(String id, String name, String description, CreatorDTO createdBy,
                             List<ContactSearchDTO> members)
implements Serializable {

    public GroupDetailDTO(String id, String name, String description) {
        this(id, name, description, null, null);
    }

    public GroupDetailDTO(String id, String name, String description, CreatorDTO createdBy) {
        this(id, name, description, createdBy, null);
    }
}
