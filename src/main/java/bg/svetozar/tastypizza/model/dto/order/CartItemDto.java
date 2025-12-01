package bg.svetozar.tastypizza.model.dto.order;

import java.util.List;

public record CartItemDto(
        Long id,
        Long productId,
        String productName,
        String productType,
        String imageUrl,
        Long variantId,
        String variantLabel,
        int quantity,
        String unitPrice,
        String note,
        List<CartCustomizationDto> customizations
) {
}
