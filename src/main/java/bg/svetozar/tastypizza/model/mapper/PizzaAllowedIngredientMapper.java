package bg.svetozar.tastypizza.model.mapper;

import bg.svetozar.tastypizza.model.dto.pizzaAllowedIngredient.PizzaAllowedIngredientDto;
import bg.svetozar.tastypizza.model.entity.PizzaAllowedIngredient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PizzaAllowedIngredientMapper {

    public PizzaAllowedIngredientDto toResponse(PizzaAllowedIngredient entity) {
        if (entity == null) {
            return null;
        }

        return new PizzaAllowedIngredientDto(
                entity.getId(),
                entity.getPizza().getId(),
                entity.getIngredient().getId(),
                entity.getIngredient().getName(),
                entity.getExtraPrice().toString()
        );
    }

    public List<PizzaAllowedIngredientDto> toResponseList(List<PizzaAllowedIngredient> entities) {
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }
}
