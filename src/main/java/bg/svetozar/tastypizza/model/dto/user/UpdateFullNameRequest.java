package bg.svetozar.tastypizza.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateFullNameRequest(
        @NotBlank
        @Size(max = 100)
        String fullname
) {}
