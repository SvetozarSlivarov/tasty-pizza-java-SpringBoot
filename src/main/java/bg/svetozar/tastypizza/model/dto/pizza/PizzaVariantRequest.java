package bg.svetozar.tastypizza.model.dto.pizza;

import jakarta.validation.constraints.*;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_PIZZA_SIZE;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_PIZZA_SIZE_MAX_20_CHARS;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_PIZZA_DOUGH;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_PIZZA_DOUGH_MAX_20_CHARS;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_EXTRA_PRICE;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_EXTRA_PRICE_2_DECIMALS;

public record PizzaVariantRequest(
        @NotBlank(message = REQUIRED_PIZZA_SIZE)
        @Size(max = 20, message = INVALID_PIZZA_SIZE_MAX_20_CHARS)
        String size,

        @NotBlank(message = REQUIRED_PIZZA_DOUGH)
        @Size(max = 20, message = INVALID_PIZZA_DOUGH_MAX_20_CHARS)
        String dough,

        @NotBlank(message = REQUIRED_EXTRA_PRICE)
        @Pattern(
                regexp = "^(?:0|[1-9]\\d*)(?:\\.\\d{1,2})?$",
                message = INVALID_EXTRA_PRICE_2_DECIMALS
        )
        String extraPrice
) {
}
