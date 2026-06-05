package bg.svetozar.tastypizza.model.dto.ingredientType;

import java.util.Map;

public record IngredientTypeRequest(
        String name,
        Map<String, Map<String, String>> translations,
        Map<String, Map<String, String>> fields
) {}
