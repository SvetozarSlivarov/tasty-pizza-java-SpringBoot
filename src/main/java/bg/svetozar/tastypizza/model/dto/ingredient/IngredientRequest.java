package bg.svetozar.tastypizza.model.dto.ingredient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_NAME;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_INGREDIENT_NAME_BETWEEN_2_100_CHARS;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_TYPE_ID;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_TYPE_ID_POSITIVE;

public record IngredientRequest(
        @NotBlank(message = REQUIRED_NAME)
        @Size(min = 2,max = 100, message = INVALID_INGREDIENT_NAME_BETWEEN_2_100_CHARS)
        String name,

        @NotNull(message = REQUIRED_TYPE_ID)
        @Positive(message = INVALID_TYPE_ID_POSITIVE)
        Long typeId
) {}
