package at.htlleonding.leomail.model;

import at.htlleonding.leomail.entities.Group;

import java.util.List;
import java.util.Optional;

public record Receiver(Optional<List<String>> contacts, Optional<List<Group>> groups) {
}
