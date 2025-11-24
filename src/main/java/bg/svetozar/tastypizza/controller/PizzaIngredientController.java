package bg.svetozar.tastypizza.controller;

import bg.svetozar.tastypizza.model.dto.pizzaIngredient.PizzaIngredientRequest;
import bg.svetozar.tastypizza.model.dto.pizzaIngredient.PizzaIngredientDto;
import bg.svetozar.tastypizza.service.PizzaIngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pizzas/{pizzaId}/ingredients")
@RequiredArgsConstructor
public class PizzaIngredientController {

    private final PizzaIngredientService pizzaIngredientService;

    @GetMapping
    public List<PizzaIngredientDto> getByPizza(@PathVariable Long pizzaId) {
        return pizzaIngredientService.getByPizzaId(pizzaId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public PizzaIngredientDto add(
            @PathVariable Long pizzaId,
            @RequestBody PizzaIngredientRequest request
    ) {
        return pizzaIngredientService.addIngredientToPizza(pizzaId, request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public PizzaIngredientDto update(
            @PathVariable Long pizzaId,
            @PathVariable Long id,
            @RequestBody PizzaIngredientRequest request
    ) {
        return pizzaIngredientService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void delete(
            @PathVariable Long pizzaId,
            @PathVariable Long id
    ) {
        pizzaIngredientService.delete(id);
    }
}
