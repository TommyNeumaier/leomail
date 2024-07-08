package at.htlleonding.leomail.entities;

import com.arjuna.ats.jta.exceptions.NotImplementedException;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;

@Entity
public class UsedTemplate extends Template {
    public LocalDateTime sentOn;
    public Account sentBy;

    public UsedTemplate() throws NotImplementedException {
        throw new NotImplementedException();
    }

    public UsedTemplate(String name, String headline, String content, Account createdBy, LocalDateTime sentOn, Account sentBy) {
        super(name, headline, content, createdBy);
        this.sentOn = sentOn;
        this.sentBy = sentBy;
    }
}
