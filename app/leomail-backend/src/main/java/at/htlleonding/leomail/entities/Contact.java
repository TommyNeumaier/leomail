package at.htlleonding.leomail.entities;

import at.htlleonding.leomail.model.enums.Gender;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import java.time.LocalDate;

@Entity
public class Contact extends PanacheEntity {

    public String firstName;
    public String lastName;
    public String mailAddress;
    public LocalDate birthDate;
    public Gender gender;
    public String phoneNumber;
    public String company;
    public String position;
    public String departement;

    @ManyToOne
    public Group group;

    public Contact() {
    }

    public Contact(String firstName, String lastName, String mailAddress, LocalDate birthDate, String phoneNumber, String company, String position, String departement, Gender gender) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.mailAddress = mailAddress;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
        this.company = company;
        this.position = position;
        this.departement = departement;
        this.gender = gender;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
