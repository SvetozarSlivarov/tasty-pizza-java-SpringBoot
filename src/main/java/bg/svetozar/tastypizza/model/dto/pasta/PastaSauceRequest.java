package bg.svetozar.tastypizza.model.dto.pasta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import static bg.svetozar.tastypizza.exception.ErrorMessage.*;

public record PastaSauceRequest(
        @NotNull(message = REQUIRED_INGREDIENT_ID)
        @Positive(message = INVALID_INGREDIENT_ID_POSITIVE)
        Long ingredientId,

        @NotBlank(message = REQUIRED_EXTRA_PRICE)
        @Pattern(
                regexp = "^(?:0|[1-9]\\d*)(?:\\.\\d{1,2})?$",
                message = INVALID_EXTRA_PRICE_2_DECIMALS
        )
        String extraPrice,

        @NotBlank(message = REQUIRED_SPICY_LEVEL)
        @Size(max = 20, message = INVALID_SPICY_LEVEL_MAX_20)
        String spicyLevel
) {
}
