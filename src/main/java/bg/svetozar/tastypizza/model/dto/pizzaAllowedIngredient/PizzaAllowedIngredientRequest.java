package bg.svetozar.tastypizza.model.dto.pizzaAllowedIngredient;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PizzaAllowedIngredientRequest(
        @NotNull(message = "Ingredient id is required")
        @Positive(message = "Ingredient id must be positive")
        Long ingredientId,

        @NotNull(message = "Extra price is required")
        @DecimalMin(value = "0.00", inclusive = true, message = "Extra price must not be negative")
        @Digits(integer = 6, fraction = 2, message = "Extra price must have up to 6 digits and 2 decimals")
        String extraPrice
) {}
