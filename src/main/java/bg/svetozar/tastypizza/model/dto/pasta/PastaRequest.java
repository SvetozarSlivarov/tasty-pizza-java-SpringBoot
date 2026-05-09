package bg.svetozar.tastypizza.model.dto.pasta;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

import static bg.svetozar.tastypizza.exception.ErrorMessage.*;

public record PastaRequest(
        @NotBlank(message = REQUIRED_NAME)
        @Size(min = 2, max = 100, message = INVALID_PASTA_NAME_BETWEEN_2_100_CHARS)
        String name,

        @Size(max = 1000, message = INVALID_PASTA_DESCRIPTION_MAX_1000_CHARS)
        String description,

        @NotBlank(message = REQUIRED_PASTA_BASE_PRICE)
        @Pattern(
                regexp = "^(?:0|[1-9]\\d*)(?:\\.\\d{1,2})?$",
                message = INVALID_PASTA_BASE_PRICE_2_DECIMALS
        )
        String basePrice,

        String imageBase64,

        @Valid
        @NotNull(message = REQUIRED_PASTA_SAUCES)
        List<@Valid PastaSauceRequest> sauces,

        @Valid
        @NotNull(message = REQUIRED_ALLOWED_INGREDIENTS)
        List<@Valid PastaAllowedIngredientRequest> allowedIngredients
) {
}
