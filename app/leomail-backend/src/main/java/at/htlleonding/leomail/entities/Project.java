package at.htlleonding.leomail.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
public class Project extends PanacheEntity {
    public String name;
    public String description;
    public LocalDateTime createdOn;

    @ManyToOne
    public Contact createdBy;

    @ManyToMany
    public Set<Contact> members;

    @OneToMany(mappedBy = "project")
    public Set<Group> groups;

    public Project() {
        this.createdOn = LocalDateTime.now();
    }

    public Project(String name, String description, Contact createdBy) {
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.createdOn = LocalDateTime.now();
    }
}
