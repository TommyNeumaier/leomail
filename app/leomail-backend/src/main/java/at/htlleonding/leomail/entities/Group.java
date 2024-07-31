package at.htlleonding.leomail.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.util.List;
import java.util.Set;

@Entity
@Table(name = "Groups")
public class Group extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public String id;

    public String name;

    @ManyToOne
    public Contact createdBy;

    @ManyToOne
    public Project project;

    @ManyToMany
    public List<Contact> members;

    public Group() {
    }

    public Group(String name, Contact createdBy, Project project) {
        this.name = name;
        this.createdBy = createdBy;
        this.project = project;
    }

    public Group(String name, Contact creator, Project project, List<Object> list) {
        this(name, creator, project);

    }
}
