package at.htlleonding.leomail.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
@Table(name = "Groups", uniqueConstraints = @UniqueConstraint(columnNames = {"name", "project_id"}))
public class Group extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public String id;

    @Column(nullable = false)
    public String name;

    @Column(nullable = true)
    public String description;

    @ManyToOne(fetch = FetchType.LAZY)
    public NaturalContact createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    public Project project;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    public List<Contact> members = new ArrayList<>();

    public Group() {
    }

    public Group(String name, String description, NaturalContact createdBy, Project project) {
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.project = project;
    }

    public Group(String name, String description, NaturalContact createdBy, Project project, List<Contact> members) {
        this(name, description, createdBy, project);
        this.members = members;
    }
}