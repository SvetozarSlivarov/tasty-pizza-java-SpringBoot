package bg.svetozar.tastypizza.model.mapper;

import bg.svetozar.tastypizza.model.dto.drink.DrinkDto;
import bg.svetozar.tastypizza.model.entity.Drink;

public final class DrinkMapper {

    private DrinkMapper() {}

    public static DrinkDto toDrinkDto(Drink drink) {
        var product = drink.getProduct();

        return new DrinkDto(
                drink.getId(),
                product.getName(),
                product.getDescription(),
                product.getBasePrice().toString(),
                product.getType().toString(),
                product.isDeleted(),
                product.getDeletedAt(),
                product.getImageUrl()
        );
    }
}
