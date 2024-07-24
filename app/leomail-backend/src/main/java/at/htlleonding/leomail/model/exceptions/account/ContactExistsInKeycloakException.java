package at.htlleonding.leomail.model.exceptions.account;

public class ContactExistsInKeycloakException extends RuntimeException {
    public ContactExistsInKeycloakException(String message) {
        super(message);
    }
}
