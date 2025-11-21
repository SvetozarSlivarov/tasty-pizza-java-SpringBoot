package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.model.dto.ingredient.IngredientDto;
import bg.svetozar.tastypizza.model.dto.ingredient.IngredientResponse;
import bg.svetozar.tastypizza.model.dto.ingredient.IngredientWithTypeResponse;
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


    public List<IngredientResponse> findAllBasic(String show) {
        List<Ingredient> ingredients = resolveListByShow(show);
        return ingredientMapper.toResponseList(ingredients);
    }

    public List<IngredientWithTypeResponse> findAllWithType(String show) {
        List<Ingredient> ingredients = resolveListByShow(show);
        return ingredientMapper.toWithTypeResponseList(ingredients);
    }

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

        return ingredientRepository.findAllByDeletedFalse();
    }


    public IngredientWithTypeResponse findOne(Long id) {
        Ingredient ingredient;

        if (isAdmin()) {
            ingredient = ingredientRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Ingredient not found: " + id));
        } else {
            ingredient = ingredientRepository.findByIdAndDeletedFalse(id)
                    .orElseThrow(() -> new IllegalArgumentException("Ingredient not found or deleted: " + id));
        }

        return ingredientMapper.toWithTypeResponse(ingredient);
    }

    public IngredientWithTypeResponse create(IngredientDto dto) {
        IngredientType type = ingredientTypeRepository.findById(dto.typeId())
                .orElseThrow(() -> new IllegalArgumentException("IngredientType not found: " + dto.typeId()));

        Ingredient ingredient = Ingredient.builder()
                .name(dto.name())
                .type(type)
                .deleted(false)
                .deletedAt(null)
                .build();

        ingredient = ingredientRepository.save(ingredient);
        return ingredientMapper.toWithTypeResponse(ingredient);
    }

    public IngredientWithTypeResponse update(Long id, IngredientDto dto) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ingredient not found: " + id));

        IngredientType type = ingredientTypeRepository.findById(dto.typeId())
                .orElseThrow(() -> new IllegalArgumentException("IngredientType not found: " + dto.typeId()));

        ingredient.setName(dto.name());
        ingredient.setType(type);

        return ingredientMapper.toWithTypeResponse(ingredient);
    }

    public void softDelete(Long id) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ingredient not found: " + id));

        ingredient.setDeleted(true);
        ingredient.setDeletedAt(LocalDateTime.now());
    }

    public void restore(Long id) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ingredient not found: " + id));

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
