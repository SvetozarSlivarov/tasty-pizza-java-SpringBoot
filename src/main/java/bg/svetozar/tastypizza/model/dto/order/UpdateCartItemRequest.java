package bg.svetozar.tastypizza.model.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_QUANTITY_POSITIVE;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_NOTE_MAX_300_CHARS;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_VARIANT_ID_POSITIVE;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_PASTA_SAUCE_ID_POSITIVE;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_REMOVE_INGREDIENT_IDS_POSITIVE;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_ADD_INGREDIENT_IDS_POSITIVE;

import java.util.List;

public record UpdateCartItemRequest(
        @Min(value = 1, message = INVALID_QUANTITY_POSITIVE)
        Integer quantity,

        @Size(max = 300, message = INVALID_NOTE_MAX_300_CHARS)
        String note,

        @Min(value = 1, message = INVALID_VARIANT_ID_POSITIVE)
        Long variantId,

        @Min(value = 1, message = INVALID_PASTA_SAUCE_ID_POSITIVE)
        Long pastaSauceId,

        List<@Min(value = 1, message = INVALID_REMOVE_INGREDIENT_IDS_POSITIVE) Long> removeIngredientIds,
        List<@Min(value = 1, message = INVALID_ADD_INGREDIENT_IDS_POSITIVE) Long> addIngredientIds
) {}
