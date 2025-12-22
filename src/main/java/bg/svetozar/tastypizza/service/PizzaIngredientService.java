package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.exception.ConflictException;
import bg.svetozar.tastypizza.exception.ErrorCode;
import bg.svetozar.tastypizza.exception.ErrorContext;
import bg.svetozar.tastypizza.exception.NotFoundException;
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
                        "Pizza not found: " + pizzaId,
                        ErrorCode.PIZZA_NOT_FOUND,
                        ErrorContext.of("pizzaId", pizzaId)
                ));

        Ingredient ingredient = ingredientRepository.findByIdAndDeletedFalse(request.ingredientId())
                .orElseThrow(() -> new NotFoundException(
                        "Ingredient not found: " + request.ingredientId(),
                        ErrorCode.INGREDIENT_NOT_FOUND,
                        ErrorContext.of("ingredientId", request.ingredientId())
                ));

        if (pizzaIngredientRepository.existsByPizza_IdAndIngredient_Id(pizzaId, request.ingredientId())) {
            throw new ConflictException(
                    "Ingredient already exists in pizza base recipe",
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
        PizzaIngredient entity = pizzaIngredientRepository.findByIdAndPizza_Id(id, pizzaId)
                .orElseThrow(() -> new NotFoundException(
                        "PizzaIngredient not found: " + id,
                        ErrorCode.PIZZA_INGREDIENT_NOT_FOUND,
                        ErrorContext.of("pizzaId", pizzaId, "id", id)
                ));

        Ingredient ingredient = ingredientRepository.findByIdAndDeletedFalse(request.ingredientId())
                .orElseThrow(() -> new NotFoundException(
                        "Ingredient not found: " + request.ingredientId(),
                        ErrorCode.INGREDIENT_NOT_FOUND,
                        ErrorContext.of("ingredientId", request.ingredientId())
                ));

        // duplicate-check при update (ако сменяме ingredient-а)
        if (pizzaIngredientRepository.existsByPizza_IdAndIngredient_Id(pizzaId, request.ingredientId())
                && (entity.getIngredient() == null || !entity.getIngredient().getId().equals(request.ingredientId()))) {

            throw new ConflictException(
                    "Ingredient already exists in pizza base recipe",
                    ErrorCode.PIZZA_INGREDIENT_ALREADY_EXISTS,
                    ErrorContext.of("pizzaId", pizzaId, "ingredientId", request.ingredientId())
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
                        "PizzaIngredient not found: " + id,
                        ErrorCode.PIZZA_INGREDIENT_NOT_FOUND,
                        ErrorContext.of("pizzaId", pizzaId, "id", id)
                ));

        pizzaIngredientRepository.delete(entity);
    }
}
