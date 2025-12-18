package bg.svetozar.tastypizza.controller;


import bg.svetozar.tastypizza.model.dto.drink.DrinkDto;
import bg.svetozar.tastypizza.model.dto.drink.DrinkRequest;
import bg.svetozar.tastypizza.service.DrinkService;
import jakarta.validation.Valid;
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

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<DrinkDto> getAllDeleted() {
        return drinkService.getAllDeleted();
    }

    @GetMapping("/{id}")
    public DrinkDto getById(@PathVariable Long id) {
        return drinkService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public DrinkDto create(@Valid @RequestBody DrinkRequest request) {
        return drinkService.create(request);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public DrinkDto update(@PathVariable Long id, @Valid @RequestBody DrinkRequest request) {
        return drinkService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void delete(@PathVariable Long id) {
        drinkService.softDelete(id);
    }

    @PostMapping("/{id}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void restore(@PathVariable Long id) {
        drinkService.restoreDeletedDrink(id);
    }
}
