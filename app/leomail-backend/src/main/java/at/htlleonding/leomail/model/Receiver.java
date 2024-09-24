package at.htlleonding.leomail.model;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public record Receiver(Optional<List<String>> contacts, Optional<List<String>> groups) implements Serializable {
}
