package bg.svetozar.tastypizza.model.dto.pizza;

import bg.svetozar.tastypizza.model.dto.pizzaAllowedIngredient.PizzaAllowedIngredientDto;
import bg.svetozar.tastypizza.model.dto.pizzaIngredient.PizzaIngredientDto;

import java.time.LocalDateTime;
import java.util.List;

public record PizzaDto(
        Long id,
        String name,
        String description,
        String basePrice,
        boolean deleted,
        LocalDateTime deletedAt,
        String spicyLevel,
        String imageUrl,
        List<PizzaVariantDto> variants,
        List<PizzaIngredientDto> ingredients,
        List<PizzaAllowedIngredientDto> allowedIngredients
) {
}
