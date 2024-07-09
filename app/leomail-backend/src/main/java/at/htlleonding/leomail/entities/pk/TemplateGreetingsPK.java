package at.htlleonding.leomail.entities.pk;

import at.htlleonding.leomail.model.enums.Gender;

import java.io.Serializable;
import java.util.Objects;

public class TemplateGreetingsPK implements Serializable {

    protected Long id;

    protected Gender gender;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TemplateGreetingsPK that = (TemplateGreetingsPK) o;
        return Objects.equals(id, that.id) && gender == that.gender;
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(gender);
        return result;
    }
}
