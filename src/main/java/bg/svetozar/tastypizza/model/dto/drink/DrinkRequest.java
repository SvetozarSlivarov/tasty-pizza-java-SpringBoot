package bg.svetozar.tastypizza.model.dto.drink;

import jakarta.validation.constraints.*;

public record DrinkRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        @Size(max = 1000, message = "Description must be at most 1000 characters")
        String description,

        @NotNull(message = "Base price is required")
        @DecimalMin(value = "0.01", inclusive = true, message = "Base price must be greater than 0")
        @Digits(integer = 6, fraction = 2, message = "Base price must have up to 6 digits and 2 decimals")
        String basePrice,

        String imageBase64
) {}
