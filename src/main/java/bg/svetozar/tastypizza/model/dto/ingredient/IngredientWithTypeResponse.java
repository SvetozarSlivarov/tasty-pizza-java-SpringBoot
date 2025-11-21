package bg.svetozar.tastypizza.model.dto.ingredient;

import bg.svetozar.tastypizza.model.dto.ingredientType.IngredientTypeResponse;

import java.time.LocalDateTime;

public record IngredientWithTypeResponse(
        Long id,
        String name,
        IngredientTypeResponse type,
        boolean deleted,
        LocalDateTime deletedAt
) {}
