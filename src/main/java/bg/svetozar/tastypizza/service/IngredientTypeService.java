package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.exception.*;
import bg.svetozar.tastypizza.model.dto.ingredientType.IngredientTypeRequest;
import bg.svetozar.tastypizza.model.entity.IngredientType;
import bg.svetozar.tastypizza.repository.IngredientRepository;
import bg.svetozar.tastypizza.repository.IngredientTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

import static bg.svetozar.tastypizza.exception.ErrorMessage.INGREDIENT_TYPE_ALREADY_EXISTS;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INGREDIENT_TYPE_IN_USE;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INGREDIENT_TYPE_NAME_CANNOT_BE_EMPTY;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INGREDIENT_TYPE_NOT_FOUND;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_TYPE_NAME_BETWEEN_2_50_CHARS;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_NAME;

@Service
@RequiredArgsConstructor
@Transactional
public class IngredientTypeService {

    private final IngredientTypeRepository ingredientTypeRepository;
    private final IngredientRepository ingredientRepository;
    private final LocalizedTextService localizedTextService;

    public List<IngredientType> findAll() {
        return ingredientTypeRepository.findAll();
    }

    public IngredientType findById(Long id) {
        return ingredientTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        INGREDIENT_TYPE_NOT_FOUND + id,
                        ErrorCode.INGREDIENT_TYPE_NOT_FOUND,
                        ErrorContext.of("id", id)
                ));
    }

    public IngredientType create(String name) {
        return create(name, null, null);
    }

    public IngredientType create(IngredientTypeRequest request) {
        String englishName = localizedTextService.resolveEnglishField(
                request.translations(), request.fields(), "name", request.name()
        );
        return create(englishName, request.translations(), request.fields());
    }

    private IngredientType create(String name, java.util.Map<String, java.util.Map<String, String>> translations, java.util.Map<String, java.util.Map<String, String>> fields) {
        String normalizedName = normalizeName(name);

        if (ingredientTypeRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new ConflictException(
                    INGREDIENT_TYPE_ALREADY_EXISTS + normalizedName,
                    ErrorCode.INGREDIENT_TYPE_ALREADY_EXISTS,
                    ErrorContext.of("name", normalizedName)
            );
        }

        IngredientType ingredientType = IngredientType.builder()
                .name(normalizedName)
                .build();

        IngredientType saved = ingredientTypeRepository.save(ingredientType);
        localizedTextService.saveTranslations(
                "INGREDIENT_TYPE",
                saved.getId(),
                translations,
                fields,
                java.util.Map.of("name", normalizedName)
        );
        return saved;
    }

    public IngredientType update(Long id, String name) {
        return update(id, name, null, null);
    }

    public IngredientType update(Long id, IngredientTypeRequest request) {
        String englishName = localizedTextService.resolveEnglishField(
                request.translations(), request.fields(), "name", request.name()
        );
        return update(id, englishName, request.translations(), request.fields());
    }

    private IngredientType update(Long id, String name, java.util.Map<String, java.util.Map<String, String>> translations, java.util.Map<String, java.util.Map<String, String>> fields) {
        IngredientType ingredientType = findById(id);
        String normalizedName = normalizeName(name);

        boolean nameTaken = ingredientTypeRepository.existsByNameIgnoreCase(normalizedName);
        boolean sameAsCurrent = ingredientType.getName() != null
                && ingredientType.getName().equalsIgnoreCase(normalizedName);

        if (nameTaken && !sameAsCurrent) {
            throw new ConflictException(
                    INGREDIENT_TYPE_ALREADY_EXISTS + normalizedName,
                    ErrorCode.INGREDIENT_TYPE_ALREADY_EXISTS,
                    ErrorContext.of("name", normalizedName)
            );
        }

        ingredientType.setName(normalizedName);
        IngredientType saved = ingredientTypeRepository.save(ingredientType);
        localizedTextService.saveTranslations(
                "INGREDIENT_TYPE",
                saved.getId(),
                translations,
                fields,
                java.util.Map.of("name", normalizedName)
        );
        return saved;
    }

    public void deleteById(Long id) {
        long used = ingredientRepository.countByType_Id(id);
        if (used > 0) {
            throw new ConflictException(
                    INGREDIENT_TYPE_IN_USE + id,
                    ErrorCode.TYPE_IN_USE,
                    ErrorContext.of("id", id, "usedCount", used)
            );
        }

        IngredientType ingredientType = findById(id);
        ingredientTypeRepository.delete(ingredientType);
    }

    public void deleteByName(String name) {
        String normalizedName = normalizeName(name);

        int deleted = ingredientTypeRepository.deleteAllByNameIgnoreCase(normalizedName);
        if (deleted == 0) {
            throw new NotFoundException(
                    INGREDIENT_TYPE_NOT_FOUND + normalizedName,
                    ErrorCode.INGREDIENT_TYPE_NOT_FOUND,
                    ErrorContext.of("name", normalizedName)
            );
        }
    }

    private String normalizeName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new BadRequestException(
                    REQUIRED_NAME,
                    ErrorCode.BAD_REQUEST,
                    ErrorContext.of("field", "translations.name.en")
            );
        }
        String normalized = name.trim().toUpperCase();
        if (normalized.length() < 2 || normalized.length() > 50) {
            throw new BadRequestException(
                    INVALID_TYPE_NAME_BETWEEN_2_50_CHARS,
                    ErrorCode.BAD_REQUEST,
                    ErrorContext.of("field", "translations.name.en")
            );
        }
        return normalized;
    }
}
