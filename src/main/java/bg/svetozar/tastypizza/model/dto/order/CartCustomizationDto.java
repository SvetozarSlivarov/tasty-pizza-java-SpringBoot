package bg.svetozar.tastypizza.model.dto.order;

public record CartCustomizationDto(
        Long ingredientId,
        String ingredientName,
        String action // "ADD" / "REMOVE"
) {
}
