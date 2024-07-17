package at.htlleonding.leomail.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class SentMail extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne
    public Contact contact;

    @ManyToOne
    @JsonIgnore
    public UsedTemplate usedTemplate;

    @Column(length = 8192)
    public String actualContent;

    public SentMail() {
    }

    public SentMail(Contact contact, UsedTemplate usedTemplate, String content) {
        this.contact = contact;
        this.usedTemplate = usedTemplate;
        this.actualContent = content;
    }
}