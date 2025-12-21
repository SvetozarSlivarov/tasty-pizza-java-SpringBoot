package bg.svetozar.tastypizza.model.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateCartItemRequest(
        @Min(value = 1, message = "quantity must be >= 1")
        Integer quantity,

        @Size(max = 300, message = "note must be <= 300 characters")
        String note,

        @Min(value = 1, message = "variantId must be >= 1")
        Long variantId,

        List<@Min(value = 1, message = "removeIngredientIds values must be >= 1") Long> removeIngredientIds,
        List<@Min(value = 1, message = "addIngredientIds values must be >= 1") Long> addIngredientIds
) {}
