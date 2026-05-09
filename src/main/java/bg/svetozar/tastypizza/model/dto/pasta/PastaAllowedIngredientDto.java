package bg.svetozar.tastypizza.model.dto.pasta;

public record PastaAllowedIngredientDto(
        Long id,
        Long pastaId,
        Long ingredientId,
        String ingredientName,
        String extraPrice
) {
}
