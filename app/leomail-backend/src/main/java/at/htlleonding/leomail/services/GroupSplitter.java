package at.htlleonding.leomail.services;

import at.htlleonding.leomail.entities.Contact;
import at.htlleonding.leomail.entities.Group;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.LinkedList;
import java.util.List;

public class GroupSplitter {

    public static List<Contact> getAllContacts(List<Group> groups, List<String> contacts) {
        List<Contact> contactList = Contact.find("id in ?1", contacts).list();
        List<Contact> allContacts = new LinkedList<>(contactList);
        for (Group group : groups) {
            for (Contact contact : group.members) {
                if (!allContacts.contains(contact)) {
                    allContacts.add(contact);
                }
            }
        }
        return allContacts;
    }
}
