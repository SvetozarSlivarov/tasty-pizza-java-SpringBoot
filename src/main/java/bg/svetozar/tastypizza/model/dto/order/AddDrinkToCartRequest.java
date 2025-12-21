package bg.svetozar.tastypizza.model.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddDrinkToCartRequest(
        @NotNull(message = "productId is required")
        @Min(value = 1, message = "productId must be >= 1")
        Long productId,

        @NotNull(message = "quantity is required")
        @Min(value = 1, message = "quantity must be >= 1")
        Integer quantity,

        @Size(max = 300, message = "note must be <= 300 characters")
        String note
) {}
