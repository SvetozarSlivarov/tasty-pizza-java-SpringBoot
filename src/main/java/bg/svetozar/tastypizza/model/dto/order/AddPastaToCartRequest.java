package bg.svetozar.tastypizza.model.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

import static bg.svetozar.tastypizza.exception.ErrorMessage.*;

public record AddPastaToCartRequest(
        @NotNull(message = REQUIRED_PRODUCT_ID)
        @Min(value = 1, message = INVALID_PRODUCT_ID_POSITIVE)
        Long productId,

        @NotNull(message = REQUIRED_PASTA_SAUCE_ID)
        @Min(value = 1, message = INVALID_PASTA_SAUCE_ID_POSITIVE)
        Long pastaSauceId,

        @NotNull(message = REQUIRED_QUANTITY)
        @Min(value = 1, message = INVALID_QUANTITY_POSITIVE)
        Integer quantity,

        @Size(max = 300, message = INVALID_NOTE_MAX_300_CHARS)
        String note,

        List<@Min(value = 1, message = INVALID_ADD_INGREDIENT_IDS_POSITIVE) Long> addIngredientIds
) {
}
