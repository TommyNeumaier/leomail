package at.htlleonding.leomail.services;

import at.htlleonding.leomail.entities.Contact;
import at.htlleonding.leomail.entities.Template;
import io.quarkus.qute.Engine;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@ApplicationScoped
public class TemplateBuilder {

    @Inject
    Engine engine;

    public List<String> renderTemplates(Long templateId, List<Contact> contacts) {
        List<TemplateInstance> instances = buildTemplateInstances(templateId, contacts);
        System.out.println(instances.size());
        List<String> renderedTemplates = new LinkedList<>();
        for (TemplateInstance instance : instances) {
            renderedTemplates.add(instance.render());
        }
        return renderedTemplates;
    }

    private List<TemplateInstance> buildTemplateInstances(Long templateId, List<Contact> contacts) {
        Template template = Template.findById(templateId);
        List<String> templateVariables = getTemplateVariables(template.content);
        io.quarkus.qute.Template quteTemplate = engine.parse(template.content);
        List<TemplateInstance> instances = new LinkedList<>();

        System.out.println("hey");
        for(Contact contact : contacts) {
            System.out.println(contact.firstName);
            TemplateInstance instance = quteTemplate.instance();
            for (String variable : templateVariables) {
                String value = getContactValue(contact, variable);
                instance.data(variable, value);
                System.out.println(value + " into " + variable);
            }
            instances.add(instance);
        }
        System.out.println("wups");
        return instances;
    }

    private List<String> getTemplateVariables(String templateContent) {
        List<String> variables = new ArrayList<>();
        int startIndex = 0;
        while ((startIndex = templateContent.indexOf("{", startIndex)) != -1) {
            int endIndex = templateContent.indexOf("}", startIndex);
            if (endIndex != -1) {
                String variable = templateContent.substring(startIndex + 1, endIndex).trim();
                variables.add(variable);
                startIndex = endIndex + 1;
            } else {
                break;
            }
        }
        return variables;
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