package bg.svetozar.tastypizza.model.dto.user;

import java.time.LocalDateTime;

public record UserDto(
        Long id,
        String fullname,
        String username,
        String role,
        LocalDateTime createdAt
) {
}
