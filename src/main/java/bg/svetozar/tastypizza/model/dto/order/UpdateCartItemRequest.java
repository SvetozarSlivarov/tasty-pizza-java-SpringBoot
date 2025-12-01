package bg.svetozar.tastypizza.model.dto.order;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateCartItemRequest(
        @Positive
        Integer quantity,

        @Size(max = 500)
        String note,

        Long variantId,

        List<Long> removeIngredientIds,

        List<Long> addIngredientIds
) {
}