package at.htlleonding.leomail.services;

import at.htlleonding.leomail.entities.Contact;
import at.htlleonding.leomail.entities.Template;
import io.quarkus.qute.Engine;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@ApplicationScoped
public class TemplateBuilder {

    @Inject
    Engine engine;

    public List<String> renderTemplates(Long templateId, List<Contact> contacts, boolean personalized) {
        List<TemplateInstance> instances = buildTemplateInstances(templateId, contacts, personalized);
        List<String> renderedTemplates = new LinkedList<>();
        for (TemplateInstance instance : instances) {
            renderedTemplates.add(instance.render());
        }
        return renderedTemplates;
    }

    private List<TemplateInstance> buildTemplateInstances(Long templateId, List<Contact> contacts, boolean personalized) {
        Template template = Template.findById(templateId);
        List<String> templateVariables = getTemplateVariables(template.greeting.templateString + "<br>" + template.content);
        io.quarkus.qute.Template quteTemplate = engine.parse(template.greeting.templateString + "<br>" + template.content);
        List<TemplateInstance> instances = new LinkedList<>();

        for (Contact contact : contacts) {
            TemplateInstance instance = quteTemplate.instance();
            for (String variable : templateVariables) {
                String value = getContactValue(contact, variable);
                if (value != null) {
                    instance.data(variable, value);
                }
            }
            instance.data("personalized", personalized);
            if (personalized) instance.data("sex", contact.attributes.get("sex"));
            instances.add(instance);
        }

        return instances;
    }

    private List<String> getTemplateVariables(String templateContent) {
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

    private boolean isControlStatement(String variable) {
        List<String> controlStatements = List.of("if", "else", "for", "end", "set", "define", "include", "extends");
        for (String statement : controlStatements) {
            if (variable.startsWith(statement)) {
                return true;
            }
        }
        return false;
    }

    private String getContactValue(Contact contact, String variable) {
        return switch (variable.toLowerCase()) {
            case "firstname" -> contact.firstName;
            case "lastname" -> contact.lastName;
            case "mailaddress" -> contact.mailAddress;
            default -> contact.attributes.get(variable);
        };
    }
}