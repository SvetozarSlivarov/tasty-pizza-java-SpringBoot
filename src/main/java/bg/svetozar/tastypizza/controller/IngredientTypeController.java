package bg.svetozar.tastypizza.controller;

import bg.svetozar.tastypizza.model.dto.ingredientType.IngredientTypeRequest;
import bg.svetozar.tastypizza.model.dto.ingredientType.IngredientTypeDto;
import bg.svetozar.tastypizza.model.mapper.IngredientTypeMapper;
import bg.svetozar.tastypizza.service.IngredientTypeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingredient-type")
@RequiredArgsConstructor
@Validated
public class IngredientTypeController {

    private final IngredientTypeService ingredientTypeService;
    private final IngredientTypeMapper ingredientTypeMapper;

    @GetMapping
    public List<IngredientTypeDto> getAll() {
        return ingredientTypeService.findAll()
                .stream()
                .map(ingredientTypeMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public IngredientTypeDto getById(
            @PathVariable @Positive(message = "id must be positive") Long id
    ) {
        return ingredientTypeMapper.toResponse(ingredientTypeService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public IngredientTypeDto save(
            @Valid @RequestBody IngredientTypeRequest dto
    ) {
        return ingredientTypeMapper.toResponse(
                ingredientTypeService.create(dto.name())
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public IngredientTypeDto update(
            @PathVariable @Positive(message = "id must be positive") Long id,
            @Valid @RequestBody IngredientTypeRequest dto
    ) {
        return ingredientTypeMapper.toResponse(
                ingredientTypeService.update(id, dto.name())
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteById(
            @PathVariable @Positive(message = "id must be positive") Long id
    ) {
        ingredientTypeService.deleteById(id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteByName(
            @Valid @RequestBody IngredientTypeRequest dto
    ) {
        ingredientTypeService.deleteByName(dto.name());
    }
}
