package bg.svetozar.tastypizza.model.mapper;

import bg.svetozar.tastypizza.model.dto.pasta.PastaAllowedIngredientDto;
import bg.svetozar.tastypizza.model.dto.pasta.PastaDto;
import bg.svetozar.tastypizza.model.dto.pasta.PastaSauceDto;
import bg.svetozar.tastypizza.model.entity.Pasta;
import bg.svetozar.tastypizza.model.entity.PastaAllowedIngredient;
import bg.svetozar.tastypizza.model.entity.PastaSauce;

import java.util.List;

public final class PastaMapper {

    private PastaMapper() {
    }

    public static PastaDto toPastaDto(Pasta pasta) {
        return map(pasta, true);
    }

    public static PastaDto toPastaDtoWithoutFullData(Pasta pasta) {
        return map(pasta, false);
    }

    private static PastaDto map(Pasta pasta, boolean includeDetails) {
        var product = pasta.getProduct();

        List<PastaSauceDto> sauceDtos =
                includeDetails && pasta.getSauces() != null
                        ? pasta.getSauces().stream()
                        .map(PastaMapper::toPastaSauceDto)
                        .toList()
                        : List.of();

        List<PastaAllowedIngredientDto> allowedIngredientDtos =
                includeDetails && pasta.getAllowedIngredients() != null
                        ? pasta.getAllowedIngredients().stream()
                        .map(PastaMapper::toPastaAllowedIngredientDto)
                        .toList()
                        : List.of();

        return new PastaDto(
                pasta.getId(),
                product.getName(),
                product.getDescription(),
                product.getBasePrice().toString(),
                product.getType().toString(),
                product.isDeleted(),
                product.getDeletedAt(),
                product.getImageUrl(),
                sauceDtos,
                allowedIngredientDtos
        );
    }

    public static PastaSauceDto toPastaSauceDto(PastaSauce entity) {
        return new PastaSauceDto(
                entity.getId(),
                entity.getPasta().getId(),
                entity.getIngredient().getId(),
                entity.getIngredient().getName(),
                entity.getExtraPrice().toString(),
                entity.getSpicyLevel() != null ? entity.getSpicyLevel().name() : null
        );
    }

    public static PastaAllowedIngredientDto toPastaAllowedIngredientDto(PastaAllowedIngredient entity) {
        return new PastaAllowedIngredientDto(
                entity.getId(),
                entity.getPasta().getId(),
                entity.getIngredient().getId(),
                entity.getIngredient().getName(),
                entity.getExtraPrice().toString()
        );
    }
}
