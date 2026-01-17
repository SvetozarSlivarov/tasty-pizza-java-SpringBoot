package bg.svetozar.tastypizza.controller;

import bg.svetozar.tastypizza.model.dto.ingredient.IngredientRequest;
import bg.svetozar.tastypizza.model.dto.ingredient.IngredientDto;
import bg.svetozar.tastypizza.model.dto.ingredient.IngredientWithTypeDto;
import bg.svetozar.tastypizza.service.IngredientService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_ID_POSITIVE;

import java.util.List;

@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
@Validated
public class IngredientController {

    private final IngredientService ingredientService;

    // /ingredients?show=active|all|deleted
    @GetMapping
    public ResponseEntity<List<IngredientDto>> getAllBasic(
            @RequestParam(name = "show", required = false)
            @Pattern(
                    regexp = "active|all|deleted",
                    message = "show must be one of: active, all, deleted"
            )
            String show
    ) {
        List<IngredientDto> result = ingredientService.findAllBasic(show);
        return ResponseEntity.ok(result);
    }

    // /ingredients/with-type?show=active|all|deleted
    @GetMapping("/with-type")
    public ResponseEntity<List<IngredientWithTypeDto>> getAllWithType(
            @RequestParam(name = "show", required = false)
            @Pattern(
                    regexp = "active|all|deleted",
                    message = "show must be one of: active, all, deleted"
            )
            String show
    ) {
        List<IngredientWithTypeDto> result = ingredientService.findAllWithType(show);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IngredientWithTypeDto> getOne(
            @PathVariable @Positive(message = INVALID_ID_POSITIVE) Long id
    ) {
        IngredientWithTypeDto response = ingredientService.findOne(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<IngredientWithTypeDto> create(
            @Valid @RequestBody IngredientRequest dto
    ) {
        IngredientWithTypeDto response = ingredientService.create(dto);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<IngredientWithTypeDto> update(
            @PathVariable @Positive(message = INVALID_ID_POSITIVE) Long id,
            @Valid @RequestBody IngredientRequest dto
    ) {
        IngredientWithTypeDto response = ingredientService.update(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> softDelete(
            @PathVariable @Positive(message = INVALID_ID_POSITIVE) Long id
    ) {
        ingredientService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> restore(
            @PathVariable @Positive(message = INVALID_ID_POSITIVE) Long id
    ) {
        ingredientService.restore(id);
        return ResponseEntity.noContent().build();
    }
}
