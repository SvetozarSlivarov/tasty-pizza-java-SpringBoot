package bg.svetozar.tastypizza.model.dto.ingredientType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_NAME;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_TYPE_NAME_BETWEEN_2_50_CHARS;

public record IngredientTypeRequest(
        @NotBlank(message = REQUIRED_NAME)
        @Size(min = 2, max = 50, message = INVALID_TYPE_NAME_BETWEEN_2_50_CHARS)
        String name
) {}
