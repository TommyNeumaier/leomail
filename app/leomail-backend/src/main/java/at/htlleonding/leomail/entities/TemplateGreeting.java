package at.htlleonding.leomail.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class TemplateGreeting extends PanacheEntity {

    public String content;

    @Column(nullable = false, length = 1024)
    public String templateString;

    public TemplateGreeting() {
    }

    public TemplateGreeting(Long id, String content) {
        this.id = id;
        this.content = content;
    }
}
