package bg.svetozar.tastypizza.model.dto.admin;

import bg.svetozar.tastypizza.model.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

public record UpdateUserRoleRequest(@NotNull UserRole role) {}
