package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.model.dto.pizzaAllowedIngredient.PizzaAllowedIngredientRequest;
import bg.svetozar.tastypizza.model.dto.pizzaAllowedIngredient.PizzaAllowedIngredientDto;
import bg.svetozar.tastypizza.model.entity.Ingredient;
import bg.svetozar.tastypizza.model.entity.Pizza;
import bg.svetozar.tastypizza.model.entity.PizzaAllowedIngredient;
import bg.svetozar.tastypizza.model.mapper.PizzaAllowedIngredientMapper;
import bg.svetozar.tastypizza.repository.IngredientRepository;
import bg.svetozar.tastypizza.repository.PizzaAllowedIngredientRepository;
import bg.svetozar.tastypizza.repository.PizzaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PizzaAllowedIngredientService {

    private final PizzaRepository pizzaRepository;
    private final IngredientRepository ingredientRepository;
    private final PizzaAllowedIngredientRepository allowedIngredientRepository;
    private final PizzaAllowedIngredientMapper mapper;

    public List<PizzaAllowedIngredientDto> getByPizzaId(Long pizzaId) {
        List<PizzaAllowedIngredient> entities = allowedIngredientRepository.findAllByPizza_Id(pizzaId);
        return mapper.toResponseList(entities);
    }

    public PizzaAllowedIngredientDto addAllowedIngredient(Long pizzaId, PizzaAllowedIngredientRequest request) {
        Pizza pizza = pizzaRepository.findById(pizzaId)
                .orElseThrow(() -> new IllegalArgumentException("Pizza not found: " + pizzaId));

        Ingredient ingredient = ingredientRepository.findByIdAndDeletedFalse(request.ingredientId())
                .orElseThrow(() -> new IllegalArgumentException("Ingredient not found or deleted: " + request.ingredientId()));

        BigDecimal extraPrice = new BigDecimal(request.extraPrice());

        PizzaAllowedIngredient entity = PizzaAllowedIngredient.builder()
                .pizza(pizza)
                .ingredient(ingredient)
                .extraPrice(extraPrice)
                .build();

        PizzaAllowedIngredient saved = allowedIngredientRepository.save(entity);
        return mapper.toResponse(saved);
    }

    public PizzaAllowedIngredientDto update(Long id, PizzaAllowedIngredientRequest request) {
        PizzaAllowedIngredient entity = allowedIngredientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("PizzaAllowedIngredient not found: " + id));

        Ingredient ingredient = ingredientRepository.findByIdAndDeletedFalse(request.ingredientId())
                .orElseThrow(() -> new IllegalArgumentException("Ingredient not found or deleted: " + request.ingredientId()));

        entity.setIngredient(ingredient);
        entity.setExtraPrice(new BigDecimal(request.extraPrice()));

        PizzaAllowedIngredient saved = allowedIngredientRepository.save(entity);
        return mapper.toResponse(saved);
    }

    public void delete(Long id) {
        if (!allowedIngredientRepository.existsById(id)) {
            throw new IllegalArgumentException("PizzaAllowedIngredient not found: " + id);
        }
        allowedIngredientRepository.deleteById(id);
    }
}
