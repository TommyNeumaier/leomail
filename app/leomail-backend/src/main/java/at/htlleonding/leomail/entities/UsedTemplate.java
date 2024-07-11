package at.htlleonding.leomail.entities;

import com.arjuna.ats.jta.exceptions.NotImplementedException;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

@Entity
public class UsedTemplate extends Template {
    public LocalDateTime sentOn;

    @ManyToOne
    public Account sentBy;

    public UsedTemplate()  {
    }

    public UsedTemplate(String name, String headline, String content, String createdBy, LocalDateTime sentOn, Account sentBy) {
        super(name, headline, content, createdBy);
        this.sentOn = sentOn;
        this.sentBy = sentBy;
    }
}
