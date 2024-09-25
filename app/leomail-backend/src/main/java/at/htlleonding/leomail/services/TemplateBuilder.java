package at.htlleonding.leomail.services;

import at.htlleonding.leomail.entities.Contact;
import at.htlleonding.leomail.entities.Template;
import io.quarkus.qute.Engine;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class TemplateBuilder {

    private static final Logger LOGGER = Logger.getLogger(TemplateBuilder.class);

    @Inject
    Engine engine;

    /**
     * Rendert Templates basierend auf der Vorlage, den Kontakten und dem Personalisierungsflag.
     *
     * @param templateId    ID der Vorlage
     * @param contacts      Liste der Kontakte
     * @param personalized  Wenn true, werden die Templates personalisiert
     * @return Liste der gerenderten Templates als Strings
     */
    public List<String> renderTemplates(Long templateId, List<Contact> contacts, boolean personalized) {
        List<TemplateInstance> instances = buildTemplateInstances(templateId, contacts, personalized);
        List<String> renderedTemplates = new ArrayList<>(instances.size());

        for (TemplateInstance instance : instances) {
            try {
                String rendered = instance.render();
                renderedTemplates.add(rendered);
                LOGGER.debugf("Gerendertes Template: %s", rendered);
            } catch (Exception e) {
                LOGGER.error("Fehler beim Rendern des Templates", e);
            }
        }

        return renderedTemplates;
    }

    /**
     * Baut eine Liste von TemplateInstance basierend auf der Vorlage und den Kontakten.
     *
     * @param templateId    ID der Vorlage
     * @param contacts      Liste der Kontakte
     * @param personalized  Wenn true, werden die Templates personalisiert
     * @return Liste von TemplateInstance
     */
    private List<TemplateInstance> buildTemplateInstances(Long templateId, List<Contact> contacts, boolean personalized) {
        Template template = Template.findById(templateId);
        if (template == null) {
            LOGGER.errorf("Vorlage mit ID %d nicht gefunden.", templateId);
            throw new IllegalArgumentException("Vorlage nicht gefunden.");
        }

        String combinedTemplate = template.greeting.templateString + "<br>" + template.content;
        io.quarkus.qute.Template quteTemplate = engine.parse(combinedTemplate);
        List<String> templateVariables = extractTemplateVariables(combinedTemplate);

        List<TemplateInstance> instances = new ArrayList<>(contacts.size());

        for (Contact contact : contacts) {
            TemplateInstance instance = quteTemplate.instance();
            for (String variable : templateVariables) {
                String value = getContactValue(contact, variable);
                if (value != null) {
                    instance.data(variable, value);
                }
            }
            instance.data("personalized", personalized);
            if (personalized) {
                instance.data("sex", contact.attributes.get("sex"));
            }
            instances.add(instance);
        }

        return instances;
    }

    /**
     * Extrahiert die Variablen aus dem Template-Inhalt.
     *
     * @param templateContent Inhalt des Templates
     * @return Liste der Variablen
     */
    private List<String> extractTemplateVariables(String templateContent) {
        List<String> variables = new ArrayList<>();
        int startIndex = 0;
        while ((startIndex = templateContent.indexOf("{", startIndex)) != -1) {
            int endIndex = templateContent.indexOf("}", startIndex);
            if (endIndex != -1) {
                String variable = templateContent.substring(startIndex + 1, endIndex).trim();
                if (!isControlStatement(variable)) {
                    variables.add(variable);
                }
                startIndex = endIndex + 1;
            } else {
                break;
            }
        }
        return variables;
    }

    /**
     * Prüft, ob eine Variable ein Kontrollstatement ist.
     *
     * @param variable Name der Variable
     * @return true, wenn es ein Kontrollstatement ist, sonst false
     */
    private boolean isControlStatement(String variable) {
        List<String> controlStatements = List.of("if", "else", "for", "end", "set", "define", "include", "extends");
        return controlStatements.stream().anyMatch(variable::startsWith);
    }

    /**
     * Holt den Wert eines Kontaktes basierend auf der Variable.
     *
     * @param contact  Kontakt
     * @param variable Variable im Template
     * @return Wert des Kontaktes für die Variable
     */
    private String getContactValue(Contact contact, String variable) {
        return switch (variable.toLowerCase()) {
            case "firstname" -> contact.firstName;
            case "lastname" -> contact.lastName;
            case "mailaddress" -> contact.mailAddress;
            default -> contact.attributes.get(variable);
        };
    }
}