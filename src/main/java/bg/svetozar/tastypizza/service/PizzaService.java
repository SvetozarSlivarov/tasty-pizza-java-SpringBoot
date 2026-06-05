package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.exception.*;
import bg.svetozar.tastypizza.model.dto.pizza.PizzaDto;
import bg.svetozar.tastypizza.model.dto.pizza.PizzaRequest;
import bg.svetozar.tastypizza.model.dto.pizza.PizzaVariantRequest;
import bg.svetozar.tastypizza.model.dto.pizzaAllowedIngredient.PizzaAllowedIngredientDto;
import bg.svetozar.tastypizza.model.dto.pizzaAllowedIngredient.PizzaAllowedIngredientRequest;
import bg.svetozar.tastypizza.model.dto.pizzaIngredient.PizzaIngredientDto;
import bg.svetozar.tastypizza.model.dto.pizzaIngredient.PizzaIngredientRequest;
import bg.svetozar.tastypizza.model.entity.*;
import bg.svetozar.tastypizza.model.enums.*;
import bg.svetozar.tastypizza.model.mapper.PizzaMapper;
import bg.svetozar.tastypizza.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static bg.svetozar.tastypizza.exception.ErrorMessage.DUPLICATE_ALLOWED_INGREDIENT;
import static bg.svetozar.tastypizza.exception.ErrorMessage.DUPLICATE_PIZZA_INGREDIENT;
import static bg.svetozar.tastypizza.exception.ErrorMessage.DUPLICATE_PIZZA_VARIANT;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INGREDIENT_CANNOT_BOTH_ALLOWED_AND_BASE;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INGREDIENT_NOT_FOUND;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_ENUM_VALUE;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_ENUM_VALUE_WITH_VALUE;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_PIZZA_DESCRIPTION_MAX_1000_CHARS;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_PIZZA_NAME_BETWEEN_5_100_CHARS;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_PRICE;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_PRICE_FORMAT;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_PRICE_MUST_BE_POSITIVE;
import static bg.svetozar.tastypizza.exception.ErrorMessage.PIZZA_ALREADY_DELETED;
import static bg.svetozar.tastypizza.exception.ErrorMessage.PIZZA_NOT_DELETED;
import static bg.svetozar.tastypizza.exception.ErrorMessage.PIZZA_NOT_FOUND;
import static bg.svetozar.tastypizza.exception.ErrorMessage.PRODUCT_NOT_FOUND_FOR_PIZZA;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_NAME;

@Service
@RequiredArgsConstructor
@Transactional
public class PizzaService {

    private final PizzaRepository pizzaRepository;
    private final ProductService productService;
    private final LocalizedTextService localizedTextService;
    private final IngredientRepository ingredientRepository;
    private final PizzaIngredientRepository pizzaIngredientRepository;
    private final PizzaAllowedIngredientRepository pizzaAllowedIngredientRepository;

    @Transactional(readOnly = true)
    public List<PizzaDto> getAll(boolean fullView) {
        return getAll(fullView, null);
    }

    @Transactional(readOnly = true)
    public List<PizzaDto> getAll(boolean fullView, String lang) {
        List<Pizza> pizzas = fullView
                ? pizzaRepository.findAllFull()
                : pizzaRepository.findAllLight();

        if (fullView) {
            hydrateIngredients(pizzas);
            return pizzas.stream().map(pizza -> toPizzaDto(pizza, true, lang)).toList();
        }
        return pizzas.stream().map(pizza -> toPizzaDto(pizza, false, lang)).toList();
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
        return getById(id, null);
    }

    @Transactional(readOnly = true)
    public PizzaDto getById(Long id, String lang) {
        Pizza pizza = pizzaRepository.findByIdFull(id)
                .orElseThrow(() -> new NotFoundException(
                        PIZZA_NOT_FOUND,
                        ErrorCode.PIZZA_NOT_FOUND,
                        ErrorContext.of("pizzaId", id)
                ));

        hydrateIngredients(List.of(pizza));
        return toPizzaDto(pizza, true, lang);
    }

    public PizzaDto create(PizzaRequest request) {
        BigDecimal basePrice = parseMoney(request.basePrice(), "basePrice");
        String englishName = localizedTextService.resolveEnglishField(
                request.translations(), request.fields(), "name", request.name()
        );
        String englishDescription = localizedTextService.resolveEnglishField(
                request.translations(), request.fields(), "description", request.description()
        );

        validateEnglishProductText(englishName, englishDescription);

        SpicyLevel spicy = null;
        if (request.spicyLevel() != null) {
            spicy = parseEnum(SpicyLevel.class, request.spicyLevel(), "spicyLevel");
        }

        validateVariantUniqueness(request.variants());
        validateIngredientUniquenessAndCrossLists(request.ingredients(), request.allowedIngredients());

        Product product = productService.createProduct(
                englishName,
                englishDescription,
                basePrice,
                ProductType.PIZZA,
                request.imageBase64()
        );

        localizedTextService.saveTranslations(
                "PRODUCT",
                product.getId(),
                request.translations(),
                request.fields(),
                Map.of("name", englishName, "description", englishDescription == null ? "" : englishDescription)
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
                        PIZZA_NOT_FOUND,
                        ErrorCode.PIZZA_NOT_FOUND,
                        ErrorContext.of("pizzaId", id)
                ));

        BigDecimal basePrice = parseMoney(request.basePrice(), "basePrice");
        String englishName = localizedTextService.resolveEnglishField(
                request.translations(), request.fields(), "name", request.name()
        );
        String englishDescription = localizedTextService.resolveEnglishField(
                request.translations(), request.fields(), "description", request.description()
        );

        validateEnglishProductText(englishName, englishDescription);

        SpicyLevel spicy = null;
        if (request.spicyLevel() != null) {
            spicy = parseEnum(SpicyLevel.class, request.spicyLevel(), "spicyLevel");
        }

        validateVariantUniqueness(request.variants());
        validateIngredientUniquenessAndCrossLists(request.ingredients(), request.allowedIngredients());

        Product updatedProduct = productService.updateProduct(
                existing.getProduct().getId(),
                englishName,
                englishDescription,
                basePrice,
                ProductType.PIZZA,
                request.imageBase64()
        );

        localizedTextService.saveTranslations(
                "PRODUCT",
                updatedProduct.getId(),
                request.translations(),
                request.fields(),
                Map.of("name", englishName, "description", englishDescription == null ? "" : englishDescription)
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
                        PIZZA_NOT_FOUND,
                        ErrorCode.PIZZA_NOT_FOUND,
                        ErrorContext.of("pizzaId", id)
                ));

        if (pizza.getProduct() == null) {
            throw new NotFoundException(
                    PRODUCT_NOT_FOUND_FOR_PIZZA,
                    ErrorCode.PRODUCT_NOT_FOUND,
                    ErrorContext.of("pizzaId", id)
            );
        }

        if (pizza.getProduct().isDeleted()) {
            throw new ConflictException(
                    PIZZA_ALREADY_DELETED,
                    ErrorCode.PIZZA_ALREADY_DELETED,
                    ErrorContext.of("pizzaId", id)
            );
        }

        productService.softDelete(pizza.getProduct().getId());
    }

    public void restoreDeletedPizza(Long id) {
        Pizza pizza = pizzaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        PIZZA_NOT_FOUND,
                        ErrorCode.PIZZA_NOT_FOUND,
                        ErrorContext.of("pizzaId", id)
                ));

        if (pizza.getProduct() == null || !Boolean.TRUE.equals(pizza.getProduct().isDeleted())) {
            throw new ConflictException(
                    PIZZA_NOT_DELETED,
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

    private PizzaDto toPizzaDto(Pizza pizza, boolean includeDetails, String lang) {
        Product product = pizza.getProduct();
        String name = localizedTextService.getTranslationOrDefault(
                "PRODUCT",
                product.getId(),
                "name",
                lang,
                product.getName()
        );
        String description = localizedTextService.getTranslationOrDefault(
                "PRODUCT",
                product.getId(),
                "description",
                lang,
                product.getDescription()
        );

        PizzaDto dto = includeDetails
                ? PizzaMapper.toPizzaDto(pizza, name, description)
                : PizzaMapper.toPizzaDtoWithoutFullData(pizza, name, description);
        return withLocalizedIngredientNames(dto, lang);
    }

    private PizzaDto withLocalizedIngredientNames(PizzaDto dto, String lang) {
        List<PizzaIngredientDto> ingredients = dto.ingredients() == null
                ? List.of()
                : dto.ingredients().stream()
                .map(item -> new PizzaIngredientDto(
                        item.id(),
                        item.pizzaId(),
                        item.ingredientId(),
                        localizedTextService.getTranslationOrDefault("INGREDIENT", item.ingredientId(), "name", lang, item.ingredientName()),
                        item.removable()
                ))
                .toList();

        List<PizzaAllowedIngredientDto> allowedIngredients = dto.allowedIngredients() == null
                ? List.of()
                : dto.allowedIngredients().stream()
                .map(item -> new PizzaAllowedIngredientDto(
                        item.id(),
                        item.pizzaId(),
                        item.ingredientId(),
                        localizedTextService.getTranslationOrDefault("INGREDIENT", item.ingredientId(), "name", lang, item.ingredientName()),
                        item.extraPrice()
                ))
                .toList();

        return new PizzaDto(
                dto.id(),
                dto.name(),
                dto.description(),
                dto.basePrice(),
                dto.type(),
                dto.deleted(),
                dto.deletedAt(),
                dto.spicyLevel(),
                dto.imageUrl(),
                dto.variants(),
                ingredients,
                allowedIngredients
        );
    }

    private String normalizeLanguage(String lang) {
        if (!StringUtils.hasText(lang)) {
            return null;
        }
        return lang.trim().toLowerCase(Locale.ROOT);
    }

    private void validateEnglishProductText(String englishName, String englishDescription) {
        if (!StringUtils.hasText(englishName)) {
            throw new BadRequestException(
                    REQUIRED_NAME,
                    ErrorCode.BAD_REQUEST,
                    ErrorContext.of("field", "translations.name.en")
            );
        }
        if (englishName.length() < 5 || englishName.length() > 100) {
            throw new BadRequestException(
                    INVALID_PIZZA_NAME_BETWEEN_5_100_CHARS,
                    ErrorCode.BAD_REQUEST,
                    ErrorContext.of("field", "translations.name.en")
            );
        }
        if (englishDescription != null && englishDescription.length() > 1000) {
            throw new BadRequestException(
                    INVALID_PIZZA_DESCRIPTION_MAX_1000_CHARS,
                    ErrorCode.BAD_REQUEST,
                    ErrorContext.of("field", "translations.description.en")
            );
        }
    }

    private void ensureCollections(Pizza p) {
        if (p.getVariants() == null) p.setVariants(new ArrayList<>());
        if (p.getIngredients() == null) p.setIngredients(new ArrayList<>());
        if (p.getAllowedIngredients() == null) p.setAllowedIngredients(new ArrayList<>());
    }

    private BigDecimal parseMoney(String value, String field) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(
                    INVALID_PRICE,
                    ErrorCode.INVALID_PRICE,
                    ErrorContext.of("field", field, "value", value)
            );
        }
        try {
            BigDecimal bd = new BigDecimal(value);
            if (bd.signum() < 0) {
                throw new BadRequestException(
                        INVALID_PRICE_MUST_BE_POSITIVE,
                        ErrorCode.INVALID_PRICE,
                        ErrorContext.of("field", field, "value", value)
                );
            }
            return bd;
        } catch (NumberFormatException ex) {
            throw new BadRequestException(
                    INVALID_PRICE_FORMAT,
                    ErrorCode.INVALID_PRICE,
                    ErrorContext.of("field", field, "value", value)
            );
        }
    }

    private <E extends Enum<E>> E parseEnum(Class<E> type, String value, String field) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(
                    INVALID_ENUM_VALUE,
                    ErrorCode.INVALID_ENUM,
                    ErrorContext.of("field", field, "value", value)
            );
        }
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(
                    INVALID_ENUM_VALUE_WITH_VALUE + value,
                    ErrorCode.INVALID_ENUM,
                    ErrorContext.of("field", field, "value", value)
            );
        }
    }

    private void validateVariantUniqueness(List<PizzaVariantRequest> variants) {
        if (CollectionUtils.isEmpty(variants)) return;

        Set<String> seen = new HashSet<>();
        for (PizzaVariantRequest variant : variants) {
            parseEnum(PizzaSize.class, variant.size(), "variants.size");
            parseEnum(DoughType.class, variant.dough(), "variants.dough");
            parseMoney(variant.extraPrice(), "variants.extraPrice");

            String key = variant.size() + "|" + variant.dough();
            if (!seen.add(key)) {
                throw new ConflictException(
                        DUPLICATE_PIZZA_VARIANT,
                        ErrorCode.DUPLICATE_VARIANT,
                        ErrorContext.of("size", variant.size(), "dough", variant.dough())
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
            for (PizzaIngredientRequest request : base) {
                Long id = request.ingredientId();
                if (id == null) continue;
                if (!baseIds.add(id)) {
                    throw new ConflictException(
                            DUPLICATE_PIZZA_INGREDIENT,
                            ErrorCode.DUPLICATE_INGREDIENT,
                            ErrorContext.of("ingredientId", id)
                    );
                }
            }
        }

        Set<Long> allowedIds = new HashSet<>();
        if (allowed != null) {
            for (PizzaAllowedIngredientRequest request : allowed) {
                Long id = request.ingredientId();
                if (id == null) continue;

                parseMoney(request.extraPrice(), "allowedIngredients.extraPrice");

                if (!allowedIds.add(id)) {
                    throw new ConflictException(
                            DUPLICATE_ALLOWED_INGREDIENT,
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
                    INGREDIENT_CANNOT_BOTH_ALLOWED_AND_BASE,
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
                                    INGREDIENT_NOT_FOUND,
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
                                    INGREDIENT_NOT_FOUND,
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
