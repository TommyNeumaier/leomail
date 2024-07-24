package at.htlleonding.leomail.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class UsedTemplate extends Template {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_template")
    public Long id;

    public LocalDateTime sentOn;

    public LocalDateTime scheduledAt;

    @ManyToOne
    public Account sentBy;

    @OneToMany(mappedBy = "usedTemplate", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    public List<SentMail> mails = new ArrayList<>();

    public UsedTemplate() {
    }

    public UsedTemplate(Template template, LocalDateTime scheduledAt, Account sentBy) {
        super(template.name, template.headline, template.content, template.createdBy, template.greeting);
        this.scheduledAt = scheduledAt;
        this.sentBy = sentBy;
    }
}