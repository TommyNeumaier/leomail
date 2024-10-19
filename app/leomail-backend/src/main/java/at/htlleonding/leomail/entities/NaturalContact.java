package at.htlleonding.leomail.entities;

import jakarta.persistence.*;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import at.htlleonding.leomail.model.enums.Gender;

@Entity
public class NaturalContact extends Contact {

    @Column(nullable = false)
    public String firstName;

    @Column(nullable = false)
    public String lastName;

    @Column(nullable = false, unique = true)
    public String mailAddress;

    public String prefixTitle;

    public String suffixTitle;

    public String company; // Optional

    public String positionAtCompany; // Optional

    @Enumerated(EnumType.STRING)
    public Gender gender;

    public NaturalContact() {
        super();
    }
}