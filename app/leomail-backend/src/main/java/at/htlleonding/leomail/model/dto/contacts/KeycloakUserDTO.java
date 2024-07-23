package at.htlleonding.leomail.model.dto.contacts;

import java.util.List;
import java.util.Map;

public record KeycloakUserDTO(
        String id,
        String firstName,
        String lastName,
        String mailAddress) {


}
