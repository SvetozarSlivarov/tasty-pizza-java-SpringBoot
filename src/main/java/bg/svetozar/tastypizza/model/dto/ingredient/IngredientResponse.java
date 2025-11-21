package bg.svetozar.tastypizza.model.dto.ingredient;

import java.time.LocalDateTime;

public record IngredientResponse(
        Long id,
        String name,
        boolean deleted,
        LocalDateTime deletedAt
) {}
