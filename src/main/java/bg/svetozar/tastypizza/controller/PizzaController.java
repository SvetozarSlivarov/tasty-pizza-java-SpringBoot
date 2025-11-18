package bg.svetozar.tastypizza.web;

import bg.svetozar.tastypizza.model.dto.pizza.PizzaDto;
import bg.svetozar.tastypizza.model.dto.pizza.PizzaRequest;
import bg.svetozar.tastypizza.service.PizzaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pizzas")
@RequiredArgsConstructor
public class PizzaController {

    private final PizzaService pizzaService;

    @GetMapping
    public List<PizzaDto> getAll(
            @RequestParam(name = "withVariants", defaultValue = "false") boolean withVariants
    ) {
        return pizzaService.getAll(withVariants);
    }

    @GetMapping("/{id}")
    public PizzaDto getById(@PathVariable Long id) {
        return pizzaService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public PizzaDto create(@RequestBody PizzaRequest request) {
        return pizzaService.create(request);
    }

    @PutMapping("/{id}")
    public PizzaDto update(@PathVariable Long id,
                           @RequestBody PizzaRequest request) {
        return pizzaService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        pizzaService.softDelete(id);
    }
    @PostMapping("/{id}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restore(@PathVariable Long id) {
        pizzaService.restoreDeletedPizza(id);
    }
}
