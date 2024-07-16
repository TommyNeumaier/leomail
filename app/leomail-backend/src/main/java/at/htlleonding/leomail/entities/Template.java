package at.htlleonding.leomail.entities;

import io.quarkus.Generated;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@SequenceGenerator(name = "seq_template", sequenceName = "seq_template", allocationSize = 2, initialValue = 5)
public class Template extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_template")
    public Long id;

    @Column(length = 128, nullable = false)
    public String name;

    public LocalDateTime created;

    @Column(length = 256, nullable = false)
    public String headline;

    @Column(length = 8192, nullable = false)
    public String content;

    @ManyToOne
    public Account createdBy;

    @ManyToOne
    public TemplateGreeting greeting;

    public Template() {
        this.created = LocalDateTime.now();
    }

    public Template(String name, String headline, String content, Account createdBy, TemplateGreeting greeting) {
        this();
        this.name = name;
        this.headline = headline;
        this.content = content;
        this.createdBy = createdBy;
        this.greeting = greeting;
    }

    public Template(Long id, String name, String headline, String content, Account createdBy, TemplateGreeting greeting) {
        this(name, headline, content, createdBy, greeting);
        this.id = id;
    }
}