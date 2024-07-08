package at.htlleonding.leomail.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;

@Entity
public class Template extends PanacheEntity {

    public String name;
    public LocalDateTime created;
    public String headline;
    public String content;
    public Account createdBy;

    public Template() {
        this.created = LocalDateTime.now();
    }

    public Template(String name, String headline, String content, Account createdBy) {
        this.name = name;
        this.headline = headline;
        this.content = content;
        this.createdBy = createdBy;
        this.created = LocalDateTime.now();
    }
}
