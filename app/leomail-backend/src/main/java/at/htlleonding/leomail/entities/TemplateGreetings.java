package at.htlleonding.leomail.entities;

import at.htlleonding.leomail.entities.pk.TemplateGreetingsPK;
import at.htlleonding.leomail.model.enums.Gender;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;

@Entity
@IdClass(TemplateGreetingsPK.class)
public class TemplateGreetings extends PanacheEntityBase {

    @Id
    public Long id;

    @Id
    public Gender gender;

    public String content;

    public TemplateGreetings() {
    }

    public TemplateGreetings(Long id, Gender gender, String content) {
        this.id = id;
        this.content = content;
        this.gender = gender;
    }
}
