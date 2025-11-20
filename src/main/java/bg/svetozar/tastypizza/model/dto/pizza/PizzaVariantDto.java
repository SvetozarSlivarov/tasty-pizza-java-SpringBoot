package bg.svetozar.tastypizza.model.dto.pizza;

import java.math.BigDecimal;

public record PizzaVariantDto(
        Long id,
        String size,
        String dough,
        String extraPrice
) {
}
