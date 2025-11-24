package bg.svetozar.tastypizza.model.dto.pizzaIngredient;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PizzaIngredientRequest(
        @NotNull(message = "Ingredient id is required")
        @Positive(message = "Ingredient id must be positive")
        Long ingredientId,

        boolean removable
) {}