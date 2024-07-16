package at.htlleonding.leomail.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class SentMail extends PanacheEntity {

    @ManyToOne
    public Contact contact;

    @ManyToOne
    @JsonIgnore
    public UsedTemplate template;

    @Column(nullable = false, length = 8192)
    public String actualContent;

    public SentMail() {}

    public SentMail(Contact contact, UsedTemplate template, String actualContent) {
        this.contact = contact;
        this.template = template;
        this.actualContent = actualContent;
    }
}
