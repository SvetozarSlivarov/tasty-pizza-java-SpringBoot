package bg.svetozar.tastypizza.model.dto.ingredient;

public record IngredientRequest(
        String name,
        Long typeId
) {}
