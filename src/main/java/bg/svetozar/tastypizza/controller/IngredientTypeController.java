package bg.svetozar.tastypizza.controller;


import bg.svetozar.tastypizza.model.dto.ingredientType.IngredientTypeDto;
import bg.svetozar.tastypizza.model.dto.ingredientType.IngredientTypeResponse;
import bg.svetozar.tastypizza.model.mapper.IngredientTypeMapper;
import bg.svetozar.tastypizza.service.IngredientTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingredient-type")
@RequiredArgsConstructor
public class IngredientTypeController {

    private final IngredientTypeService ingredientTypeService;
    private final IngredientTypeMapper ingredientTypeMapper;

    @GetMapping
    public List<IngredientTypeResponse> getAll() {
        return ingredientTypeService.findAll()
                .stream()
                .map(ingredientTypeMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public IngredientTypeResponse getById(@PathVariable Long id) {
        return ingredientTypeMapper.toResponse(ingredientTypeService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public IngredientTypeResponse save(@RequestBody IngredientTypeDto dto) {
        return ingredientTypeMapper.toResponse(
                ingredientTypeService.create(dto.name())
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public IngredientTypeResponse update(@PathVariable Long id, @RequestBody IngredientTypeDto dto) {
        return ingredientTypeMapper.toResponse(
                ingredientTypeService.update(id, dto.name())
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteById(@PathVariable Long id) {
        ingredientTypeService.deleteById(id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteByName(@RequestBody IngredientTypeDto dto) {
        ingredientTypeService.deleteByName(dto.name());
    }
}
