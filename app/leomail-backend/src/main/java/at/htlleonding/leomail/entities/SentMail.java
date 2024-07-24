package at.htlleonding.leomail.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
public class SentMail extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne
    public Contact contact;

    @ManyToOne
    @JsonIgnore
    public SentTemplate usedTemplate;

    @Column(length = 8192)
    public String actualContent;

    public boolean sent = false;

    public SentMail() {
    }

    public SentMail(Contact contact, SentTemplate usedTemplate, String content) {
        this.contact = contact;
        this.usedTemplate = usedTemplate;
        this.actualContent = content;
    }
}