package at.htlleonding.leomail.model.dto.contacts;

import at.htlleonding.leomail.model.enums.Gender;

import java.io.Serializable;

public record ContactDetailDTO(String id, String firstName, String lastName, String mailAddress, Gender gender, String suffixTitle, String prefixTitle, String company, String positionAtCompany) implements Serializable {
}
