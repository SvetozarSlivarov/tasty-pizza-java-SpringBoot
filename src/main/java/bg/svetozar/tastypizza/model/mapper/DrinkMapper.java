package bg.svetozar.tastypizza.model.mapper;

import bg.svetozar.tastypizza.model.dto.drink.DrinkDto;
import bg.svetozar.tastypizza.model.entity.Drink;

public final class DrinkMapper {

    private DrinkMapper() {}

    public static DrinkDto toDrinkDto(Drink drink) {
        var product = drink.getProduct();
        return toDrinkDto(drink, product.getName(), product.getDescription());
    }

    public static DrinkDto toDrinkDto(Drink drink, String name, String description) {
        var product = drink.getProduct();
        return new DrinkDto(
                drink.getId(),
                name,
                description,
                product.getBasePrice().toString(),
                product.getType().toString(),
                product.isDeleted(),
                product.getDeletedAt(),
                product.getImageUrl()
        );
    }
}
