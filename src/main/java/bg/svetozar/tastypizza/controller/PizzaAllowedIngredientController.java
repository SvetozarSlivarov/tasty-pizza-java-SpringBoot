package bg.svetozar.tastypizza.controller;

import bg.svetozar.tastypizza.model.dto.pizzaAllowedIngredient.PizzaAllowedIngredientRequest;
import bg.svetozar.tastypizza.model.dto.pizzaAllowedIngredient.PizzaAllowedIngredientDto;
import bg.svetozar.tastypizza.service.PizzaAllowedIngredientService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pizzas/{pizzaId}/allowed-ingredients")
@RequiredArgsConstructor
@Validated
public class PizzaAllowedIngredientController {

    private final PizzaAllowedIngredientService allowedIngredientService;

    @GetMapping
    public List<PizzaAllowedIngredientDto> getByPizza(
            @PathVariable @Positive(message = "pizzaId must be positive") Long pizzaId
    ) {
        return allowedIngredientService.getByPizzaId(pizzaId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public PizzaAllowedIngredientDto add(
            @PathVariable @Positive(message = "pizzaId must be positive") Long pizzaId,
            @Valid @RequestBody PizzaAllowedIngredientRequest request
    ) {
        return allowedIngredientService.addAllowedIngredient(pizzaId, request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public PizzaAllowedIngredientDto update(
            @PathVariable @Positive(message = "pizzaId must be positive") Long pizzaId,
            @PathVariable @Positive(message = "id must be positive") Long id,
            @Valid @RequestBody PizzaAllowedIngredientRequest request
    ) {
        return allowedIngredientService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void delete(
            @PathVariable @Positive(message = "pizzaId must be positive") Long pizzaId,
            @PathVariable @Positive(message = "id must be positive") Long id
    ) {
        allowedIngredientService.delete(id);
    }
}
