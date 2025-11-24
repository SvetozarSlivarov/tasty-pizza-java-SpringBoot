package bg.svetozar.tastypizza.model.dto.pizzaAllowedIngredient;

public record PizzaAllowedIngredientDto(
        Long id,
        Long pizzaId,
        Long ingredientId,
        String ingredientName,
        String extraPrice
) {}