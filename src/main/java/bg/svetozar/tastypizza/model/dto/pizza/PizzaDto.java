package bg.svetozar.tastypizza.model.dto.pizza;

import java.math.BigDecimal;
import java.util.List;

public record PizzaDto(
        Long id,
        String name,
        String description,
        String basePrice,
        boolean available,
        String spicyLevel,
        String imageUrl,
        List<PizzaVariantDto> variants
) {
}
