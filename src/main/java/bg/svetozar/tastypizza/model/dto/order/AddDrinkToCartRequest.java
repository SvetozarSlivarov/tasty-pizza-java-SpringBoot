package bg.svetozar.tastypizza.model.dto.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AddDrinkToCartRequest(
        @NotNull
        Long productId,

        @Positive
        int quantity,

        @Size(max = 500)
        String note
) {
}
