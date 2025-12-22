package bg.svetozar.tastypizza.model.dto.pizza;

import bg.svetozar.tastypizza.model.dto.pizzaAllowedIngredient.PizzaAllowedIngredientRequest;
import bg.svetozar.tastypizza.model.dto.pizzaIngredient.PizzaIngredientRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record PizzaRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        @Size(max = 1000, message = "Description must be at most 1000 characters")
        String description,

        @NotBlank(message = "Base price is required")
        @Pattern(
                regexp = "^(?:0|[1-9]\\d*)(?:\\.\\d{1,2})?$",
                message = "Base price must be a valid number with up to 2 decimals"
        )
        String basePrice,

        String imageBase64,

        @Size(max = 20, message = "Spicy level must be at most 20 characters")
        String spicyLevel,

        @Valid
        @NotNull(message = "Variants are required")
        List<@Valid PizzaVariantRequest> variants,

        @Valid
        @NotNull(message = "Ingredients are required")
        List<@Valid PizzaIngredientRequest> ingredients,

        @Valid
        @NotNull(message = "Allowed ingredients are required")
        List<@Valid PizzaAllowedIngredientRequest> allowedIngredients
) {
}