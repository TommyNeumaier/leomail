package at.htlleonding.leomail.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "Groups")
public class Group extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public String id;

    public String name;

    public String description;

    @ManyToOne
    public Contact createdBy;

    @ManyToOne
    public Project project;

    @ManyToMany
    public List<Contact> members;

    public Group() {
    }

    public Group(String name, String description, Contact createdBy, Project project) {
        this.name = name;
        this.createdBy = createdBy;
        this.project = project;
        this.description = description;
    }

    public Group(String name, String description, Contact creator, Project project, List<Contact> members) {
        this(name, description, creator, project);
        this.members = members;
    }
}
