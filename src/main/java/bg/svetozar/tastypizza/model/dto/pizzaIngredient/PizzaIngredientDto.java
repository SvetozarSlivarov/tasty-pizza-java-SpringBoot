package bg.svetozar.tastypizza.model.dto.pizzaIngredient;

public record PizzaIngredientDto(
        Long id,
        Long pizzaId,
        Long ingredientId,
        String ingredientName,
        boolean removable
) {}
