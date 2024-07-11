package at.htlleonding.leomail.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Template extends PanacheEntity {

    public String name;
    public LocalDateTime created;
    public String headline;
    public String content;

    @ManyToOne
    public Account createdBy;

    @ManyToOne
    public TemplateGreeting greeting;

    public Template() {
        this.created = LocalDateTime.now();
    }

    public Template(String name, String headline, String content, Account createdBy, TemplateGreeting greeting) {
        this.name = name;
        this.headline = headline;
        this.content = content;
        this.createdBy = createdBy;
        this.created = LocalDateTime.now();
        this.greeting = greeting;
    }
}