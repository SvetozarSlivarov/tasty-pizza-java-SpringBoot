package bg.svetozar.tastypizza.model.dto.admin;

import bg.svetozar.tastypizza.model.enums.ProductType;

import java.math.BigDecimal;
import java.util.List;

public record AdminOrderItemDto(
        Long id,
        String name,
        ProductType type,
        String imageUrl,
        String variantLabel,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal,
        String note,
        List<AdminOrderItemCustomizationDto> customizations
) {}
