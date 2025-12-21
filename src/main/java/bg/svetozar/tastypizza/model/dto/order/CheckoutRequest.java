package bg.svetozar.tastypizza.model.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CheckoutRequest(
        @NotBlank(message = "phone is required")
        @Size(max = 30, message = "phone must be <= 30 characters")
        String phone,

        @NotBlank(message = "address is required")
        @Size(max = 300, message = "address must be <= 300 characters")
        String address
) {}
