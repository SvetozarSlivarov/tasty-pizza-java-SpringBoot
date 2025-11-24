package bg.svetozar.tastypizza.model.dto.pizzaIngredient;

public record PizzaIngredientRequest(
        Long ingredientId,
        boolean removable
) {}