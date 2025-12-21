package bg.svetozar.tastypizza.model.dto.admin;

import bg.svetozar.tastypizza.model.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(
        @NotNull(message = "Role is required")
        UserRole role
) {
    @JsonCreator
    public UpdateUserRoleRequest(@JsonProperty("role") UserRole role) {
        this.role = role;
    }
}
