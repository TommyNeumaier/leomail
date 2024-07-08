package at.htlleonding.leomail.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.io.Serializable;

@Entity
public class Account extends PanacheEntityBase implements Serializable {

    @Id
    @Column(length = 50)
    public String userName;

    public String position;
    public String departement;

    @ManyToOne
    public Project project;

    public Account() {
    }

    public Account(String userName, String position, String departement) {
        this.userName = userName;
        this.position = position;
        this.departement = departement;
    }
}
