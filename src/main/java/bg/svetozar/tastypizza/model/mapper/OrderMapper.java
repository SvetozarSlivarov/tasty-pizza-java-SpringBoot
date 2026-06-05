package bg.svetozar.tastypizza.model.mapper;

import bg.svetozar.tastypizza.model.dto.order.CartCustomizationDto;
import bg.svetozar.tastypizza.model.dto.order.CartDto;
import bg.svetozar.tastypizza.model.dto.order.CartItemDto;
import bg.svetozar.tastypizza.model.entity.Order;
import bg.svetozar.tastypizza.model.entity.OrderItem;
import bg.svetozar.tastypizza.model.entity.OrderItemCustomization;
import bg.svetozar.tastypizza.model.entity.PastaSauce;
import bg.svetozar.tastypizza.model.entity.PizzaVariant;
import bg.svetozar.tastypizza.service.LocalizedTextService;

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

    public static CartDto toCartDto(Order order, LocalizedTextService localizedTextService, String lang) {
        List<CartItemDto> items = order.getItems()
                .stream()
                .map(item -> toCartItemDto(item, localizedTextService, lang))
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
        Long pastaSauceId = null;
        String pastaSauceName = null;
        String pastaSauceSpicyLevel = null;

        PizzaVariant variant = item.getPizzaVariant();
        if (variant != null) {
            variantId = variant.getId();
            variantLabel = variant.getSize().name() + " - " + variant.getDough().name();
        }

        PastaSauce pastaSauce = item.getPastaSauce();
        if (pastaSauce != null) {
            pastaSauceId = pastaSauce.getId();
            pastaSauceName = pastaSauce.getIngredient() != null ? pastaSauce.getIngredient().getName() : null;
            pastaSauceSpicyLevel = pastaSauce.getSpicyLevel() != null ? pastaSauce.getSpicyLevel().name() : null;
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
                pastaSauceId,
                pastaSauceName,
                pastaSauceSpicyLevel,
                item.getQuantity(),
                item.getUnitPrice().toString(),
                item.getNote(),
                customizations
        );
    }

    private static CartItemDto toCartItemDto(OrderItem item, LocalizedTextService localizedTextService, String lang) {
        var product = item.getProduct();

        Long variantId = null;
        String variantLabel = null;
        Long pastaSauceId = null;
        String pastaSauceName = null;
        String pastaSauceSpicyLevel = null;

        PizzaVariant variant = item.getPizzaVariant();
        if (variant != null) {
            variantId = variant.getId();
            variantLabel = variant.getSize().name() + " - " + variant.getDough().name();
        }

        PastaSauce pastaSauce = item.getPastaSauce();
        if (pastaSauce != null) {
            pastaSauceId = pastaSauce.getId();
            if (pastaSauce.getIngredient() != null) {
                pastaSauceName = localizedTextService.getTranslationOrDefault(
                        "INGREDIENT",
                        pastaSauce.getIngredient().getId(),
                        "name",
                        lang,
                        pastaSauce.getIngredient().getName()
                );
            }
            pastaSauceSpicyLevel = pastaSauce.getSpicyLevel() != null ? pastaSauce.getSpicyLevel().name() : null;
        }

        List<CartCustomizationDto> customizations = item.getCustomizations()
                .stream()
                .map(customization -> toCartCustomizationDto(customization, localizedTextService, lang))
                .toList();

        String productName = localizedTextService.getTranslationOrDefault(
                "PRODUCT",
                product.getId(),
                "name",
                lang,
                product.getName()
        );

        return new CartItemDto(
                item.getId(),
                product.getId(),
                productName,
                product.getType().name(),
                product.getImageUrl(),
                variantId,
                variantLabel,
                pastaSauceId,
                pastaSauceName,
                pastaSauceSpicyLevel,
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

    private static CartCustomizationDto toCartCustomizationDto(
            OrderItemCustomization customization,
            LocalizedTextService localizedTextService,
            String lang
    ) {
        String ingredientName = localizedTextService.getTranslationOrDefault(
                "INGREDIENT",
                customization.getIngredient().getId(),
                "name",
                lang,
                customization.getIngredient().getName()
        );

        return new CartCustomizationDto(
                customization.getIngredient().getId(),
                ingredientName,
                customization.getAction().name()
        );
    }
}
