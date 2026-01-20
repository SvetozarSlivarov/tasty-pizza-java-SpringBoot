package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.exception.*;
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
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static bg.svetozar.tastypizza.exception.ErrorMessage.ALLOWED_INGREDIENT_NOT_FOUND_WITH_ID;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INGREDIENT_ALREADY_ALLOWED;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INGREDIENT_NOT_FOUND_WITH_ID;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_EXTRA_PRICE;
import static bg.svetozar.tastypizza.exception.ErrorMessage.PIZZA_NOT_FOUND_WITH_ID;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_EXTRA_PRICE;

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
                        PIZZA_NOT_FOUND_WITH_ID + pizzaId,
                        ErrorCode.PIZZA_NOT_FOUND,
                        ErrorContext.of("pizzaId", pizzaId)
                ));

        List<PizzaAllowedIngredient> entities = allowedIngredientRepository.findAllByPizza_Id(pizzaId);
        return mapper.toResponseList(entities);
    }

    public PizzaAllowedIngredientDto addAllowedIngredient(Long pizzaId, PizzaAllowedIngredientRequest request) {
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

        if (allowedIngredientRepository.existsByPizza_IdAndIngredient_Id(pizzaId, request.ingredientId())) {
            throw new ConflictException(
                    INGREDIENT_ALREADY_ALLOWED,
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
        Long ingredientId = request.ingredientId();

        var ctxPizzaAllowed = ErrorContext.of("pizzaId", pizzaId, "id", id);
        var ctxIngredient = ErrorContext.of("ingredientId", ingredientId);
        var ctxConflict = ErrorContext.of("pizzaId", pizzaId, "ingredientId", ingredientId);

        PizzaAllowedIngredient entity = allowedIngredientRepository.findByIdAndPizza_Id(id, pizzaId)
                .orElseThrow(() -> new NotFoundException(
                        ALLOWED_INGREDIENT_NOT_FOUND_WITH_ID + id,
                        ErrorCode.PIZZA_ALLOWED_INGREDIENT_NOT_FOUND,
                        ctxPizzaAllowed
                ));

        Ingredient ingredient = ingredientRepository.findByIdAndDeletedFalse(ingredientId)
                .orElseThrow(() -> new NotFoundException(
                        INGREDIENT_NOT_FOUND_WITH_ID + ingredientId,
                        ErrorCode.INGREDIENT_NOT_FOUND,
                        ctxIngredient
                ));

        boolean ingredientAlreadyAllowed =
                allowedIngredientRepository.existsByPizza_IdAndIngredient_Id(pizzaId, ingredientId);

        Long currentIngredientId = entity.getIngredient() != null ? entity.getIngredient().getId() : null;
        boolean isDifferentIngredient = !Objects.equals(currentIngredientId, ingredientId);

        if (ingredientAlreadyAllowed && isDifferentIngredient) {
            throw new ConflictException(
                    INGREDIENT_ALREADY_ALLOWED,
                    ErrorCode.PIZZA_ALLOWED_INGREDIENT_ALREADY_EXISTS,
                    ctxConflict
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
                        ALLOWED_INGREDIENT_NOT_FOUND_WITH_ID + id,
                        ErrorCode.PIZZA_ALLOWED_INGREDIENT_NOT_FOUND,
                        ErrorContext.of("pizzaId", pizzaId, "id", id)
                ));

        allowedIngredientRepository.delete(entity);
    }

    private BigDecimal parseExtraPrice(String value) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(
                    REQUIRED_EXTRA_PRICE,
                    ErrorCode.INVALID_EXTRA_PRICE,
                    ErrorContext.of("field", "extraPrice")
            );
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new BadRequestException(
                    INVALID_EXTRA_PRICE + value,
                    ErrorCode.INVALID_EXTRA_PRICE,
                    ErrorContext.of("field", "extraPrice", "value", value)
            );
        }
    }
}
