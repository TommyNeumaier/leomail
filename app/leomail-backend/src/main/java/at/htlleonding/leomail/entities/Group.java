package at.htlleonding.leomail.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.util.List;
import java.util.Set;

@Entity
@Table(name = "Groups")
public class Group extends PanacheEntity {

    public String name;

    @ManyToOne
    public Account createdBy;

    @ManyToOne
    public Project project;

    @ManyToMany
    public List<Contact> members;

    public Group() {
    }

    public Group(String name, Account createdBy, Project project) {
        this.name = name;
        this.createdBy = createdBy;
        this.project = project;
    }
}
