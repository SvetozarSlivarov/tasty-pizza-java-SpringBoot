package bg.svetozar.tastypizza.controller;

import bg.svetozar.tastypizza.model.dto.ingredientType.IngredientTypeDto;
import bg.svetozar.tastypizza.model.dto.ingredientType.IngredientTypeRequest;
import bg.svetozar.tastypizza.model.mapper.IngredientTypeMapper;
import bg.svetozar.tastypizza.service.IngredientTypeService;
import bg.svetozar.tastypizza.service.LocalizedTextService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_ID_POSITIVE;

import java.util.List;

@RestController
@RequestMapping("/api/ingredient-type")
@RequiredArgsConstructor
@Validated
public class IngredientTypeController {

    private final IngredientTypeService ingredientTypeService;
    private final IngredientTypeMapper ingredientTypeMapper;
    private final LocalizedTextService localizedTextService;

    @GetMapping
    public List<IngredientTypeDto> getAll(@RequestParam(name = "lang", required = false) String lang) {
        return ingredientTypeService.findAll().stream()
                .map(type -> ingredientTypeMapper.toResponse(
                        type,
                        localizedTextService.getTranslationOrDefault("INGREDIENT_TYPE", type.getId(), "name", lang, type.getName())
                ))
                .toList();
    }

    @GetMapping("/{id}")
    public IngredientTypeDto getById(
            @PathVariable @Positive(message = INVALID_ID_POSITIVE) Long id,
            @RequestParam(name = "lang", required = false) String lang
    ) {
        var type = ingredientTypeService.findById(id);
        return ingredientTypeMapper.toResponse(
                type,
                localizedTextService.getTranslationOrDefault("INGREDIENT_TYPE", type.getId(), "name", lang, type.getName())
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public IngredientTypeDto save(@Valid @RequestBody IngredientTypeRequest dto) {
        return ingredientTypeMapper.toResponse(ingredientTypeService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public IngredientTypeDto update(
            @PathVariable @Positive(message = INVALID_ID_POSITIVE) Long id,
            @Valid @RequestBody IngredientTypeRequest dto
    ) {
        return ingredientTypeMapper.toResponse(ingredientTypeService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteById(@PathVariable @Positive(message = INVALID_ID_POSITIVE) Long id) {
        ingredientTypeService.deleteById(id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteByName(@Valid @RequestBody IngredientTypeRequest dto) {
        ingredientTypeService.deleteByName(dto.name());
    }
}
