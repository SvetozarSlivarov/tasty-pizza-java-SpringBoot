package bg.svetozar.tastypizza.model.mapper;

import bg.svetozar.tastypizza.model.dto.order.CartCustomizationDto;
import bg.svetozar.tastypizza.model.dto.order.CartDto;
import bg.svetozar.tastypizza.model.dto.order.CartItemDto;
import bg.svetozar.tastypizza.model.entity.Order;
import bg.svetozar.tastypizza.model.entity.OrderItem;
import bg.svetozar.tastypizza.model.entity.OrderItemCustomization;
import bg.svetozar.tastypizza.model.entity.PizzaVariant;

import java.math.BigDecimal;
import java.util.List;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static CartDto toCartDto(Order order) {
        List<CartItemDto> items = order.getItems()
                .stream()
                .map(OrderMapper::toCartItemDto)
                .toList();

        BigDecimal total = items.stream()
                .map(it -> new BigDecimal(it.unitPrice())
                        .multiply(BigDecimal.valueOf(it.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartDto(
                order.getId(),
                order.getStatus().name(),
                order.getDeliveryPhone(),
                order.getDeliveryAddress(),
                total.toString(),
                items
        );
    }

    private static CartItemDto toCartItemDto(OrderItem item) {
        var product = item.getProduct();

        Long variantId = null;
        String variantLabel = null;

        PizzaVariant variant = item.getPizzaVariant();
        if (variant != null) {
            variantId = variant.getId();
            variantLabel = variant.getSize().name() + " · " + variant.getDough().name();
        }

        List<CartCustomizationDto> customizations = item.getCustomizations()
                .stream()
                .map(OrderMapper::toCartCustomizationDto)
                .toList();

        return new CartItemDto(
                item.getId(),
                product.getId(),
                product.getName(),
                product.getType().name(),
                product.getImageUrl(),
                variantId,
                variantLabel,
                item.getQuantity(),
                item.getUnitPrice().toString(),
                item.getNote(),
                customizations
        );
    }

    private static CartCustomizationDto toCartCustomizationDto(OrderItemCustomization customization) {
        return new CartCustomizationDto(
                customization.getIngredient().getId(),
                customization.getIngredient().getName(),
                customization.getAction().name()
        );
    }
}
