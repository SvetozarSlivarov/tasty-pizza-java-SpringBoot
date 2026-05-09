package bg.svetozar.tastypizza.model.dto.pasta;

import java.time.LocalDateTime;
import java.util.List;

public record PastaDto(
        Long id,
        String name,
        String description,
        String basePrice,
        String type,
        boolean deleted,
        LocalDateTime deletedAt,
        String imageUrl,
        List<PastaSauceDto> sauces,
        List<PastaAllowedIngredientDto> allowedIngredients
) {
}
