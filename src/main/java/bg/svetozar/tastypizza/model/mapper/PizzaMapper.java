package bg.svetozar.tastypizza.model.mapper;

import bg.svetozar.tastypizza.model.dto.pizza.PizzaDto;
import bg.svetozar.tastypizza.model.dto.pizza.PizzaVariantDto;
import bg.svetozar.tastypizza.model.entity.Pizza;
import bg.svetozar.tastypizza.model.entity.PizzaVariant;

import java.math.BigDecimal;
import java.util.List;

public final class PizzaMapper {

    private PizzaMapper() {
    }

    public static PizzaDto toDto(Pizza pizza) {
        var product = pizza.getProduct();

        List<PizzaVariantDto> variants = pizza.getVariants().stream()
                .map(PizzaMapper::toVariantDto)
                .toList();

        return new PizzaDto(
                pizza.getId(),
                product.getName(),
                product.getDescription(),
                product.getBasePrice().toString(),
                product.isAvailable(),
                pizza.getSpicyLevel() != null ? pizza.getSpicyLevel().name() : null,
                product.getImageUrl(),
                variants
        );
    }
    public static PizzaDto toDtoWithoutVariants(Pizza pizza) {
        var product = pizza.getProduct();

        return new PizzaDto(
                pizza.getId(),
                product.getName(),
                product.getDescription(),
                product.getBasePrice().toString(),
                product.isAvailable(),
                pizza.getSpicyLevel() != null ? pizza.getSpicyLevel().name() : null,
                product.getImageUrl(),
                List.of()
        );
    }


    private static PizzaVariantDto toVariantDto(PizzaVariant variant) {
        return new PizzaVariantDto(
                variant.getId(),
                variant.getSize().name(),
                variant.getDough().name(),
                variant.getExtraPrice().toString()
        );
    }
}
