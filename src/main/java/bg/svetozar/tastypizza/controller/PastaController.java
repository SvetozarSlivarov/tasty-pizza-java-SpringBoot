package bg.svetozar.tastypizza.controller;

import bg.svetozar.tastypizza.model.dto.pasta.*;
import bg.svetozar.tastypizza.service.PastaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_ID_POSITIVE;

@RestController
@RequestMapping("/api/pastas")
@RequiredArgsConstructor
@Validated
public class PastaController {

    private final PastaService pastaService;

    @GetMapping
    public List<PastaDto> getAll(
            @RequestParam(name = "withDetails", defaultValue = "false") boolean withDetails,
            @RequestParam(name = "lang", required = false) String lang
    ) {
        return pastaService.getAll(withDetails, lang);
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<PastaDto> getAllDeleted(
            @RequestParam(name = "withDetails", defaultValue = "false") boolean withDetails
    ) {
        return pastaService.getAllDeleted(withDetails);
    }

    @GetMapping("/{id}")
    public PastaDto getById(
            @PathVariable @Positive(message = INVALID_ID_POSITIVE) Long id,
            @RequestParam(name = "lang", required = false) String lang
    ) {
        return pastaService.getById(id, lang);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public PastaDto create(
            @Valid @RequestBody PastaRequest request
    ) {
        return pastaService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public PastaDto update(
            @PathVariable @Positive(message = INVALID_ID_POSITIVE) Long id,
            @Valid @RequestBody PastaRequest request
    ) {
        return pastaService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void delete(
            @PathVariable @Positive(message = INVALID_ID_POSITIVE) Long id
    ) {
        pastaService.softDelete(id);
    }

    @PostMapping("/{id}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void restore(
            @PathVariable @Positive(message = INVALID_ID_POSITIVE) Long id
    ) {
        pastaService.restoreDeletedPasta(id);
    }

    @GetMapping("/{pastaId}/sauces")
    public List<PastaSauceDto> getSauces(
            @PathVariable @Positive(message = INVALID_ID_POSITIVE) Long pastaId
    ) {
        return pastaService.getSauces(pastaId);
    }

    @PostMapping("/{pastaId}/sauces")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public PastaSauceDto addSauce(
            @PathVariable @Positive(message = INVALID_ID_POSITIVE) Long pastaId,
            @Valid @RequestBody PastaSauceRequest request
    ) {
        return pastaService.addSauce(pastaId, request);
    }

    @PutMapping("/{pastaId}/sauces/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public PastaSauceDto updateSauce(
            @PathVariable @Positive(message = INVALID_ID_POSITIVE) Long pastaId,
            @PathVariable @Positive(message = INVALID_ID_POSITIVE) Long id,
            @Valid @RequestBody PastaSauceRequest request
    ) {
        return pastaService.updateSauce(pastaId, id, request);
    }

    @DeleteMapping("/{pastaId}/sauces/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteSauce(
            @PathVariable @Positive(message = INVALID_ID_POSITIVE) Long pastaId,
            @PathVariable @Positive(message = INVALID_ID_POSITIVE) Long id
    ) {
        pastaService.deleteSauce(pastaId, id);
    }

    @GetMapping("/{pastaId}/allowed-ingredients")
    public List<PastaAllowedIngredientDto> getAllowedIngredients(
            @PathVariable @Positive(message = INVALID_ID_POSITIVE) Long pastaId
    ) {
        return pastaService.getAllowedIngredients(pastaId);
    }

    @PostMapping("/{pastaId}/allowed-ingredients")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public PastaAllowedIngredientDto addAllowedIngredient(
            @PathVariable @Positive(message = INVALID_ID_POSITIVE) Long pastaId,
            @Valid @RequestBody PastaAllowedIngredientRequest request
    ) {
        return pastaService.addAllowedIngredient(pastaId, request);
    }

    @PutMapping("/{pastaId}/allowed-ingredients/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public PastaAllowedIngredientDto updateAllowedIngredient(
            @PathVariable @Positive(message = INVALID_ID_POSITIVE) Long pastaId,
            @PathVariable @Positive(message = INVALID_ID_POSITIVE) Long id,
            @Valid @RequestBody PastaAllowedIngredientRequest request
    ) {
        return pastaService.updateAllowedIngredient(pastaId, id, request);
    }

    @DeleteMapping("/{pastaId}/allowed-ingredients/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteAllowedIngredient(
            @PathVariable @Positive(message = INVALID_ID_POSITIVE) Long pastaId,
            @PathVariable @Positive(message = INVALID_ID_POSITIVE) Long id
    ) {
        pastaService.deleteAllowedIngredient(pastaId, id);
    }
}
