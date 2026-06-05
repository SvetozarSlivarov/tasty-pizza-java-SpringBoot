package bg.svetozar.tastypizza.model.dto.drink;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_NAME;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_DRINK_NAME_BETWEEN_2_80_CHARS;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_DRINK_DESCRIPTION_MAX_500_CHARS;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_DRINK_BASE_PRICE;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_DRINK_BASE_PRICE_2_DECIMALS;

import java.util.Map;

public record DrinkRequest(

        String name,

        @Size(max = 500, message = INVALID_DRINK_DESCRIPTION_MAX_500_CHARS)
        String description,

        @NotBlank(message = REQUIRED_DRINK_BASE_PRICE)
        @Pattern(
                regexp = "^(?:0|[1-9]\\d*)(?:\\.\\d{1,2})?$",
                message = INVALID_DRINK_BASE_PRICE_2_DECIMALS
        )
        String basePrice,

        String imageBase64,

        Map<String, Map<String, String>> translations,

        Map<String, Map<String, String>> fields
) {}
