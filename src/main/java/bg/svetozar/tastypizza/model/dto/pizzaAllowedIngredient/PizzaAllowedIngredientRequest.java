package bg.svetozar.tastypizza.model.dto.pizzaAllowedIngredient;

import jakarta.validation.constraints.*;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_INGREDIENT_ID;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_INGREDIENT_ID_POSITIVE;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_EXTRA_PRICE;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_EXTRA_PRICE_2_DECIMALS;

public record PizzaAllowedIngredientRequest(
        @NotNull(message = REQUIRED_INGREDIENT_ID)
        @Positive(message = INVALID_INGREDIENT_ID_POSITIVE)
        Long ingredientId,

        @NotBlank(message = REQUIRED_EXTRA_PRICE)
        @Pattern(
                regexp = "^(?:0|[1-9]\\d*)(?:\\.\\d{1,2})?$",
                message = INVALID_EXTRA_PRICE_2_DECIMALS
        )
        String extraPrice
) {}
