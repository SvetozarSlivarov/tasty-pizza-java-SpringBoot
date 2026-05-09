package bg.svetozar.tastypizza.model.dto.pasta;

public record PastaSauceDto(
        Long id,
        Long pastaId,
        Long ingredientId,
        String ingredientName,
        String extraPrice,
        String spicyLevel
) {
}
