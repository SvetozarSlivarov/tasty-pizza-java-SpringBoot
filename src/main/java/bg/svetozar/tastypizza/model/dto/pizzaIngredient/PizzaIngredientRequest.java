package bg.svetozar.tastypizza.model.dto.pizzaIngredient;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_INGREDIENT_ID;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_INGREDIENT_ID_POSITIVE;

public record PizzaIngredientRequest(
        @NotNull(message = REQUIRED_INGREDIENT_ID)
        @Positive(message = INVALID_INGREDIENT_ID_POSITIVE)
        Long ingredientId,

        boolean removable
) {}