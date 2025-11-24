package bg.svetozar.tastypizza.model.dto.ingredient;

import java.time.LocalDateTime;

public record IngredientDto(
        Long id,
        String name,
        boolean deleted,
        LocalDateTime deletedAt
) {}
