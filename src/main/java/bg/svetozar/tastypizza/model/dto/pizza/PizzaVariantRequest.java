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

        @NotBlank(message = "Extra price is required")
        @Pattern(
                regexp = "^(?:0|[1-9]\\d*)(?:\\.\\d{1,2})?$",
                message = "Extra price must be a valid number with up to 2 decimals"
        )
        String extraPrice
) {
}
