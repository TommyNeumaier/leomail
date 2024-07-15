package at.htlleonding.leomail.model.dto;

import java.io.Serializable;

public record TemplateDTO(Long id, String name, String headline, String content, Long greeting, String accountName) implements Serializable {

}
