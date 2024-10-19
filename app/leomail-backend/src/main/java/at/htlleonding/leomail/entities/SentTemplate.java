package at.htlleonding.leomail.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@SequenceGenerator(name = "seq_sent_template", sequenceName = "seq_sent_template", allocationSize = 1, initialValue = 3)
public class SentTemplate extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_sent_template")
    public Long id;

    @Column(length = 128, nullable = false)
    public String name;

    @CreationTimestamp
    public LocalDateTime created;

    @Column(length = 256, nullable = false)
    public String headline;

    @Column(length = 8192, nullable = false)
    public String content;

    @ManyToOne(fetch = FetchType.LAZY)
    public Contact createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    public Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    public TemplateGreeting greeting;

    public LocalDateTime sentOn;

    public LocalDateTime scheduledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    public Contact sentBy;

    @OneToMany(mappedBy = "usedTemplate", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    public List<SentMail> mails = new ArrayList<>();

    public SentTemplate() {
        this.created = LocalDateTime.now();
    }

    public SentTemplate(String name, String headline, String content, Contact createdBy, TemplateGreeting greeting) {
        this();
        this.name = name;
        this.headline = headline;
        this.content = content;
        this.createdBy = createdBy;
        this.greeting = greeting;
    }

    public SentTemplate(Template template, LocalDateTime scheduledAt, Project project, Contact sentBy) {
        this(template.name, template.headline, template.content, template.createdBy, template.greeting);
        this.scheduledAt = scheduledAt;
        this.project = project;
        this.sentBy = sentBy;
    }
}