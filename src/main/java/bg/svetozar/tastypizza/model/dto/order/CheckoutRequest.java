package bg.svetozar.tastypizza.model.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CheckoutRequest(
        @NotBlank
        @Size(min = 5, max = 32)
        String phone,

        @NotBlank
        @Size(min = 5, max = 200)
        String address
) {
}
