package bg.svetozar.tastypizza.model.dto.pizza;

import java.math.BigDecimal;

public record PizzaVariantRequest(
        String size,
        String dough,
        String extraPrice
) {
}
