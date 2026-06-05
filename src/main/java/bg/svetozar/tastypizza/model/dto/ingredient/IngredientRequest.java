package bg.svetozar.tastypizza.model.dto.ingredient;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_TYPE_ID;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_TYPE_ID_POSITIVE;

import java.util.Map;

public record IngredientRequest(
        String name,

        @NotNull(message = REQUIRED_TYPE_ID)
        @Positive(message = INVALID_TYPE_ID_POSITIVE)
        Long typeId,

        Map<String, Map<String, String>> translations,

        Map<String, Map<String, String>> fields
) {}
