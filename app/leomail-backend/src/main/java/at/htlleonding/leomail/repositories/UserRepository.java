package at.htlleonding.leomail.repositories;

import at.htlleonding.leomail.entities.Contact;
import at.htlleonding.leomail.entities.NaturalContact;
import at.htlleonding.leomail.model.dto.ProfileDTO;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserRepository {

    public boolean checkUser(String sub) {
        Contact contact = Contact.findById(sub);
        if(contact == null) throw new RuntimeException("User not found");
        return contact.kcUser;
    }

    public ProfileDTO getProfile(String id) {
        NaturalContact contact = Contact.findById(id);
        if(contact == null) throw new RuntimeException("User not found");
        return new ProfileDTO(contact.firstName, contact.lastName, contact.mailAddress, "contact.schoolClass", "contact.departement");
    }
}
