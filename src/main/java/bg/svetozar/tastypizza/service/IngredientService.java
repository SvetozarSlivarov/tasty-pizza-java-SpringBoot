package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.exception.ErrorCode;
import bg.svetozar.tastypizza.exception.NotFoundException;
import bg.svetozar.tastypizza.model.dto.ingredient.IngredientDto;
import bg.svetozar.tastypizza.model.dto.ingredient.IngredientRequest;
import bg.svetozar.tastypizza.model.dto.ingredient.IngredientWithTypeDto;
import bg.svetozar.tastypizza.model.entity.Ingredient;
import bg.svetozar.tastypizza.model.entity.IngredientType;
import bg.svetozar.tastypizza.model.mapper.IngredientMapper;
import bg.svetozar.tastypizza.repository.IngredientRepository;
import bg.svetozar.tastypizza.repository.IngredientTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final IngredientTypeRepository ingredientTypeRepository;
    private final IngredientMapper ingredientMapper;

    public List<IngredientDto> findAllBasic(String show) {
        List<Ingredient> ingredients = resolveListByShow(show);
        return ingredientMapper.toResponseList(ingredients);
    }

    public List<IngredientWithTypeDto> findAllWithType(String show) {
        List<Ingredient> ingredients = resolveListByShowWithType(show);
        return ingredientMapper.toWithTypeResponseList(ingredients);
    }

    // ----------------------------------------------------------------
    // LIST RESOLUTION
    // ----------------------------------------------------------------

    private List<Ingredient> resolveListByShow(String show) {
        boolean admin = isAdmin();

        if (!admin) {
            return ingredientRepository.findAllByDeletedFalse();
        }

        if (show == null || show.isBlank() || show.equalsIgnoreCase("active")) {
            return ingredientRepository.findAllByDeletedFalse();
        }

        if (show.equalsIgnoreCase("all")) {
            return ingredientRepository.findAll();
        }

        if (show.equalsIgnoreCase("deleted")) {
            return ingredientRepository.findAllByDeletedTrue();
        }

        throw new IllegalArgumentException("Invalid show filter: " + show);
    }

    private List<Ingredient> resolveListByShowWithType(String show) {
        boolean admin = isAdmin();

        if (!admin) {
            return ingredientRepository.findAllActiveWithType();
        }

        if (show == null || show.isBlank() || show.equalsIgnoreCase("active")) {
            return ingredientRepository.findAllActiveWithType();
        }

        if (show.equalsIgnoreCase("all")) {
            return ingredientRepository.findAllWithType();
        }

        if (show.equalsIgnoreCase("deleted")) {
            return ingredientRepository.findAllDeletedWithType();
        }

        throw new IllegalArgumentException("Invalid show filter: " + show);
    }



    public IngredientWithTypeDto findOne(Long id) {
        Ingredient ingredient;

        if (isAdmin()) {
            ingredient = ingredientRepository.findByIdWithType(id)
                    .orElseThrow(() -> new NotFoundException(
                            "Ingredient not found: " + id,
                            ErrorCode.INGREDIENT_NOT_FOUND
                    ));
        } else {
            ingredient = ingredientRepository.findByIdAndDeletedFalseWithType(id)
                    .orElseThrow(() -> new NotFoundException(
                            "Ingredient not found or deleted: " + id,
                            ErrorCode.INGREDIENT_NOT_FOUND
                    ));
        }

        return ingredientMapper.toWithTypeResponse(ingredient);
    }


    public IngredientWithTypeDto create(IngredientRequest dto) {
        IngredientType type = ingredientTypeRepository.findById(dto.typeId())
                .orElseThrow(() -> new NotFoundException(
                        "Ingredient type not found: " + dto.typeId(),
                        ErrorCode.INGREDIENT_TYPE_NOT_FOUND
                ));

        Ingredient ingredient = Ingredient.builder()
                .name(dto.name())
                .type(type)
                .deleted(false)
                .deletedAt(null)
                .build();

        ingredient = ingredientRepository.save(ingredient);
        return ingredientMapper.toWithTypeResponse(ingredient);
    }

    public IngredientWithTypeDto update(Long id, IngredientRequest dto) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Ingredient not found: " + id,
                        ErrorCode.INGREDIENT_NOT_FOUND
                ));

        IngredientType type = ingredientTypeRepository.findById(dto.typeId())
                .orElseThrow(() -> new NotFoundException(
                        "Ingredient type not found: " + dto.typeId(),
                        ErrorCode.INGREDIENT_TYPE_NOT_FOUND
                ));

        ingredient.setName(dto.name());
        ingredient.setType(type);

        return ingredientMapper.toWithTypeResponse(ingredient);
    }

    public void softDelete(Long id) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Ingredient not found: " + id,
                        ErrorCode.INGREDIENT_NOT_FOUND
                ));

        ingredient.setDeleted(true);
        ingredient.setDeletedAt(LocalDateTime.now());
    }

    public void restore(Long id) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Ingredient not found: " + id,
                        ErrorCode.INGREDIENT_NOT_FOUND
                ));

        ingredient.setDeleted(false);
        ingredient.setDeletedAt(null);
    }


    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
