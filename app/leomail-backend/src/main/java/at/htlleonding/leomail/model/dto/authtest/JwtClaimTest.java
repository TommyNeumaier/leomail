package at.htlleonding.leomail.model.dto.authtest;

import java.io.Serializable;

public record JwtClaimTest(String key, String val) implements Serializable {
}
