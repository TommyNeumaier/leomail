package at.htlleonding.leomail.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class TemplateGreeting extends PanacheEntity {

    public String content;

    public TemplateGreeting() {
    }

    public TemplateGreeting(Long id, String content) {
        this.id = id;
        this.content = content;
    }
}
