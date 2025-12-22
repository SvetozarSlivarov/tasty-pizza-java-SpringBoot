package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.exception.ErrorCode;
import bg.svetozar.tastypizza.exception.NotFoundException;
import bg.svetozar.tastypizza.model.dto.drink.DrinkDto;
import bg.svetozar.tastypizza.model.dto.drink.DrinkRequest;
import bg.svetozar.tastypizza.model.entity.Drink;
import bg.svetozar.tastypizza.model.entity.Product;
import bg.svetozar.tastypizza.model.enums.ProductType;
import bg.svetozar.tastypizza.model.mapper.DrinkMapper;
import bg.svetozar.tastypizza.repository.DrinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DrinkService {

    private final DrinkRepository drinkRepository;
    private final ProductService productService;

    public List<DrinkDto> getAll() {
        return drinkRepository.findAllLight().stream()
                .map(DrinkMapper::toDrinkDto)
                .toList();
    }

    public List<DrinkDto> getAllDeleted() {
        return drinkRepository.findDeletedLight().stream()
                .map(DrinkMapper::toDrinkDto)
                .toList();
    }

    public DrinkDto getById(Long id) {
        Drink drink = drinkRepository.findByIdLight(id)
                .orElseThrow(() -> new NotFoundException(
                        "Drink not found: " + id,
                        ErrorCode.DRINK_NOT_FOUND
                ));

        return DrinkMapper.toDrinkDto(drink);
    }

    // ---------- CREATE ----------
    public DrinkDto create(DrinkRequest request) {
        Product product = productService.createProduct(
                request.name(),
                request.description(),
                new BigDecimal(request.basePrice()),
                ProductType.DRINK,
                request.imageBase64()
        );

        Drink drink = Drink.builder()
                .product(product)
                .build();

        drinkRepository.save(drink);

        return DrinkMapper.toDrinkDto(drink);
    }

    // ---------- UPDATE ----------
    public DrinkDto update(Long id, DrinkRequest request) {
        Drink drink = drinkRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Drink not found: " + id,
                        ErrorCode.DRINK_NOT_FOUND
                ));

        Product updatedProduct = productService.updateProduct(
                drink.getProduct().getId(),
                request.name(),
                request.description(),
                new BigDecimal(request.basePrice()),
                ProductType.DRINK,
                request.imageBase64()
        );

        drink.setProduct(updatedProduct);
        drinkRepository.save(drink);

        return DrinkMapper.toDrinkDto(drink);
    }

    public void softDelete(Long id) {
        Drink drink = drinkRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Drink not found: " + id,
                        ErrorCode.DRINK_NOT_FOUND
                ));

        productService.softDelete(drink.getProduct().getId());
    }

    public void restoreDeletedDrink(Long id) {
        Drink drink = drinkRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Drink not found: " + id,
                        ErrorCode.DRINK_NOT_FOUND
                ));

        productService.restoreDeletedProduct(drink.getProduct().getId());
    }
}
