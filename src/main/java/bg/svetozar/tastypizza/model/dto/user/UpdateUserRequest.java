package bg.svetozar.tastypizza.model.dto.user;

import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(max = 100)
        String fullname,

        @Size(min = 3, max = 50)
        String username
) {
}
