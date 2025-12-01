package bg.svetozar.tastypizza.model.dto.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AddPizzaToCartRequest(
        @NotNull
        Long productId,

        @NotNull
        Long variantId,

        @Positive
        int quantity,

        @Size(max = 500)
        String note,

        List<Long> removeIngredientIds,

        List<Long> addIngredientIds
) {
}
