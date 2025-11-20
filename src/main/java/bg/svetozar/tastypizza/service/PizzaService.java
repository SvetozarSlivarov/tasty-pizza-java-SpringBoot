package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.model.dto.pizza.PizzaDto;
import bg.svetozar.tastypizza.model.dto.pizza.PizzaRequest;
import bg.svetozar.tastypizza.model.dto.pizza.PizzaVariantRequest;
import bg.svetozar.tastypizza.model.entity.Pizza;
import bg.svetozar.tastypizza.model.entity.PizzaVariant;
import bg.svetozar.tastypizza.model.entity.Product;
import bg.svetozar.tastypizza.model.enums.ProductType;
import bg.svetozar.tastypizza.model.enums.PizzaSize;
import bg.svetozar.tastypizza.model.enums.DoughType;
import bg.svetozar.tastypizza.model.enums.SpicyLevel;
import bg.svetozar.tastypizza.model.mapper.PizzaMapper;
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



    public List<PizzaDto> getAll(boolean withVariants) {

        List<Pizza> pizzas = withVariants
                ? pizzaRepository.findAllFull()
                : pizzaRepository.findAllLight();

        return pizzas.stream()
                .map(p -> withVariants
                        ? PizzaMapper.toPizzaDto(p)
                        : PizzaMapper.toPizzaDtoWithoutVariants(p))
                .toList();
    }
    public PizzaDto getById(Long id) {
        Pizza pizza = pizzaRepository.findByIdFull(id)
                .orElseThrow(() -> new IllegalArgumentException("Pizza not found: " + id));

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

        existing.getVariants().clear();
        List<PizzaVariant> variants = mapVariantsFromRequest(request.variants(), existing);
        existing.getVariants().addAll(variants);
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
}
