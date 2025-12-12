package bg.svetozar.tastypizza.model.mapper;

import bg.svetozar.tastypizza.model.dto.pizza.PizzaDto;
import bg.svetozar.tastypizza.model.dto.pizza.PizzaVariantDto;
import bg.svetozar.tastypizza.model.dto.pizzaAllowedIngredient.PizzaAllowedIngredientDto;
import bg.svetozar.tastypizza.model.dto.pizzaIngredient.PizzaIngredientDto;
import bg.svetozar.tastypizza.model.entity.Pizza;
import bg.svetozar.tastypizza.model.entity.PizzaAllowedIngredient;
import bg.svetozar.tastypizza.model.entity.PizzaIngredient;
import bg.svetozar.tastypizza.model.entity.PizzaVariant;

import java.util.List;

public final class PizzaMapper {

    private PizzaMapper() {
    }

    public static PizzaDto toPizzaDto(Pizza pizza) {
        return map(pizza, true);
    }

    public static PizzaDto toPizzaDtoWithoutFullData(Pizza pizza) {
        return map(pizza, false);
    }

    private static PizzaDto map(Pizza pizza, boolean includeDetails) {
        var product = pizza.getProduct();

        List<PizzaVariantDto> variantDtos =
                includeDetails && pizza.getVariants() != null
                        ? pizza.getVariants().stream()
                        .map(PizzaMapper::toVariantDto)
                        .toList()
                        : List.of();

        List<PizzaIngredientDto> ingredientDtos =
                includeDetails && pizza.getIngredients() != null
                        ? pizza.getIngredients().stream()
                        .map(PizzaMapper::toPizzaIngredientDto)
                        .toList()
                        : List.of();

        List<PizzaAllowedIngredientDto> allowedIngredientDtos =
                includeDetails && pizza.getAllowedIngredients() != null
                        ? pizza.getAllowedIngredients().stream()
                        .map(PizzaMapper::toPizzaAllowedIngredientDto)
                        .toList()
                        : List.of();

        return new PizzaDto(
                pizza.getId(),
                product.getName(),
                product.getDescription(),
                product.getBasePrice().toString(),
                product.getType().toString(),
                product.isDeleted(),
                product.getDeletedAt(),
                pizza.getSpicyLevel() != null ? pizza.getSpicyLevel().name() : null,
                product.getImageUrl(),
                variantDtos,
                ingredientDtos,
                allowedIngredientDtos
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

    private static PizzaIngredientDto toPizzaIngredientDto(PizzaIngredient entity) {
        return new PizzaIngredientDto(
                entity.getId(),
                entity.getIngredient().getId(),
                entity.getPizza().getId(),
                entity.getIngredient().getName(),
                entity.isRemovable()
        );
    }

    private static PizzaAllowedIngredientDto toPizzaAllowedIngredientDto(PizzaAllowedIngredient entity) {
        return new PizzaAllowedIngredientDto(
                entity.getId(),
                entity.getIngredient().getId(),
                entity.getPizza().getId(),
                entity.getIngredient().getName(),
                entity.getExtraPrice().toString()
        );
    }
}
