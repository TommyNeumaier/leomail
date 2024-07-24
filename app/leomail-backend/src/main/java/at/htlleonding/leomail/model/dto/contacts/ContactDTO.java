package at.htlleonding.leomail.model.dto.contacts;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ContactDTO(
        String id,
        String firstName,
        String lastName,
        String mailAddress) implements Serializable {

}
