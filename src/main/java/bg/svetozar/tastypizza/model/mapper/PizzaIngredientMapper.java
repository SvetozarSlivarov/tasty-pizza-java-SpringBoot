package bg.svetozar.tastypizza.model.mapper;

import bg.svetozar.tastypizza.model.dto.pizzaIngredient.PizzaIngredientDto;
import bg.svetozar.tastypizza.model.entity.PizzaIngredient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PizzaIngredientMapper {

    public PizzaIngredientDto toResponse(PizzaIngredient entity) {
        if (entity == null) {
            return null;
        }

        return new PizzaIngredientDto(
                entity.getId(),
                entity.getPizza().getId(),
                entity.getIngredient().getId(),
                entity.getIngredient().getName(),
                entity.isRemovable()
        );
    }

    public List<PizzaIngredientDto> toResponseList(List<PizzaIngredient> entities) {
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }
}
