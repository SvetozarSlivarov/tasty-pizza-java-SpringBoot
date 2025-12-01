package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.exception.IngredientNotFoundException;
import bg.svetozar.tastypizza.model.dto.pizza.PizzaDto;
import bg.svetozar.tastypizza.model.dto.pizza.PizzaRequest;
import bg.svetozar.tastypizza.model.dto.pizza.PizzaVariantRequest;
import bg.svetozar.tastypizza.model.dto.pizzaAllowedIngredient.PizzaAllowedIngredientRequest;
import bg.svetozar.tastypizza.model.dto.pizzaIngredient.PizzaIngredientRequest;
import bg.svetozar.tastypizza.model.entity.*;
import bg.svetozar.tastypizza.model.enums.ProductType;
import bg.svetozar.tastypizza.model.enums.PizzaSize;
import bg.svetozar.tastypizza.model.enums.DoughType;
import bg.svetozar.tastypizza.model.enums.SpicyLevel;
import bg.svetozar.tastypizza.model.mapper.PizzaMapper;
import bg.svetozar.tastypizza.repository.IngredientRepository;
import bg.svetozar.tastypizza.repository.PizzaAllowedIngredientRepository;
import bg.svetozar.tastypizza.repository.PizzaIngredientRepository;
import bg.svetozar.tastypizza.repository.PizzaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

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
            pizzas.forEach(pizza -> {
                List<PizzaIngredient> ingredients =
                        pizzaIngredientRepository.findAllByPizzaWithIngredient(pizza);

                List<PizzaAllowedIngredient> allowedIngredients =
                        pizzaAllowedIngredientRepository.findAllByPizzaWithIngredient(pizza);

                pizza.setIngredients(ingredients);
                pizza.setAllowedIngredients(allowedIngredients);
            });
        }

        return pizzas.stream()
                .map(p -> fullView
                        ? PizzaMapper.toPizzaDto(p)
                        : PizzaMapper.toPizzaDtoWithoutFullData(p))
                .toList();
    }

    @Transactional(readOnly = true)
    public PizzaDto getById(Long id) {
        Pizza pizza = pizzaRepository.findByIdFull(id)
                .orElseThrow(() -> new IllegalArgumentException("Pizza not found"));

        List<PizzaIngredient> ingredients =
                pizzaIngredientRepository.findAllByPizzaWithIngredient(pizza);

        List<PizzaAllowedIngredient> allowedIngredients =
                pizzaAllowedIngredientRepository.findAllByPizzaWithIngredient(pizza);

        pizza.setIngredients(ingredients);
        pizza.setAllowedIngredients(allowedIngredients);

        return PizzaMapper.toPizzaDto(pizza);
    }

    public PizzaDto create(PizzaRequest request) {
        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .basePrice(new BigDecimal(request.basePrice()))
                .imageUrl(request.imageUrl())
                .type(ProductType.PIZZA)
                .build();

        product = productService.createProduct(product);

        Pizza pizza = Pizza.builder()
                .product(product)
                .spicyLevel(request.spicyLevel() != null
                        ? SpicyLevel.valueOf(request.spicyLevel())
                        : null)
                .build();

        List<PizzaVariant> variants = mapVariantsFromRequest(request.variants(), pizza);
        pizza.setVariants(variants);


        List<PizzaIngredient> ingredients = mapIngredientsFromRequest(request.ingredients(), pizza);
        pizza.setIngredients(ingredients);


        List<PizzaAllowedIngredient> allowedIngredients =
                mapAllowedIngredientsFromRequest(request.allowedIngredients(), pizza);
        pizza.setAllowedIngredients(allowedIngredients);

        Pizza saved = pizzaRepository.save(pizza);
        return PizzaMapper.toPizzaDto(saved);
    }


    public PizzaDto update(Long id, PizzaRequest request) {
        Pizza existing = pizzaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pizza not found: " + id));

        Product product = existing.getProduct();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setBasePrice(new BigDecimal(request.basePrice()));
        product.setImageUrl(request.imageUrl());

        existing.setSpicyLevel(request.spicyLevel() != null
                ? SpicyLevel.valueOf(request.spicyLevel())
                : null);

        // variants
        existing.getVariants().clear();
        List<PizzaVariant> variants = mapVariantsFromRequest(request.variants(), existing);
        existing.getVariants().addAll(variants);

        // ingredients
        existing.getIngredients().clear();
        List<PizzaIngredient> ingredients = mapIngredientsFromRequest(request.ingredients(), existing);
        existing.getIngredients().addAll(ingredients);

        // allowedIngredients
        existing.getAllowedIngredients().clear();
        List<PizzaAllowedIngredient> allowedIngredients =
                mapAllowedIngredientsFromRequest(request.allowedIngredients(), existing);
        existing.getAllowedIngredients().addAll(allowedIngredients);

        return PizzaMapper.toPizzaDto(existing);
    }


    public void softDelete(Long id) {
        Pizza pizza = pizzaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pizza not found: " + id));

        productService.softDelete(pizza.getProduct().getId());
    }
    public void restoreDeletedPizza(Long id) {
        Pizza pizza = pizzaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pizza not found: " + id));

        productService.restoreDeletedProduct(pizza.getProduct().getId());
    }

    private List<PizzaVariant> mapVariantsFromRequest(List<PizzaVariantRequest> requests, Pizza pizza) {
        if (requests == null) {
            return List.of();
        }

        return requests.stream()
                .map(req -> PizzaVariant.builder()
                        .pizza(pizza)
                        .size(PizzaSize.valueOf(req.size()))
                        .dough(DoughType.valueOf(req.dough()))
                        .extraPrice(new BigDecimal(req.extraPrice()))
                        .build())
                .toList();
    }
    private List<PizzaIngredient> mapIngredientsFromRequest(List<PizzaIngredientRequest> requests, Pizza pizza) {
        if (requests == null) {
            return List.of();
        }

        return requests.stream()
                .map(req -> {
                    Ingredient ingredient = ingredientRepository.findByIdAndDeletedFalse(req.ingredientId())
                            .orElseThrow(() -> new IngredientNotFoundException(req.ingredientId()));

                    return PizzaIngredient.builder()
                            .pizza(pizza)
                            .ingredient(ingredient)
                            .removable(req.removable())
                            .build();
                })
                .toList();
    }

    private List<PizzaAllowedIngredient> mapAllowedIngredientsFromRequest(
            List<PizzaAllowedIngredientRequest> requests,
            Pizza pizza
    ) {
        if (requests == null) {
            return List.of();
        }

        return requests.stream()
                .map(req -> {
                    Ingredient ingredient = ingredientRepository.findByIdAndDeletedFalse(req.ingredientId())
                            .orElseThrow(() -> new IngredientNotFoundException(req.ingredientId()));

                    return PizzaAllowedIngredient.builder()
                            .pizza(pizza)
                            .ingredient(ingredient)
                            .extraPrice(new BigDecimal(req.extraPrice()))
                            .build();
                })
                .toList();
    }
}
