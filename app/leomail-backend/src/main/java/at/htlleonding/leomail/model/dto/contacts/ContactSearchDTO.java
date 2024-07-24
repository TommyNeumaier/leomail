package at.htlleonding.leomail.model.dto.contacts;

import java.io.Serializable;

public record ContactSearchDTO(
        String id,
        String firstName,
        String lastName,
        String mailAddress) implements Serializable {

}
