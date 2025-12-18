package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.exception.*;
import bg.svetozar.tastypizza.model.entity.IngredientType;
import bg.svetozar.tastypizza.repository.IngredientRepository;
import bg.svetozar.tastypizza.repository.IngredientTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
                .orElseThrow(() -> new IngredientTypeNotFoundException(id));
    }

    public IngredientType create(String name) {
        String normalizedName = normalizeName(name);

        if (ingredientTypeRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new IngredientTypeAlreadyExistsException(normalizedName);
        }

        IngredientType ingredientType = IngredientType.builder()
                .name(normalizedName)
                .build();

        return ingredientTypeRepository.save(ingredientType);
    }

    public IngredientType update(Long id, String name) {
        IngredientType ingredientType = findById(id);

        String normalizedName = normalizeName(name);

        if (ingredientTypeRepository.existsByNameIgnoreCase(normalizedName)
                && !ingredientType.getName().equalsIgnoreCase(normalizedName)) {
            throw new IngredientTypeAlreadyExistsException(normalizedName);
        }

        ingredientType.setName(normalizedName);
        return ingredientTypeRepository.save(ingredientType);
    }

    public void deleteById(Long id) {

        long used = ingredientRepository.countByType_Id(id);
        if (used > 0) {
            throw new IngredientTypeInUseException(id);
        }

        IngredientType ingredientType = findById(id);
        ingredientTypeRepository.delete(ingredientType);
    }

    public void deleteByName(String name) {
        String normalizedName = normalizeName(name);

        int deleted = ingredientTypeRepository.deleteAllByNameIgnoreCase(normalizedName);
        if (deleted == 0) {
            throw new IngredientTypeNotFoundException(normalizedName);
        }
    }

    private String normalizeName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Ingredient type name cannot be null");
        }
        return name.trim().toUpperCase();
    }
}
