package bg.svetozar.tastypizza.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_FULL_NAME;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_FULL_NAME_BETWEEN_2_100_CHARS;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_USERNAME;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_USERNAME_BETWEEN_3_50_CHARS;

public record UpdateUserRequest(
        @NotBlank(message = REQUIRED_FULL_NAME)
        @Size(min = 2, max = 100,  message = INVALID_FULL_NAME_BETWEEN_2_100_CHARS)
        String fullname,

        @NotBlank(message = REQUIRED_USERNAME)
        @Size(min = 3, max = 50, message = INVALID_USERNAME_BETWEEN_3_50_CHARS)
        String username
) {
}
