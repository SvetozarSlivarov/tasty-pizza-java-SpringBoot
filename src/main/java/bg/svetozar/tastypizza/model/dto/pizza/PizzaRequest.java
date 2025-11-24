package bg.svetozar.tastypizza.model.dto.pizza;

import bg.svetozar.tastypizza.model.dto.pizzaAllowedIngredient.PizzaAllowedIngredientRequest;
import bg.svetozar.tastypizza.model.dto.pizzaIngredient.PizzaIngredientRequest;

import java.math.BigDecimal;
import java.util.List;

public record PizzaRequest(
        String name,
        String description,
        String basePrice,
        String imageUrl,
        String spicyLevel,
        List<PizzaVariantRequest> variants,
        List<PizzaIngredientRequest> ingredients,
        List<PizzaAllowedIngredientRequest> allowedIngredients
) {
}