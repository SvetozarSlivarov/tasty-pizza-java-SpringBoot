package bg.svetozar.tastypizza.model.dto.admin;

import bg.svetozar.tastypizza.model.enums.UserRole;
import java.time.LocalDateTime;

public record AdminUserDto(
        Long id,
        String username,
        String fullname,
        UserRole role,
        boolean deleted,
        LocalDateTime deletedAt,
        int tokenVersion,
        LocalDateTime createdAt
) {}
