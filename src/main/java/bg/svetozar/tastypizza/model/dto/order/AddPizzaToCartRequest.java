package bg.svetozar.tastypizza.model.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_PRODUCT_ID;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_PRODUCT_ID_POSITIVE;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_VARIANT_ID;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_VARIANT_ID_POSITIVE;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_QUANTITY;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_QUANTITY_POSITIVE;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_NOTE_MAX_300_CHARS;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_REMOVE_INGREDIENT_IDS_POSITIVE;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_ADD_INGREDIENT_IDS_POSITIVE;


import java.util.List;

public record AddPizzaToCartRequest(
        @NotNull(message = REQUIRED_PRODUCT_ID)
        @Min(value = 1, message = INVALID_PRODUCT_ID_POSITIVE)
        Long productId,

        @NotNull(message = REQUIRED_VARIANT_ID)
        @Min(value = 1, message = INVALID_VARIANT_ID_POSITIVE)
        Long variantId,

        @NotNull(message = REQUIRED_QUANTITY)
        @Min(value = 1, message = INVALID_QUANTITY_POSITIVE)
        Integer quantity,

        @Size(max = 300, message = INVALID_NOTE_MAX_300_CHARS)
        String note,

        List<@Min(value = 1, message = INVALID_REMOVE_INGREDIENT_IDS_POSITIVE) Long> removeIngredientIds,
        List<@Min(value = 1, message = INVALID_ADD_INGREDIENT_IDS_POSITIVE) Long> addIngredientIds
) {}
