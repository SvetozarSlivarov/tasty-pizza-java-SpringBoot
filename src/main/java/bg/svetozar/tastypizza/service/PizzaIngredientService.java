package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.model.dto.pizzaIngredient.PizzaIngredientRequest;
import bg.svetozar.tastypizza.model.dto.pizzaIngredient.PizzaIngredientDto;
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
                .orElseThrow(() -> new IllegalArgumentException("Pizza not found: " + pizzaId));

        Ingredient ingredient = ingredientRepository.findByIdAndDeletedFalse(request.ingredientId())
                .orElseThrow(() -> new IllegalArgumentException("Ingredient not found or deleted: " + request.ingredientId()));

        PizzaIngredient entity = PizzaIngredient.builder()
                .pizza(pizza)
                .ingredient(ingredient)
                .removable(request.removable())
                .build();

        PizzaIngredient saved = pizzaIngredientRepository.save(entity);
        return mapper.toResponse(saved);
    }

    public PizzaIngredientDto update(Long id, PizzaIngredientRequest request) {
        PizzaIngredient entity = pizzaIngredientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("PizzaIngredient not found: " + id));

        Ingredient ingredient = ingredientRepository.findByIdAndDeletedFalse(request.ingredientId())
                .orElseThrow(() -> new IllegalArgumentException("Ingredient not found or deleted: " + request.ingredientId()));

        entity.setIngredient(ingredient);
        entity.setRemovable(request.removable());

        PizzaIngredient saved = pizzaIngredientRepository.save(entity);
        return mapper.toResponse(saved);
    }

    public void delete(Long id) {
        if (!pizzaIngredientRepository.existsById(id)) {
            throw new IllegalArgumentException("PizzaIngredient not found: " + id);
        }
        pizzaIngredientRepository.deleteById(id);
    }
}
