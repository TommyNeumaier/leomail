package at.htlleonding.leomail.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Entity
public class Contact extends PanacheEntity {

    public String firstName;
    public String lastName;
    public String mailAddress;

    @ElementCollection
    @CollectionTable(name = "contact_attributes", joinColumns = @JoinColumn(name = "id"))
    @MapKeyColumn(name = "key")
    @Column(name = "val")
    public Map<String, String> attributes = new HashMap<>();

    @ManyToOne
    @JsonIgnore
    public Group group;

    public Contact() {
    }

    public Contact(String firstName, String lastName, String mailAddress) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.mailAddress = mailAddress;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact) o;
        return Objects.equals(id, contact.id) && Objects.equals(firstName, contact.firstName) && Objects.equals(lastName, contact.lastName) && Objects.equals(mailAddress, contact.mailAddress) && Objects.equals(attributes, contact.attributes) && Objects.equals(group, contact.group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, mailAddress, attributes, group);
    }
}
