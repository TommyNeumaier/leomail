package at.htlleonding.leomail.entities;

import at.htlleonding.leomail.entities.pk.TemplateGreetingsPK;
import at.htlleonding.leomail.model.enums.Gender;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
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
