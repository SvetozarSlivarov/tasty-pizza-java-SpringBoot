package bg.svetozar.tastypizza.model.dto.pizza;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record PizzaVariantRequest(
        @NotBlank(message = "Size is required")
        @Size(max = 20, message = "Size must be at most 20 characters")
        String size,

        @NotBlank(message = "Dough is required")
        @Size(max = 20, message = "Dough must be at most 20 characters")
        String dough,

        @NotNull(message = "Extra price is required")
        @DecimalMin(value = "0.00", inclusive = true, message = "Extra price must not be negative")
        @Digits(integer = 6, fraction = 2, message = "Extra price must have up to 6 digits and 2 decimals")
        String extraPrice
) {
}
