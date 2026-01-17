package bg.svetozar.tastypizza.model.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_PRODUCT_ID;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_PRODUCT_ID_POSITIVE;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_QUANTITY;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_QUANTITY_POSITIVE;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_NOTE_MAX_300_CHARS;


public record AddDrinkToCartRequest(
        @NotNull(message = REQUIRED_PRODUCT_ID)
        @Min(value = 1, message = INVALID_PRODUCT_ID_POSITIVE)
        Long productId,

        @NotNull(message = REQUIRED_QUANTITY)
        @Min(value = 1, message = INVALID_QUANTITY_POSITIVE)
        Integer quantity,

        @Size(max = 300, message = INVALID_NOTE_MAX_300_CHARS)
        String note
) {}
