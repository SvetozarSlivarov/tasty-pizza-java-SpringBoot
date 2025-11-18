package bg.svetozar.tastypizza.model.dto.pizza;

import java.math.BigDecimal;
import java.util.List;

public record PizzaRequest(
        String name,
        String description,
        BigDecimal basePrice,
        boolean available,
        String imageUrl,
        String spicyLevel,
        List<PizzaVariantRequest> variants
) {
}