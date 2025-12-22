package bg.svetozar.tastypizza.model.dto.pizzaAllowedIngredient;

import jakarta.validation.constraints.*;

public record PizzaAllowedIngredientRequest(
        @NotNull(message = "Ingredient id is required")
        @Positive(message = "Ingredient id must be positive")
        Long ingredientId,

        @NotBlank(message = "Extra price is required")
        @Pattern(
                regexp = "^(?:0|[1-9]\\d*)(?:\\.\\d{1,2})?$",
                message = "Extra price must be a valid number with up to 2 decimals"
        )
        String extraPrice
) {}
