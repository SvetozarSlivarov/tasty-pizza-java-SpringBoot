package bg.svetozar.tastypizza.controller;

import bg.svetozar.tastypizza.model.dto.pizza.PizzaDto;
import bg.svetozar.tastypizza.model.dto.pizza.PizzaRequest;
import bg.svetozar.tastypizza.service.PizzaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pizzas")
@RequiredArgsConstructor
@Validated
public class PizzaController {

    private final PizzaService pizzaService;

    @GetMapping
    public List<PizzaDto> getAll(
            @RequestParam(name = "withVariants", defaultValue = "false") boolean withVariants
    ) {
        return pizzaService.getAll(withVariants);
    }

    @GetMapping("/{id}")
    public PizzaDto getById(
            @PathVariable @Positive(message = "id must be positive") Long id
    ) {
        return pizzaService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public PizzaDto create(
            @Valid @RequestBody PizzaRequest request
    ) {
        return pizzaService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public PizzaDto update(
            @PathVariable @Positive(message = "id must be positive") Long id,
            @Valid @RequestBody PizzaRequest request
    ) {
        return pizzaService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void delete(
            @PathVariable @Positive(message = "id must be positive") Long id
    ) {
        pizzaService.softDelete(id);
    }

    @PostMapping("/{id}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void restore(
            @PathVariable @Positive(message = "id must be positive") Long id
    ) {
        pizzaService.restoreDeletedPizza(id);
    }
}
