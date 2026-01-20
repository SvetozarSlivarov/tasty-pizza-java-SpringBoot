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

import static bg.svetozar.tastypizza.exception.ErrorMessage.INGREDIENT_NOT_FOUND;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INGREDIENT_NOT_FOUND_OR_DELETE;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INGREDIENT_TYPE_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
public class IngredientService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final IngredientRepository ingredientRepository;
    private final IngredientTypeRepository ingredientTypeRepository;
    private final IngredientMapper ingredientMapper;

    public List<IngredientDto> findAllBasic(String show) {
        return ingredientMapper.toResponseList(resolveIngredients(show, false));
    }

    public List<IngredientWithTypeDto> findAllWithType(String show) {
        return ingredientMapper.toWithTypeResponseList(resolveIngredients(show, true));
    }

    public IngredientWithTypeDto findOne(Long id) {
        Ingredient ingredient = isAdmin()
                ? ingredientRepository.findByIdWithType(id).orElseThrow(() ->
                new NotFoundException(INGREDIENT_NOT_FOUND + id, ErrorCode.INGREDIENT_NOT_FOUND))
                : ingredientRepository.findByIdAndDeletedFalseWithType(id).orElseThrow(() ->
                new NotFoundException(INGREDIENT_NOT_FOUND_OR_DELETE + id, ErrorCode.INGREDIENT_NOT_FOUND));

        return ingredientMapper.toWithTypeResponse(ingredient);
    }

    public IngredientWithTypeDto create(IngredientRequest dto) {
        IngredientType type = requireIngredientType(dto.typeId());

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
        Ingredient ingredient = requireIngredient(id);
        IngredientType type = requireIngredientType(dto.typeId());

        ingredient.setName(dto.name());
        ingredient.setType(type);

        return ingredientMapper.toWithTypeResponse(ingredient);
    }

    public void softDelete(Long id) {
        Ingredient ingredient = requireIngredient(id);
        ingredient.setDeleted(true);
        ingredient.setDeletedAt(LocalDateTime.now());
    }

    public void restore(Long id) {
        Ingredient ingredient = requireIngredient(id);
        ingredient.setDeleted(false);
        ingredient.setDeletedAt(null);
    }

    private List<Ingredient> resolveIngredients(String show, boolean withType) {
        boolean admin = isAdmin();

        if (!admin) {
            return withType
                    ? ingredientRepository.findAllActiveWithType()
                    : ingredientRepository.findAllByDeletedFalse();
        }

        String normalized = normalizeShow(show);

        return switch (normalized) {
            case "active" -> withType
                    ? ingredientRepository.findAllActiveWithType()
                    : ingredientRepository.findAllByDeletedFalse();
            case "all" -> withType
                    ? ingredientRepository.findAllWithType()
                    : ingredientRepository.findAll();
            case "deleted" -> withType
                    ? ingredientRepository.findAllDeletedWithType()
                    : ingredientRepository.findAllByDeletedTrue();
            default -> throw new IllegalArgumentException("Invalid show filter: " + show);
        };
    }

    private String normalizeShow(String show) {
        if (show == null || show.isBlank()) {
            return "active";
        }
        return show.trim().toLowerCase();
    }

    private Ingredient requireIngredient(Long id) {
        return ingredientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        INGREDIENT_NOT_FOUND + id,
                        ErrorCode.INGREDIENT_NOT_FOUND
                ));
    }

    private IngredientType requireIngredientType(Long typeId) {
        return ingredientTypeRepository.findById(typeId)
                .orElseThrow(() -> new NotFoundException(
                        INGREDIENT_TYPE_NOT_FOUND + typeId,
                        ErrorCode.INGREDIENT_TYPE_NOT_FOUND
                ));
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> ROLE_ADMIN.equals(a.getAuthority()));
    }
}
