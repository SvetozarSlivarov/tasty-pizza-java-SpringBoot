package bg.svetozar.tastypizza.controller;

import bg.svetozar.tastypizza.model.dto.pizzaAllowedIngredient.PizzaAllowedIngredientRequest;
import bg.svetozar.tastypizza.model.dto.pizzaAllowedIngredient.PizzaAllowedIngredientDto;
import bg.svetozar.tastypizza.service.PizzaAllowedIngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pizzas/{pizzaId}/allowed-ingredients")
@RequiredArgsConstructor
public class PizzaAllowedIngredientController {

    private final PizzaAllowedIngredientService allowedIngredientService;

    @GetMapping
    public List<PizzaAllowedIngredientDto> getByPizza(@PathVariable Long pizzaId) {
        return allowedIngredientService.getByPizzaId(pizzaId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public PizzaAllowedIngredientDto add(
            @PathVariable Long pizzaId,
            @RequestBody PizzaAllowedIngredientRequest request
    ) {
        return allowedIngredientService.addAllowedIngredient(pizzaId, request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public PizzaAllowedIngredientDto update(
            @PathVariable Long pizzaId,
            @PathVariable Long id,
            @RequestBody PizzaAllowedIngredientRequest request
    ) {
        return allowedIngredientService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void delete(
            @PathVariable Long pizzaId,
            @PathVariable Long id
    ) {
        allowedIngredientService.delete(id);
    }
}
