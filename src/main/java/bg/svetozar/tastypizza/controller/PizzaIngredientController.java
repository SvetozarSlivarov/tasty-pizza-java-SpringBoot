package bg.svetozar.tastypizza.controller;

import bg.svetozar.tastypizza.model.dto.pizzaIngredient.PizzaIngredientRequest;
import bg.svetozar.tastypizza.model.dto.pizzaIngredient.PizzaIngredientDto;
import bg.svetozar.tastypizza.service.PizzaIngredientService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pizzas/{pizzaId}/ingredients")
@RequiredArgsConstructor
@Validated
public class PizzaIngredientController {

    private final PizzaIngredientService pizzaIngredientService;

    @GetMapping
    public List<PizzaIngredientDto> getByPizza(
            @PathVariable @Positive(message = "pizzaId must be positive") Long pizzaId
    ) {
        return pizzaIngredientService.getByPizzaId(pizzaId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public PizzaIngredientDto add(
            @PathVariable @Positive(message = "pizzaId must be positive") Long pizzaId,
            @Valid @RequestBody PizzaIngredientRequest request
    ) {
        return pizzaIngredientService.addIngredientToPizza(pizzaId, request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public PizzaIngredientDto update(
            @PathVariable @Positive(message = "pizzaId must be positive") Long pizzaId,
            @PathVariable @Positive(message = "id must be positive") Long id,
            @Valid @RequestBody PizzaIngredientRequest request
    ) {
        return pizzaIngredientService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void delete(
            @PathVariable @Positive(message = "pizzaId must be positive") Long pizzaId,
            @PathVariable @Positive(message = "id must be positive") Long id
    ) {
        pizzaIngredientService.delete(id);
    }
}
