package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.exception.ErrorCode;
import bg.svetozar.tastypizza.exception.BadRequestException;
import bg.svetozar.tastypizza.exception.ErrorContext;
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

import static bg.svetozar.tastypizza.exception.ErrorMessage.DRINK_NOT_FOUND;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_DRINK_DESCRIPTION_MAX_500_CHARS;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_DRINK_NAME_BETWEEN_2_80_CHARS;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_NAME;

@Service
@RequiredArgsConstructor
@Transactional
public class DrinkService {

    private final DrinkRepository drinkRepository;
    private final ProductService productService;
    private final LocalizedTextService localizedTextService;

    public List<DrinkDto> getAll() {
        return getAll(null);
    }

    public List<DrinkDto> getAll(String lang) {
        return drinkRepository.findAllLight().stream()
                .map(drink -> toDrinkDto(drink, lang))
                .toList();
    }

    public List<DrinkDto> getAllDeleted() {
        return drinkRepository.findDeletedLight().stream()
                .map(DrinkMapper::toDrinkDto)
                .toList();
    }

    public DrinkDto getById(Long id) {
        return getById(id, null);
    }

    public DrinkDto getById(Long id, String lang) {
        Drink drink = drinkRepository.findByIdLight(id)
                .orElseThrow(() -> new NotFoundException(
                        DRINK_NOT_FOUND + id,
                        ErrorCode.DRINK_NOT_FOUND
                ));

        return toDrinkDto(drink, lang);
    }

    public DrinkDto create(DrinkRequest request) {
        String englishName = localizedTextService.resolveEnglishField(
                request.translations(), request.fields(), "name", request.name()
        );
        String englishDescription = localizedTextService.resolveEnglishField(
                request.translations(), request.fields(), "description", request.description()
        );
        validateEnglishProductText(englishName, englishDescription);

        Product product = productService.createProduct(
                englishName,
                englishDescription,
                new BigDecimal(request.basePrice()),
                ProductType.DRINK,
                request.imageBase64()
        );

        localizedTextService.saveTranslations(
                "PRODUCT",
                product.getId(),
                request.translations(),
                request.fields(),
                java.util.Map.of("name", englishName, "description", englishDescription == null ? "" : englishDescription)
        );

        Drink drink = Drink.builder()
                .product(product)
                .build();

        drinkRepository.save(drink);

        return DrinkMapper.toDrinkDto(drink);
    }

    private DrinkDto toDrinkDto(Drink drink, String lang) {
        Product product = drink.getProduct();
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
        return DrinkMapper.toDrinkDto(drink, name, description);
    }

    private void validateEnglishProductText(String englishName, String englishDescription) {
        if (!org.springframework.util.StringUtils.hasText(englishName)) {
            throw new BadRequestException(REQUIRED_NAME, ErrorCode.BAD_REQUEST, ErrorContext.of("field", "translations.name.en"));
        }
        if (englishName.length() < 2 || englishName.length() > 80) {
            throw new BadRequestException(INVALID_DRINK_NAME_BETWEEN_2_80_CHARS, ErrorCode.BAD_REQUEST, ErrorContext.of("field", "translations.name.en"));
        }
        if (englishDescription != null && englishDescription.length() > 500) {
            throw new BadRequestException(INVALID_DRINK_DESCRIPTION_MAX_500_CHARS, ErrorCode.BAD_REQUEST, ErrorContext.of("field", "translations.description.en"));
        }
    }

    public DrinkDto update(Long id, DrinkRequest request) {
        Drink drink = drinkRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        DRINK_NOT_FOUND + id,
                        ErrorCode.DRINK_NOT_FOUND
                ));
        String englishName = localizedTextService.resolveEnglishField(
                request.translations(), request.fields(), "name", request.name()
        );
        String englishDescription = localizedTextService.resolveEnglishField(
                request.translations(), request.fields(), "description", request.description()
        );
        validateEnglishProductText(englishName, englishDescription);

        Product updatedProduct = productService.updateProduct(
                drink.getProduct().getId(),
                englishName,
                englishDescription,
                new BigDecimal(request.basePrice()),
                ProductType.DRINK,
                request.imageBase64()
        );

        localizedTextService.saveTranslations(
                "PRODUCT",
                updatedProduct.getId(),
                request.translations(),
                request.fields(),
                java.util.Map.of("name", englishName, "description", englishDescription == null ? "" : englishDescription)
        );

        drink.setProduct(updatedProduct);
        drinkRepository.save(drink);

        return DrinkMapper.toDrinkDto(drink);
    }

    public void softDelete(Long id) {
        Drink drink = drinkRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        DRINK_NOT_FOUND + id,
                        ErrorCode.DRINK_NOT_FOUND
                ));

        productService.softDelete(drink.getProduct().getId());
    }

    public void restoreDeletedDrink(Long id) {
        Drink drink = drinkRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        DRINK_NOT_FOUND + id,
                        ErrorCode.DRINK_NOT_FOUND
                ));

        productService.restoreDeletedProduct(drink.getProduct().getId());
    }
}
