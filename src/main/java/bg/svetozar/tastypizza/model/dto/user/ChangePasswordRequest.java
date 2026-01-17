package bg.svetozar.tastypizza.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_CURRENT_PASSWORD;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_NEW_PASSWORD;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_NEW_PASSWORD_BETWEEN_6_255_CHARS;

public record ChangePasswordRequest(
        @NotBlank(message = REQUIRED_CURRENT_PASSWORD)
        String currentPassword,

        @NotBlank(message = REQUIRED_NEW_PASSWORD)
        @Size(min = 6, max = 255, message = INVALID_NEW_PASSWORD_BETWEEN_6_255_CHARS)
        String newPassword,

        boolean logoutAll
) {}
