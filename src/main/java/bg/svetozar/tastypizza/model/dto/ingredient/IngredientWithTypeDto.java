package bg.svetozar.tastypizza.model.dto.ingredient;

import bg.svetozar.tastypizza.model.dto.ingredientType.IngredientTypeDto;

import java.time.LocalDateTime;

public record IngredientWithTypeDto(
        Long id,
        String name,
        IngredientTypeDto type,
        boolean deleted,
        LocalDateTime deletedAt
) {}
