package at.htlleonding.leomail.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@SequenceGenerator(name = "seq_template", sequenceName = "seq_template", allocationSize = 1, initialValue = 5)
public class Template extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_template")
    public Long id;

    @Column(length = 128, nullable = false, unique = true)
    public String name;

    @ManyToOne
    public Project project;

    @CreationTimestamp
    public LocalDateTime created;

    @Column(length = 256, nullable = false)
    public String headline;

    @Column(length = 8192, nullable = false)
    public String content;

    @ManyToOne
    public Contact createdBy;

    @ManyToOne
    public TemplateGreeting greeting;

    public Template() {
        this.created = LocalDateTime.now();
    }

    public Template(String name, String headline, String content, Contact createdBy, TemplateGreeting greeting) {
        this();
        this.name = name;
        this.headline = headline;
        this.content = content;
        this.createdBy = createdBy;
        this.greeting = greeting;
    }

    public Template(String name, String headline, String content, Contact createdBy, TemplateGreeting greeting, String projectId) {
        this(name, headline, content, createdBy, greeting);
        this.project = Project.findById(projectId);
    }
}