package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.exception.*;
import bg.svetozar.tastypizza.model.dto.pizzaIngredient.PizzaIngredientDto;
import bg.svetozar.tastypizza.model.dto.pizzaIngredient.PizzaIngredientRequest;
import bg.svetozar.tastypizza.model.entity.Ingredient;
import bg.svetozar.tastypizza.model.entity.Pizza;
import bg.svetozar.tastypizza.model.entity.PizzaIngredient;
import bg.svetozar.tastypizza.model.mapper.PizzaIngredientMapper;
import bg.svetozar.tastypizza.repository.IngredientRepository;
import bg.svetozar.tastypizza.repository.PizzaIngredientRepository;
import bg.svetozar.tastypizza.repository.PizzaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static bg.svetozar.tastypizza.exception.ErrorMessage.INGREDIENT_NOT_FOUND_WITH_ID;
import static bg.svetozar.tastypizza.exception.ErrorMessage.PIZZA_INGREDIENT_ALREADY_EXISTS;
import static bg.svetozar.tastypizza.exception.ErrorMessage.PIZZA_INGREDIENT_NOT_FOUND_WITH_ID;
import static bg.svetozar.tastypizza.exception.ErrorMessage.PIZZA_NOT_FOUND_WITH_ID;

@Service
@Transactional
@RequiredArgsConstructor
public class PizzaIngredientService {

    private final PizzaRepository pizzaRepository;
    private final IngredientRepository ingredientRepository;
    private final PizzaIngredientRepository pizzaIngredientRepository;
    private final PizzaIngredientMapper mapper;

    public List<PizzaIngredientDto> getByPizzaId(Long pizzaId) {
        List<PizzaIngredient> entities = pizzaIngredientRepository.findAllByPizza_Id(pizzaId);
        return mapper.toResponseList(entities);
    }

    public PizzaIngredientDto addIngredientToPizza(Long pizzaId, PizzaIngredientRequest request) {
        Pizza pizza = pizzaRepository.findById(pizzaId)
                .orElseThrow(() -> new NotFoundException(
                        PIZZA_NOT_FOUND_WITH_ID + pizzaId,
                        ErrorCode.PIZZA_NOT_FOUND,
                        ErrorContext.of("pizzaId", pizzaId)
                ));

        Ingredient ingredient = ingredientRepository.findByIdAndDeletedFalse(request.ingredientId())
                .orElseThrow(() -> new NotFoundException(
                        INGREDIENT_NOT_FOUND_WITH_ID + request.ingredientId(),
                        ErrorCode.INGREDIENT_NOT_FOUND,
                        ErrorContext.of("ingredientId", request.ingredientId())
                ));

        if (pizzaIngredientRepository.existsByPizza_IdAndIngredient_Id(pizzaId, request.ingredientId())) {
            throw new ConflictException(
                    PIZZA_INGREDIENT_ALREADY_EXISTS,
                    ErrorCode.PIZZA_INGREDIENT_ALREADY_EXISTS,
                    ErrorContext.of("pizzaId", pizzaId, "ingredientId", request.ingredientId())
            );
        }

        PizzaIngredient entity = PizzaIngredient.builder()
                .pizza(pizza)
                .ingredient(ingredient)
                .removable(request.removable())
                .build();

        PizzaIngredient saved = pizzaIngredientRepository.save(entity);
        return mapper.toResponse(saved);
    }

    public PizzaIngredientDto update(Long pizzaId, Long id, PizzaIngredientRequest request) {
        Long ingredientId = request.ingredientId();

        var ctxEntity = ErrorContext.of("pizzaId", pizzaId, "id", id);
        var ctxIngredient = ErrorContext.of("ingredientId", ingredientId);
        var ctxConflict = ErrorContext.of("pizzaId", pizzaId, "ingredientId", ingredientId);

        PizzaIngredient entity = pizzaIngredientRepository.findByIdAndPizza_Id(id, pizzaId)
                .orElseThrow(() -> new NotFoundException(
                        PIZZA_INGREDIENT_NOT_FOUND_WITH_ID + id,
                        ErrorCode.PIZZA_INGREDIENT_NOT_FOUND,
                        ctxEntity
                ));

        Ingredient ingredient = ingredientRepository.findByIdAndDeletedFalse(ingredientId)
                .orElseThrow(() -> new NotFoundException(
                        INGREDIENT_NOT_FOUND_WITH_ID + ingredientId,
                        ErrorCode.INGREDIENT_NOT_FOUND,
                        ctxIngredient
                ));

        boolean ingredientAlreadyExists =
                pizzaIngredientRepository.existsByPizza_IdAndIngredient_Id(pizzaId, ingredientId);

        Long currentIngredientId = entity.getIngredient() != null ? entity.getIngredient().getId() : null;
        boolean isDifferentIngredient = !Objects.equals(currentIngredientId, ingredientId);

        if (ingredientAlreadyExists && isDifferentIngredient) {
            throw new ConflictException(
                    PIZZA_INGREDIENT_ALREADY_EXISTS,
                    ErrorCode.PIZZA_INGREDIENT_ALREADY_EXISTS,
                    ctxConflict
            );
        }

        entity.setIngredient(ingredient);
        entity.setRemovable(request.removable());

        PizzaIngredient saved = pizzaIngredientRepository.save(entity);
        return mapper.toResponse(saved);
    }

    public void delete(Long pizzaId, Long id) {
        PizzaIngredient entity = pizzaIngredientRepository.findByIdAndPizza_Id(id, pizzaId)
                .orElseThrow(() -> new NotFoundException(
                        PIZZA_INGREDIENT_NOT_FOUND_WITH_ID + id,
                        ErrorCode.PIZZA_INGREDIENT_NOT_FOUND,
                        ErrorContext.of("pizzaId", pizzaId, "id", id)
                ));

        pizzaIngredientRepository.delete(entity);
    }
}
