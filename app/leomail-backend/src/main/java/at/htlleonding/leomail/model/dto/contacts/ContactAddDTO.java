package at.htlleonding.leomail.model.dto.contacts;

import at.htlleonding.leomail.model.enums.Gender;

import java.io.Serializable;

public record ContactAddDTO(String firstName,
                            String lastName,
                            String mailAddress,
                            String company, String positionAtCompany, String prefixTitle, String suffixTitle, Gender gender)
implements Serializable {
}
