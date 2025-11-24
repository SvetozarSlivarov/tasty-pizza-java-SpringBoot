package bg.svetozar.tastypizza.model.dto.pizzaAllowedIngredient;

public record PizzaAllowedIngredientRequest(
        Long ingredientId,
        String extraPrice
) {}
