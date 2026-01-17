package bg.svetozar.tastypizza.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_FULL_NAME;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_FULL_NAME_BETWEEN_2_100_CHARS;

public record UpdateFullNameRequest(
        @NotBlank(message = REQUIRED_FULL_NAME)
        @Size(min = 2, max = 100,  message = INVALID_FULL_NAME_BETWEEN_2_100_CHARS)
        String fullname
) {}
