package bg.svetozar.tastypizza.model.dto.drink;

public record DrinkRequest(
        String name,
        String description,
        String basePrice,
        String imageUrl
) {}
