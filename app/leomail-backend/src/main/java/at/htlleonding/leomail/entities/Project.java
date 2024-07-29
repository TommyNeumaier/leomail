package at.htlleonding.leomail.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
public class Project extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public String id;

    public String name;
    public String description;
    public LocalDateTime createdOn;

    public String mailAddress;
    public String password;

    @ManyToOne
    public Contact createdBy;

    @OneToMany(mappedBy = "project")
    public List<Group> groups;

    @OneToMany(mappedBy = "project")
    public List<Template> templates;

    @OneToMany(mappedBy = "project")
    public List<SentTemplate> sentTemplates;

    @ManyToMany
    @JoinTable(
            name = "project_contact",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "contact_id")
    )
    public List<Contact> members;

    public Project() {
        this.createdOn = LocalDateTime.now();
    }

    public Project(String name, String description, Contact createdBy) {
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.createdOn = LocalDateTime.now();
    }

    public Project(String name, String description, Contact createdBy, String mailAddress, String password, List<Contact> members) {
        this(name, description, createdBy);
        this.mailAddress = mailAddress;
        this.password = password;
        this.members = members;
    }
}
