package bg.svetozar.tastypizza.model.dto.admin;

import bg.svetozar.tastypizza.model.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_ROLE;

public record UpdateUserRoleRequest(
        @NotNull(message = REQUIRED_ROLE)
        UserRole role
) {
    @JsonCreator
    public UpdateUserRoleRequest(@JsonProperty("role") UserRole role) {
        this.role = role;
    }
}
