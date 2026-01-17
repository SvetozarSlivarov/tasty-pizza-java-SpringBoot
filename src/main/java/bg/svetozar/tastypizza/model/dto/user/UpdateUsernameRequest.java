package bg.svetozar.tastypizza.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_USERNAME;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_USERNAME_BETWEEN_3_50_CHARS;



public record UpdateUsernameRequest(
        @NotBlank(message =  REQUIRED_USERNAME)
        @Size(min = 3, max = 50, message = INVALID_USERNAME_BETWEEN_3_50_CHARS)
        String username
) {}
