package bg.svetozar.tastypizza.model.dto.pizza;

import bg.svetozar.tastypizza.model.dto.pizzaAllowedIngredient.PizzaAllowedIngredientRequest;
import bg.svetozar.tastypizza.model.dto.pizzaIngredient.PizzaIngredientRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_NAME;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_PIZZA_NAME_BETWEEN_5_100_CHARS;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_PIZZA_DESCRIPTION_MAX_1000_CHARS;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_PIZZA_BASE_PRICE;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_PIZZA_BASE_PRICE_2_DECIMALS;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_SPICY_LEVEL;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_SPICY_LEVEL_MAX_20;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_VARIANTS;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_PIZZA_INGREDIENTS;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_ALLOWED_INGREDIENTS;


import java.math.BigDecimal;
import java.util.List;

public record PizzaRequest(
        @NotBlank(message = REQUIRED_NAME)
        @Size(min = 5, max = 100, message = INVALID_PIZZA_NAME_BETWEEN_5_100_CHARS)
        String name,

        @Size(max = 1000, message = INVALID_PIZZA_DESCRIPTION_MAX_1000_CHARS)
        String description,

        @NotBlank(message = REQUIRED_PIZZA_BASE_PRICE)
        @Pattern(
                regexp = "^(?:0|[1-9]\\d*)(?:\\.\\d{1,2})?$",
                message = INVALID_PIZZA_BASE_PRICE_2_DECIMALS
        )
        String basePrice,

        String imageBase64,

        @NotBlank(message = REQUIRED_SPICY_LEVEL)
        @Size(max = 20, message = INVALID_SPICY_LEVEL_MAX_20)
        String spicyLevel,

        @Valid
        @NotNull(message = REQUIRED_VARIANTS)
        List<@Valid PizzaVariantRequest> variants,

        @Valid
        @NotNull(message = REQUIRED_PIZZA_INGREDIENTS)
        List<@Valid PizzaIngredientRequest> ingredients,

        @Valid
        @NotNull(message = REQUIRED_ALLOWED_INGREDIENTS)
        List<@Valid PizzaAllowedIngredientRequest> allowedIngredients
) {
}