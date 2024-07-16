package at.htlleonding.leomail.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@SequenceGenerator(name = "seq_used_template", sequenceName = "seq_used_template", allocationSize = 2, initialValue = 2)
public class UsedTemplate extends Template {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_used_template")
    public Long id;

    public LocalDateTime sentOn;

    @ManyToOne
    public Account sentBy;

    @OneToMany(mappedBy = "template", fetch = FetchType.LAZY)
    public List<SentMail> mails;

    public UsedTemplate() {
    }

    public UsedTemplate(String name, String headline, String content, String createdBy, Long greeting, LocalDateTime sentOn, Account sentBy) {
        super(name, headline, content, Account.findById(createdBy), TemplateGreeting.find("id", greeting).firstResult());
        this.sentOn = sentOn;
        this.sentBy = sentBy;
    }

    public UsedTemplate(Template template, LocalDateTime sentOn, Account sentBy) {
        super(template.name, template.headline, template.content, template.createdBy, template.greeting);
        this.sentOn = sentOn;
        this.sentBy = sentBy;
    }
}
