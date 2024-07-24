package at.htlleonding.leomail.repositories;

import at.htlleonding.leomail.entities.Contact;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserRepository {

    public boolean checkUser(String sub) {
        Contact contact = Contact.findById(sub);
        if(contact == null) throw new RuntimeException("User not found");
        return contact.kcUser;
    }
}
