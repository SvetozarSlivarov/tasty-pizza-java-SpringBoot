package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.model.entity.IngredientType;
import bg.svetozar.tastypizza.repository.IngredientTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class IngredientTypeService {

    private final IngredientTypeRepository ingredientTypeRepository;

    public List<IngredientType> findAll() {
        return ingredientTypeRepository.findAll();
    }

    public IngredientType findById(Long id) {
        return ingredientTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Ingredient type with id: " + id + " not found"
                ));
    }

    public IngredientType create(String name) {

        String normalizedName = normalizeName(name);

        if (ingredientTypeRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new IllegalArgumentException(
                    "Ingredient type [" + normalizedName + "] already exists"
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

        if (ingredientTypeRepository.existsByNameIgnoreCase(normalizedName)
                && !ingredientType.getName().equalsIgnoreCase(normalizedName)) {

            throw new IllegalArgumentException(
                    "Ingredient type [" + normalizedName + "] already exists"
            );
        }

        ingredientType.setName(normalizedName);

        return ingredientTypeRepository.save(ingredientType);
    }

    public void deleteById(Long id) {
        if (!ingredientTypeRepository.existsById(id)) {
            throw new EntityNotFoundException("Ingredient type with id: " + id + " not found");
        }
        ingredientTypeRepository.deleteById(id);
    }

    public void deleteByName(String name) {
        String normalizedName = normalizeName(name);

        int deleted = ingredientTypeRepository.deleteAllByNameIgnoreCase(normalizedName);

        if (deleted == 0) {
            throw new EntityNotFoundException(
                    "Ingredient type with name [" + normalizedName + "] not found"
            );
        }
    }

    private String normalizeName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Ingredient type name cannot be null");
        }
        return name.trim().toUpperCase();
    }
}
