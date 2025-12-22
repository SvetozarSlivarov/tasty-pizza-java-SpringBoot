package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.exception.BadRequestException;
import bg.svetozar.tastypizza.exception.ConflictException;
import bg.svetozar.tastypizza.exception.ErrorCode;
import bg.svetozar.tastypizza.exception.ErrorContext;
import bg.svetozar.tastypizza.exception.NotFoundException;
import bg.svetozar.tastypizza.model.dto.pizzaAllowedIngredient.PizzaAllowedIngredientDto;
import bg.svetozar.tastypizza.model.dto.pizzaAllowedIngredient.PizzaAllowedIngredientRequest;
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
        pizzaRepository.findById(pizzaId)
                .orElseThrow(() -> new NotFoundException(
                        "Pizza not found: " + pizzaId,
                        ErrorCode.PIZZA_NOT_FOUND,
                        ErrorContext.of("pizzaId", pizzaId)
                ));

        List<PizzaAllowedIngredient> entities = allowedIngredientRepository.findAllByPizza_Id(pizzaId);
        return mapper.toResponseList(entities);
    }

    public PizzaAllowedIngredientDto addAllowedIngredient(Long pizzaId, PizzaAllowedIngredientRequest request) {
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

        if (allowedIngredientRepository.existsByPizza_IdAndIngredient_Id(pizzaId, request.ingredientId())) {
            throw new ConflictException(
                    "Ingredient already allowed for this pizza",
                    ErrorCode.PIZZA_ALLOWED_INGREDIENT_ALREADY_EXISTS,
                    ErrorContext.of("pizzaId", pizzaId, "ingredientId", request.ingredientId())
            );
        }

        BigDecimal extraPrice = parseExtraPrice(request.extraPrice());

        PizzaAllowedIngredient entity = PizzaAllowedIngredient.builder()
                .pizza(pizza)
                .ingredient(ingredient)
                .extraPrice(extraPrice)
                .build();

        PizzaAllowedIngredient saved = allowedIngredientRepository.save(entity);
        return mapper.toResponse(saved);
    }

    public PizzaAllowedIngredientDto update(Long pizzaId, Long id, PizzaAllowedIngredientRequest request) {
        PizzaAllowedIngredient entity = allowedIngredientRepository.findByIdAndPizza_Id(id, pizzaId)
                .orElseThrow(() -> new NotFoundException(
                        "PizzaAllowedIngredient not found: " + id,
                        ErrorCode.PIZZA_ALLOWED_INGREDIENT_NOT_FOUND,
                        ErrorContext.of("pizzaId", pizzaId, "id", id)
                ));

        Ingredient ingredient = ingredientRepository.findByIdAndDeletedFalse(request.ingredientId())
                .orElseThrow(() -> new NotFoundException(
                        "Ingredient not found: " + request.ingredientId(),
                        ErrorCode.INGREDIENT_NOT_FOUND,
                        ErrorContext.of("ingredientId", request.ingredientId())
                ));

        if (allowedIngredientRepository.existsByPizza_IdAndIngredient_Id(pizzaId, request.ingredientId())
                && (entity.getIngredient() == null || !entity.getIngredient().getId().equals(request.ingredientId()))) {
            throw new ConflictException(
                    "Ingredient already allowed for this pizza",
                    ErrorCode.PIZZA_ALLOWED_INGREDIENT_ALREADY_EXISTS,
                    ErrorContext.of("pizzaId", pizzaId, "ingredientId", request.ingredientId())
            );
        }

        entity.setIngredient(ingredient);
        entity.setExtraPrice(parseExtraPrice(request.extraPrice()));

        PizzaAllowedIngredient saved = allowedIngredientRepository.save(entity);
        return mapper.toResponse(saved);
    }

    public void delete(Long pizzaId, Long id) {
        PizzaAllowedIngredient entity = allowedIngredientRepository.findByIdAndPizza_Id(id, pizzaId)
                .orElseThrow(() -> new NotFoundException(
                        "PizzaAllowedIngredient not found: " + id,
                        ErrorCode.PIZZA_ALLOWED_INGREDIENT_NOT_FOUND,
                        ErrorContext.of("pizzaId", pizzaId, "id", id)
                ));

        allowedIngredientRepository.delete(entity);
    }

    private BigDecimal parseExtraPrice(String value) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(
                    "extraPrice is required",
                    ErrorCode.INVALID_EXTRA_PRICE,
                    ErrorContext.of("field", "extraPrice")
            );
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new BadRequestException(
                    "Invalid extraPrice: " + value,
                    ErrorCode.INVALID_EXTRA_PRICE,
                    ErrorContext.of("field", "extraPrice", "value", value)
            );
        }
    }
}
