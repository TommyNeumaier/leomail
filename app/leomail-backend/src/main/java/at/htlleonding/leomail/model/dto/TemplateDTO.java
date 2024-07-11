package at.htlleonding.leomail.model.dto;

import java.io.Serializable;

public record TemplateDTO(String name, String headline, String content, Long greeting, String accountName) implements Serializable {

}
