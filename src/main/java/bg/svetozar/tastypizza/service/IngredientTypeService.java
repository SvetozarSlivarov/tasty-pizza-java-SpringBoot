package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.exception.*;
import bg.svetozar.tastypizza.model.entity.IngredientType;
import bg.svetozar.tastypizza.repository.IngredientRepository;
import bg.svetozar.tastypizza.repository.IngredientTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class IngredientTypeService {

    private final IngredientTypeRepository ingredientTypeRepository;
    private final IngredientRepository ingredientRepository;

    public List<IngredientType> findAll() {
        return ingredientTypeRepository.findAll();
    }

    public IngredientType findById(Long id) {
        return ingredientTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        ErrorMessage.INGREDIENT_TYPE_NOT_FOUND + id,
                        ErrorCode.INGREDIENT_TYPE_NOT_FOUND,
                        ErrorContext.of("id", id)
                ));
    }

    public IngredientType create(String name) {
        String normalizedName = normalizeName(name);

        if (ingredientTypeRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new ConflictException(
                    ErrorMessage.INGREDIENT_TYPE_ALREADY_EXISTS + normalizedName,
                    ErrorCode.INGREDIENT_TYPE_ALREADY_EXISTS,
                    ErrorContext.of("name", normalizedName)
            );
        }

        IngredientType ingredientType = IngredientType.builder()
                .name(normalizedName)
                .build();

        return ingredientTypeRepository.save(ingredientType);
    }

    public IngredientType update(Long id, String name) {
        IngredientType ingredientType = findById(id);
        String normalizedName = normalizeName(name);

        boolean nameTaken = ingredientTypeRepository.existsByNameIgnoreCase(normalizedName);
        boolean sameAsCurrent = ingredientType.getName() != null
                && ingredientType.getName().equalsIgnoreCase(normalizedName);

        if (nameTaken && !sameAsCurrent) {
            throw new ConflictException(
                    ErrorMessage.INGREDIENT_TYPE_ALREADY_EXISTS + normalizedName,
                    ErrorCode.INGREDIENT_TYPE_ALREADY_EXISTS,
                    ErrorContext.of("name", normalizedName)
            );
        }

        ingredientType.setName(normalizedName);
        return ingredientTypeRepository.save(ingredientType);
    }

    public void deleteById(Long id) {
        long used = ingredientRepository.countByType_Id(id);
        if (used > 0) {
            throw new ConflictException(
                    ErrorMessage.INGREDIENT_TYPE_IN_USE + id,
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
                    ErrorMessage.INGREDIENT_TYPE_NOT_FOUND + normalizedName,
                    ErrorCode.INGREDIENT_TYPE_NOT_FOUND,
                    ErrorContext.of("name", normalizedName)
            );
        }
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException(
                    ErrorMessage.INGREDIENT_TYPE_NAME_CANNOT_BE_EMPTY,
                    ErrorCode.BAD_REQUEST
            );
        }
        return name.trim().toUpperCase();
    }
}
