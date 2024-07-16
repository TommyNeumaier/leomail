package at.htlleonding.leomail.model;

import at.htlleonding.leomail.entities.Contact;
import at.htlleonding.leomail.entities.Group;

import java.util.List;
import java.util.Optional;

public record Receiver(Optional<List<Contact>> contacts, Optional<List<Group>> groups) {
}
