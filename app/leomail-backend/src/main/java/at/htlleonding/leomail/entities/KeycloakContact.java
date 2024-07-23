package at.htlleonding.leomail.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class KeycloakContact extends Contact {

    @Id
    public String id;

    public KeycloakContact() {
    }

    public KeycloakContact(String firstName, String lastName, String mailAddress, String id) {
        super(firstName, lastName, mailAddress);
        this.id = id;
    }
}
