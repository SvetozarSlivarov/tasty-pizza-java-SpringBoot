package bg.svetozar.tastypizza.controller;


import bg.svetozar.tastypizza.model.dto.drink.DrinkDto;
import bg.svetozar.tastypizza.model.dto.drink.DrinkRequest;
import bg.svetozar.tastypizza.service.DrinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drinks")
@RequiredArgsConstructor
public class DrinkController {

    private final DrinkService drinkService;

    @GetMapping
    public List<DrinkDto> getAll() {
        return drinkService.getAll();
    }

    @GetMapping("/{id}")
    public DrinkDto getById(@PathVariable Long id) {
        return drinkService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public DrinkDto create(@RequestBody DrinkRequest request) {
        return drinkService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public DrinkDto update(@PathVariable Long id, @RequestBody DrinkRequest request) {
        return drinkService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        drinkService.softDelete(id);
    }

    @PostMapping("/{id}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restore(@PathVariable Long id) {
        drinkService.restoreDeletedDrink(id);
    }
}
