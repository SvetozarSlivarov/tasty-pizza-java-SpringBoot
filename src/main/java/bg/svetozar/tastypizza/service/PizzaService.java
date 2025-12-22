package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.exception.*;
import bg.svetozar.tastypizza.exception.ErrorCode;
import bg.svetozar.tastypizza.model.dto.pizza.PizzaDto;
import bg.svetozar.tastypizza.model.dto.pizza.PizzaRequest;
import bg.svetozar.tastypizza.model.dto.pizza.PizzaVariantRequest;
import bg.svetozar.tastypizza.model.dto.pizzaAllowedIngredient.PizzaAllowedIngredientRequest;
import bg.svetozar.tastypizza.model.dto.pizzaIngredient.PizzaIngredientRequest;
import bg.svetozar.tastypizza.model.entity.*;
import bg.svetozar.tastypizza.model.enums.*;
import bg.svetozar.tastypizza.model.mapper.PizzaMapper;
import bg.svetozar.tastypizza.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PizzaService {



    private final PizzaRepository pizzaRepository;
    private final ProductService productService;
    private final IngredientRepository ingredientRepository;
    private final PizzaIngredientRepository pizzaIngredientRepository;
    private final PizzaAllowedIngredientRepository pizzaAllowedIngredientRepository;


    @Transactional(readOnly = true)
    public List<PizzaDto> getAll(boolean fullView) {
        List<Pizza> pizzas = fullView
                ? pizzaRepository.findAllFull()
                : pizzaRepository.findAllLight();

        if (fullView) {
            hydrateIngredients(pizzas);
            return pizzas.stream().map(PizzaMapper::toPizzaDto).toList();
        }
        return pizzas.stream().map(PizzaMapper::toPizzaDtoWithoutFullData).toList();
    }

    @Transactional(readOnly = true)
    public List<PizzaDto> getAllDeleted(boolean fullView) {
        List<Pizza> pizzas = fullView
                ? pizzaRepository.findDeletedFull()
                : pizzaRepository.findDeletedLight();

        if (fullView) {
            hydrateIngredients(pizzas);
            return pizzas.stream().map(PizzaMapper::toPizzaDto).toList();
        }
        return pizzas.stream().map(PizzaMapper::toPizzaDtoWithoutFullData).toList();
    }

    @Transactional(readOnly = true)
    public PizzaDto getById(Long id) {
        Pizza pizza = pizzaRepository.findByIdFull(id)
                .orElseThrow(() -> new NotFoundException(
                        "Pizza not found",
                        ErrorCode.PIZZA_NOT_FOUND,
                        ErrorContext.of("pizzaId", id)
                ));

        hydrateIngredients(List.of(pizza));
        return PizzaMapper.toPizzaDto(pizza);
    }


    public PizzaDto create(PizzaRequest request) {
        BigDecimal basePrice = parseMoney(request.basePrice(), "basePrice");

        SpicyLevel spicy = null;
        if (request.spicyLevel() != null) {
            spicy = parseEnum(SpicyLevel.class, request.spicyLevel(), "spicyLevel");
        }

        validateVariantUniqueness(request.variants());
        validateIngredientUniquenessAndCrossLists(request.ingredients(), request.allowedIngredients());

        Product product = productService.createProduct(
                request.name(),
                request.description(),
                basePrice,
                ProductType.PIZZA,
                request.imageBase64()
        );

        Pizza pizza = Pizza.builder()
                .product(product)
                .spicyLevel(spicy)
                .build();

        pizza.setVariants(mapVariantsFromRequest(request.variants(), pizza));
        pizza.setIngredients(mapIngredientsFromRequest(request.ingredients(), pizza));
        pizza.setAllowedIngredients(mapAllowedIngredientsFromRequest(request.allowedIngredients(), pizza));

        Pizza saved = pizzaRepository.save(pizza);
        return PizzaMapper.toPizzaDto(saved);
    }


    public PizzaDto update(Long id, PizzaRequest request) {
        Pizza existing = pizzaRepository.findByIdFull(id)
                .orElseThrow(() -> new NotFoundException(
                        "Pizza not found",
                        ErrorCode.PIZZA_NOT_FOUND,
                        ErrorContext.of("pizzaId", id)
                ));

        BigDecimal basePrice = parseMoney(request.basePrice(), "basePrice");

        SpicyLevel spicy = null;
        if (request.spicyLevel() != null) {
            spicy = parseEnum(SpicyLevel.class, request.spicyLevel(), "spicyLevel");
        }

        validateVariantUniqueness(request.variants());
        validateIngredientUniquenessAndCrossLists(request.ingredients(), request.allowedIngredients());

        Product updatedProduct = productService.updateProduct(
                existing.getProduct().getId(),
                request.name(),
                request.description(),
                basePrice,
                ProductType.PIZZA,
                request.imageBase64()
        );

        existing.setProduct(updatedProduct);
        existing.setSpicyLevel(spicy);

        ensureCollections(existing);

        existing.getVariants().clear();
        existing.getVariants().addAll(mapVariantsFromRequest(request.variants(), existing));

        existing.getIngredients().clear();
        existing.getIngredients().addAll(mapIngredientsFromRequest(request.ingredients(), existing));

        existing.getAllowedIngredients().clear();
        existing.getAllowedIngredients().addAll(mapAllowedIngredientsFromRequest(request.allowedIngredients(), existing));

        return PizzaMapper.toPizzaDto(existing);
    }


    public void softDelete(Long id) {
        Pizza pizza = pizzaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Pizza not found",
                        ErrorCode.PIZZA_NOT_FOUND,
                        ErrorContext.of("pizzaId", id)
                ));

        if (pizza.getProduct() == null) {
            throw new NotFoundException(
                    "Product not found for pizza",
                    ErrorCode.PRODUCT_NOT_FOUND,
                    ErrorContext.of("pizzaId", id)
            );
        }

        if (pizza.getProduct().isDeleted()) {
            throw new ConflictException(
                    "Pizza is already deleted",
                    ErrorCode.PIZZA_ALREADY_DELETED,
                    ErrorContext.of("pizzaId", id)
            );
        }

        productService.softDelete(pizza.getProduct().getId());
    }

    public void restoreDeletedPizza(Long id) {
        Pizza pizza = pizzaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Pizza not found",
                        ErrorCode.PIZZA_NOT_FOUND,
                        ErrorContext.of("pizzaId", id)
                ));

        if (pizza.getProduct() == null || !Boolean.TRUE.equals(pizza.getProduct().isDeleted())) {
            throw new ConflictException(
                    "Pizza is not deleted",
                    ErrorCode.PIZZA_NOT_DELETED,
                    ErrorContext.of("pizzaId", id)
            );
        }

        productService.restoreDeletedProduct(pizza.getProduct().getId());
    }


    private void hydrateIngredients(List<Pizza> pizzas) {
        pizzas.forEach(pizza -> {
            List<PizzaIngredient> ingredients =
                    pizzaIngredientRepository.findAllByPizzaWithIngredient(pizza);

            List<PizzaAllowedIngredient> allowed =
                    pizzaAllowedIngredientRepository.findAllByPizzaWithIngredient(pizza);

            pizza.setIngredients(ingredients);
            pizza.setAllowedIngredients(allowed);
        });
    }

    private void ensureCollections(Pizza p) {
        if (p.getVariants() == null) p.setVariants(new ArrayList<>());
        if (p.getIngredients() == null) p.setIngredients(new ArrayList<>());
        if (p.getAllowedIngredients() == null) p.setAllowedIngredients(new ArrayList<>());
    }

    private BigDecimal parseMoney(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(
                    "Invalid price",
                    ErrorCode.INVALID_PRICE,
                    ErrorContext.of("field", field, "value", value)
            );
        }
        try {
            BigDecimal bd = new BigDecimal(value);
            if (bd.signum() < 0) {
                throw new BadRequestException(
                        "Price must be >= 0",
                        ErrorCode.INVALID_PRICE,
                        ErrorContext.of("field", field, "value", value)
                );
            }
            return bd;
        } catch (NumberFormatException ex) {
            throw new BadRequestException(
                    "Invalid price format",
                    ErrorCode.INVALID_PRICE,
                    ErrorContext.of("field", field, "value", value)
            );
        }
    }

    private <E extends Enum<E>> E parseEnum(Class<E> type, String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(
                    "Invalid enum value",
                    ErrorCode.INVALID_ENUM,
                    ErrorContext.of("field", field, "value", value)
            );
        }
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(
                    "Invalid enum value: " + value,
                    ErrorCode.INVALID_ENUM,
                    ErrorContext.of("field", field, "value", value)
            );
        }
    }

    private void validateVariantUniqueness(List<PizzaVariantRequest> variants) {
        if (variants == null || variants.isEmpty()) return;

        Set<String> seen = new HashSet<>();
        for (PizzaVariantRequest v : variants) {
            parseEnum(PizzaSize.class, v.size(), "variants.size");
            parseEnum(DoughType.class, v.dough(), "variants.dough");
            parseMoney(v.extraPrice(), "variants.extraPrice");

            String key = v.size() + "|" + v.dough();
            if (!seen.add(key)) {
                throw new ConflictException(
                        "Duplicate pizza variant (size+dough)",
                        ErrorCode.DUPLICATE_VARIANT,
                        ErrorContext.of("size", v.size(), "dough", v.dough())
                );
            }
        }
    }

    private void validateIngredientUniquenessAndCrossLists(
            List<PizzaIngredientRequest> base,
            List<PizzaAllowedIngredientRequest> allowed
    ) {
        Set<Long> baseIds = new HashSet<>();
        if (base != null) {
            for (PizzaIngredientRequest r : base) {
                Long id = r.ingredientId();
                if (id == null) continue;
                if (!baseIds.add(id)) {
                    throw new ConflictException(
                            "Duplicate ingredient in base ingredients list",
                            ErrorCode.DUPLICATE_INGREDIENT,
                            ErrorContext.of("ingredientId", id)
                    );
                }
            }
        }

        Set<Long> allowedIds = new HashSet<>();
        if (allowed != null) {
            for (PizzaAllowedIngredientRequest r : allowed) {
                Long id = r.ingredientId();
                if (id == null) continue;

                parseMoney(r.extraPrice(), "allowedIngredients.extraPrice");

                if (!allowedIds.add(id)) {
                    throw new ConflictException(
                            "Duplicate ingredient in allowed ingredients list",
                            ErrorCode.DUPLICATE_ALLOWED_INGREDIENT,
                            ErrorContext.of("ingredientId", id)
                    );
                }
            }
        }

        Set<Long> intersection = baseIds.stream()
                .filter(allowedIds::contains)
                .collect(Collectors.toSet());

        if (!intersection.isEmpty()) {
            Long first = intersection.iterator().next();
            throw new ConflictException(
                    "Ingredient cannot be both base and allowed extra",
                    ErrorCode.INGREDIENT_IN_BASE_AND_ALLOWED,
                    ErrorContext.of("ingredientId", first, "count", intersection.size())
            );
        }
    }

    private List<PizzaVariant> mapVariantsFromRequest(List<PizzaVariantRequest> requests, Pizza pizza) {
        if (requests == null) return List.of();

        return requests.stream()
                .map(req -> PizzaVariant.builder()
                        .pizza(pizza)
                        .size(parseEnum(PizzaSize.class, req.size(), "variants.size"))
                        .dough(parseEnum(DoughType.class, req.dough(), "variants.dough"))
                        .extraPrice(parseMoney(req.extraPrice(), "variants.extraPrice"))
                        .build())
                .toList();
    }

    private List<PizzaIngredient> mapIngredientsFromRequest(List<PizzaIngredientRequest> requests, Pizza pizza) {
        if (requests == null) return List.of();

        return requests.stream()
                .map(req -> {
                    Ingredient ingredient = ingredientRepository
                            .findByIdAndDeletedFalse(req.ingredientId())
                            .orElseThrow(() -> new NotFoundException(
                                    "Ingredient not found",
                                    ErrorCode.INGREDIENT_NOT_FOUND,
                                    ErrorContext.of("ingredientId", req.ingredientId())
                            ));

                    return PizzaIngredient.builder()
                            .pizza(pizza)
                            .ingredient(ingredient)
                            .removable(Boolean.TRUE.equals(req.removable()))
                            .build();
                })
                .toList();
    }

    private List<PizzaAllowedIngredient> mapAllowedIngredientsFromRequest(
            List<PizzaAllowedIngredientRequest> requests,
            Pizza pizza
    ) {
        if (requests == null) return List.of();

        return requests.stream()
                .map(req -> {
                    Ingredient ingredient = ingredientRepository
                            .findByIdAndDeletedFalse(req.ingredientId())
                            .orElseThrow(() -> new NotFoundException(
                                    "Ingredient not found",
                                    ErrorCode.INGREDIENT_NOT_FOUND,
                                    ErrorContext.of("ingredientId", req.ingredientId())
                            ));

                    return PizzaAllowedIngredient.builder()
                            .pizza(pizza)
                            .ingredient(ingredient)
                            .extraPrice(parseMoney(req.extraPrice(), "allowedIngredients.extraPrice"))
                            .build();
                })
                .toList();
    }
}
