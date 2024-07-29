package at.htlleonding.leomail.entities;

import at.htlleonding.leomail.model.enums.Gender;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Entity
public class Contact extends PanacheEntityBase {

    @Id
    public String id;

    @Column(nullable = false)
    public boolean kcUser = false;

    public String firstName;

    public String lastName;

    public String mailAddress;

    public String prefixTitle;

    public String suffixTitle;

    public String company;

    public String positionAtCompany;

    @Enumerated(EnumType.STRING)
    public Gender gender;

    @CreationTimestamp
    public LocalDateTime created;

    @ElementCollection
    @CollectionTable(name = "contact_attributes", joinColumns = @JoinColumn(name = "id"))
    @MapKeyColumn(name = "key")
    @Column(name = "val")
    public Map<String, String> attributes = new HashMap<>();

    @ManyToMany
    @JsonIgnore
    public List<Group> groups;

    public Contact() {
        this.id = java.util.UUID.randomUUID().toString();
    }

    public Contact(String firstName, String lastName, String mailAddress) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.mailAddress = mailAddress;
    }

    public Contact(String firstName, String lastName, String mailAddress, boolean kcUser) {
        this(firstName, lastName, mailAddress);
        this.kcUser = kcUser;
    }

    public Contact(String id, String firstName, String lastName, String mailAddress, boolean kcUser) {
        this(firstName, lastName, mailAddress, kcUser);
        this.id = id;
    }

    public Contact(String firstName, String lastName, String mailAddress, String prefixTitle, String suffixTitle, String company, String position, Gender gender) {
        this(firstName, lastName, mailAddress);
        this.prefixTitle = prefixTitle;
        this.suffixTitle = suffixTitle;
        this.company = company;
        this.positionAtCompany = position;
        this.gender = gender;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact) o;
        return Objects.equals(id, contact.id) && Objects.equals(firstName, contact.firstName) && Objects.equals(lastName, contact.lastName) && Objects.equals(mailAddress, contact.mailAddress) && Objects.equals(attributes, contact.attributes) && Objects.equals(groups, contact.groups) && Objects.equals(positionAtCompany, contact.positionAtCompany) && Objects.equals(prefixTitle, contact.prefixTitle) && Objects.equals(suffixTitle, contact.suffixTitle) && Objects.equals(company, contact.company);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, mailAddress, attributes, groups, positionAtCompany, prefixTitle, suffixTitle, company);
    }
}
