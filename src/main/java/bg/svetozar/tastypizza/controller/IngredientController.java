package bg.svetozar.tastypizza.web;

import bg.svetozar.tastypizza.model.dto.ingredient.IngredientDto;
import bg.svetozar.tastypizza.model.dto.ingredient.IngredientResponse;
import bg.svetozar.tastypizza.model.dto.ingredient.IngredientWithTypeResponse;
import bg.svetozar.tastypizza.service.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;

    // /ingredients?show=active|all|deleted
    @GetMapping
    public ResponseEntity<List<IngredientResponse>> getAllBasic(
            @RequestParam(name = "show", required = false) String show
    ) {
        List<IngredientResponse> result = ingredientService.findAllBasic(show);
        return ResponseEntity.ok(result);
    }

    // /ingredients/with-type?show=...
    @GetMapping("/with-type")
    public ResponseEntity<List<IngredientWithTypeResponse>> getAllWithType(
            @RequestParam(name = "show", required = false) String show
    ) {
        List<IngredientWithTypeResponse> result = ingredientService.findAllWithType(show);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IngredientWithTypeResponse> getOne(@PathVariable Long id) {
        IngredientWithTypeResponse response = ingredientService.findOne(id);
        return ResponseEntity.ok(response);
    }


    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<IngredientWithTypeResponse> create(@RequestBody IngredientDto dto) {
        IngredientWithTypeResponse response = ingredientService.create(dto);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<IngredientWithTypeResponse> update(
            @PathVariable Long id,
            @RequestBody IngredientDto dto
    ) {
        IngredientWithTypeResponse response = ingredientService.update(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        ingredientService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> restore(@PathVariable Long id) {
        ingredientService.restore(id);
        return ResponseEntity.noContent().build();
    }
}
