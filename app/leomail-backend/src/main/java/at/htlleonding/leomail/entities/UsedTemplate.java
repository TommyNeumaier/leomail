package at.htlleonding.leomail.entities;

import at.htlleonding.leomail.model.enums.Gender;
import com.arjuna.ats.jta.exceptions.NotImplementedException;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

@Entity
public class UsedTemplate extends Template {
    public LocalDateTime sentOn;

    @ManyToOne
    public Account sentBy;

    public UsedTemplate() {
    }

    public UsedTemplate(String name, String headline, String content, String createdBy, Long greeting, LocalDateTime sentOn, Account sentBy) {
        super(name, headline, content, Account.findById(createdBy), TemplateGreeting.find("id", greeting).firstResult());
        this.sentOn = sentOn;
        this.sentBy = sentBy;
    }
}
