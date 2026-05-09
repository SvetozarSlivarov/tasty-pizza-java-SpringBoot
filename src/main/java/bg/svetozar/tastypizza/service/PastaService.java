package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.exception.*;
import bg.svetozar.tastypizza.model.dto.pasta.*;
import bg.svetozar.tastypizza.model.entity.*;
import bg.svetozar.tastypizza.model.enums.ProductType;
import bg.svetozar.tastypizza.model.enums.SpicyLevel;
import bg.svetozar.tastypizza.model.mapper.PastaMapper;
import bg.svetozar.tastypizza.repository.IngredientRepository;
import bg.svetozar.tastypizza.repository.PastaAllowedIngredientRepository;
import bg.svetozar.tastypizza.repository.PastaRepository;
import bg.svetozar.tastypizza.repository.PastaSauceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static bg.svetozar.tastypizza.exception.ErrorMessage.*;

@Service
@RequiredArgsConstructor
@Transactional
public class PastaService {

    private final PastaRepository pastaRepository;
    private final ProductService productService;
    private final IngredientRepository ingredientRepository;
    private final PastaSauceRepository pastaSauceRepository;
    private final PastaAllowedIngredientRepository pastaAllowedIngredientRepository;

    @Transactional(readOnly = true)
    public List<PastaDto> getAll(boolean fullView) {
        List<Pasta> pastas = pastaRepository.findAllLight();

        if (fullView) {
            hydrateDetails(pastas);
            return pastas.stream().map(PastaMapper::toPastaDto).toList();
        }
        return pastas.stream().map(PastaMapper::toPastaDtoWithoutFullData).toList();
    }

    @Transactional(readOnly = true)
    public List<PastaDto> getAllDeleted(boolean fullView) {
        List<Pasta> pastas = pastaRepository.findDeletedLight();

        if (fullView) {
            hydrateDetails(pastas);
            return pastas.stream().map(PastaMapper::toPastaDto).toList();
        }
        return pastas.stream().map(PastaMapper::toPastaDtoWithoutFullData).toList();
    }

    @Transactional(readOnly = true)
    public PastaDto getById(Long id) {
        Pasta pasta = pastaRepository.findByIdFull(id)
                .orElseThrow(() -> new NotFoundException(
                        PASTA_NOT_FOUND,
                        ErrorCode.PASTA_NOT_FOUND,
                        ErrorContext.of("pastaId", id)
                ));

        hydrateDetails(List.of(pasta));
        return PastaMapper.toPastaDto(pasta);
    }

    public PastaDto create(PastaRequest request) {
        BigDecimal basePrice = parseMoney(request.basePrice(), "basePrice");

        validateIngredientLists(request.sauces(), request.allowedIngredients());

        Product product = productService.createProduct(
                request.name(),
                request.description(),
                basePrice,
                ProductType.PASTA,
                request.imageBase64()
        );

        Pasta pasta = Pasta.builder()
                .product(product)
                .build();

        pasta.setSauces(mapSaucesFromRequest(request.sauces(), pasta));
        pasta.setAllowedIngredients(mapAllowedIngredientsFromRequest(request.allowedIngredients(), pasta));

        Pasta saved = pastaRepository.save(pasta);
        return PastaMapper.toPastaDto(saved);
    }

    public PastaDto update(Long id, PastaRequest request) {
        Pasta existing = pastaRepository.findByIdFull(id)
                .orElseThrow(() -> new NotFoundException(
                        PASTA_NOT_FOUND,
                        ErrorCode.PASTA_NOT_FOUND,
                        ErrorContext.of("pastaId", id)
                ));

        BigDecimal basePrice = parseMoney(request.basePrice(), "basePrice");

        validateIngredientLists(request.sauces(), request.allowedIngredients());

        Product updatedProduct = productService.updateProduct(
                existing.getProduct().getId(),
                request.name(),
                request.description(),
                basePrice,
                ProductType.PASTA,
                request.imageBase64()
        );

        existing.setProduct(updatedProduct);
        ensureCollections(existing);

        existing.getSauces().clear();
        existing.getSauces().addAll(mapSaucesFromRequest(request.sauces(), existing));

        existing.getAllowedIngredients().clear();
        existing.getAllowedIngredients().addAll(mapAllowedIngredientsFromRequest(request.allowedIngredients(), existing));

        return PastaMapper.toPastaDto(existing);
    }

    public void softDelete(Long id) {
        Pasta pasta = pastaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        PASTA_NOT_FOUND,
                        ErrorCode.PASTA_NOT_FOUND,
                        ErrorContext.of("pastaId", id)
                ));

        if (pasta.getProduct() == null) {
            throw new NotFoundException(
                    PRODUCT_NOT_FOUND_FOR_PASTA,
                    ErrorCode.PRODUCT_NOT_FOUND,
                    ErrorContext.of("pastaId", id)
            );
        }

        if (pasta.getProduct().isDeleted()) {
            throw new ConflictException(
                    PASTA_ALREADY_DELETED,
                    ErrorCode.PASTA_ALREADY_DELETED,
                    ErrorContext.of("pastaId", id)
            );
        }

        productService.softDelete(pasta.getProduct().getId());
    }

    public void restoreDeletedPasta(Long id) {
        Pasta pasta = pastaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        PASTA_NOT_FOUND,
                        ErrorCode.PASTA_NOT_FOUND,
                        ErrorContext.of("pastaId", id)
                ));

        if (pasta.getProduct() == null || !Boolean.TRUE.equals(pasta.getProduct().isDeleted())) {
            throw new ConflictException(
                    PASTA_NOT_DELETED,
                    ErrorCode.PASTA_NOT_DELETED,
                    ErrorContext.of("pastaId", id)
            );
        }

        productService.restoreDeletedProduct(pasta.getProduct().getId());
    }

    public List<PastaSauceDto> getSauces(Long pastaId) {
        requirePasta(pastaId);
        return pastaSauceRepository.findAllByPasta_Id(pastaId).stream()
                .map(PastaMapper::toPastaSauceDto)
                .toList();
    }

    public PastaSauceDto addSauce(Long pastaId, PastaSauceRequest request) {
        Pasta pasta = requirePasta(pastaId);
        Ingredient ingredient = requireIngredient(request.ingredientId());

        if (pastaSauceRepository.existsByPasta_IdAndIngredient_Id(pastaId, request.ingredientId())) {
            throw new ConflictException(
                    PASTA_SAUCE_ALREADY_EXISTS,
                    ErrorCode.PASTA_SAUCE_ALREADY_EXISTS,
                    ErrorContext.of("pastaId", pastaId, "ingredientId", request.ingredientId())
            );
        }

        PastaSauce saved = pastaSauceRepository.save(PastaSauce.builder()
                .pasta(pasta)
                .ingredient(ingredient)
                .extraPrice(parseMoney(request.extraPrice(), "extraPrice"))
                .spicyLevel(parseEnum(SpicyLevel.class, request.spicyLevel(), "spicyLevel"))
                .build());

        return PastaMapper.toPastaSauceDto(saved);
    }

    public PastaSauceDto updateSauce(Long pastaId, Long id, PastaSauceRequest request) {
        PastaSauce entity = pastaSauceRepository.findByIdAndPasta_Id(id, pastaId)
                .orElseThrow(() -> new NotFoundException(
                        PASTA_SAUCE_NOT_FOUND_WITH_ID + id,
                        ErrorCode.PASTA_SAUCE_NOT_FOUND,
                        ErrorContext.of("pastaId", pastaId, "id", id)
                ));

        Ingredient ingredient = requireIngredient(request.ingredientId());
        Long currentIngredientId = entity.getIngredient() != null ? entity.getIngredient().getId() : null;
        boolean isDifferentIngredient = !Objects.equals(currentIngredientId, request.ingredientId());

        if (isDifferentIngredient && pastaSauceRepository.existsByPasta_IdAndIngredient_Id(pastaId, request.ingredientId())) {
            throw new ConflictException(
                    PASTA_SAUCE_ALREADY_EXISTS,
                    ErrorCode.PASTA_SAUCE_ALREADY_EXISTS,
                    ErrorContext.of("pastaId", pastaId, "ingredientId", request.ingredientId())
            );
        }

        entity.setIngredient(ingredient);
        entity.setExtraPrice(parseMoney(request.extraPrice(), "extraPrice"));
        entity.setSpicyLevel(parseEnum(SpicyLevel.class, request.spicyLevel(), "spicyLevel"));

        return PastaMapper.toPastaSauceDto(pastaSauceRepository.save(entity));
    }

    public void deleteSauce(Long pastaId, Long id) {
        PastaSauce entity = pastaSauceRepository.findByIdAndPasta_Id(id, pastaId)
                .orElseThrow(() -> new NotFoundException(
                        PASTA_SAUCE_NOT_FOUND_WITH_ID + id,
                        ErrorCode.PASTA_SAUCE_NOT_FOUND,
                        ErrorContext.of("pastaId", pastaId, "id", id)
                ));

        pastaSauceRepository.delete(entity);
    }

    public List<PastaAllowedIngredientDto> getAllowedIngredients(Long pastaId) {
        requirePasta(pastaId);
        return pastaAllowedIngredientRepository.findAllByPasta_Id(pastaId).stream()
                .map(PastaMapper::toPastaAllowedIngredientDto)
                .toList();
    }

    public PastaAllowedIngredientDto addAllowedIngredient(Long pastaId, PastaAllowedIngredientRequest request) {
        Pasta pasta = requirePasta(pastaId);
        Ingredient ingredient = requireIngredient(request.ingredientId());

        if (pastaAllowedIngredientRepository.existsByPasta_IdAndIngredient_Id(pastaId, request.ingredientId())) {
            throw new ConflictException(
                    PASTA_ALLOWED_INGREDIENT_ALREADY_EXISTS,
                    ErrorCode.PASTA_ALLOWED_INGREDIENT_ALREADY_EXISTS,
                    ErrorContext.of("pastaId", pastaId, "ingredientId", request.ingredientId())
            );
        }

        PastaAllowedIngredient saved = pastaAllowedIngredientRepository.save(PastaAllowedIngredient.builder()
                .pasta(pasta)
                .ingredient(ingredient)
                .extraPrice(parseMoney(request.extraPrice(), "extraPrice"))
                .build());

        return PastaMapper.toPastaAllowedIngredientDto(saved);
    }

    public PastaAllowedIngredientDto updateAllowedIngredient(Long pastaId, Long id, PastaAllowedIngredientRequest request) {
        PastaAllowedIngredient entity = pastaAllowedIngredientRepository.findByIdAndPasta_Id(id, pastaId)
                .orElseThrow(() -> new NotFoundException(
                        PASTA_ALLOWED_INGREDIENT_NOT_FOUND_WITH_ID + id,
                        ErrorCode.PASTA_ALLOWED_INGREDIENT_NOT_FOUND,
                        ErrorContext.of("pastaId", pastaId, "id", id)
                ));

        Ingredient ingredient = requireIngredient(request.ingredientId());
        Long currentIngredientId = entity.getIngredient() != null ? entity.getIngredient().getId() : null;
        boolean isDifferentIngredient = !Objects.equals(currentIngredientId, request.ingredientId());

        if (isDifferentIngredient && pastaAllowedIngredientRepository.existsByPasta_IdAndIngredient_Id(pastaId, request.ingredientId())) {
            throw new ConflictException(
                    PASTA_ALLOWED_INGREDIENT_ALREADY_EXISTS,
                    ErrorCode.PASTA_ALLOWED_INGREDIENT_ALREADY_EXISTS,
                    ErrorContext.of("pastaId", pastaId, "ingredientId", request.ingredientId())
            );
        }

        entity.setIngredient(ingredient);
        entity.setExtraPrice(parseMoney(request.extraPrice(), "extraPrice"));

        return PastaMapper.toPastaAllowedIngredientDto(pastaAllowedIngredientRepository.save(entity));
    }

    public void deleteAllowedIngredient(Long pastaId, Long id) {
        PastaAllowedIngredient entity = pastaAllowedIngredientRepository.findByIdAndPasta_Id(id, pastaId)
                .orElseThrow(() -> new NotFoundException(
                        PASTA_ALLOWED_INGREDIENT_NOT_FOUND_WITH_ID + id,
                        ErrorCode.PASTA_ALLOWED_INGREDIENT_NOT_FOUND,
                        ErrorContext.of("pastaId", pastaId, "id", id)
                ));

        pastaAllowedIngredientRepository.delete(entity);
    }

    private void hydrateDetails(List<Pasta> pastas) {
        pastas.forEach(pasta -> {
            pasta.setSauces(pastaSauceRepository.findAllByPastaWithIngredient(pasta));
            pasta.setAllowedIngredients(pastaAllowedIngredientRepository.findAllByPastaWithIngredient(pasta));
        });
    }

    private Pasta requirePasta(Long pastaId) {
        return pastaRepository.findById(pastaId)
                .orElseThrow(() -> new NotFoundException(
                        PASTA_NOT_FOUND_WITH_ID + pastaId,
                        ErrorCode.PASTA_NOT_FOUND,
                        ErrorContext.of("pastaId", pastaId)
                ));
    }

    private Ingredient requireIngredient(Long ingredientId) {
        return ingredientRepository.findByIdAndDeletedFalse(ingredientId)
                .orElseThrow(() -> new NotFoundException(
                        INGREDIENT_NOT_FOUND_WITH_ID + ingredientId,
                        ErrorCode.INGREDIENT_NOT_FOUND,
                        ErrorContext.of("ingredientId", ingredientId)
                ));
    }

    private void ensureCollections(Pasta pasta) {
        if (pasta.getSauces() == null) pasta.setSauces(new ArrayList<>());
        if (pasta.getAllowedIngredients() == null) pasta.setAllowedIngredients(new ArrayList<>());
    }

    private void validateIngredientLists(
            List<PastaSauceRequest> sauces,
            List<PastaAllowedIngredientRequest> allowed
    ) {
        Set<Long> sauceIds = new HashSet<>();
        if (!CollectionUtils.isEmpty(sauces)) {
            for (PastaSauceRequest request : sauces) {
                Long id = request.ingredientId();
                if (id == null) continue;
                parseMoney(request.extraPrice(), "sauces.extraPrice");
                parseEnum(SpicyLevel.class, request.spicyLevel(), "sauces.spicyLevel");

                if (!sauceIds.add(id)) {
                    throw new ConflictException(
                            PASTA_SAUCE_ALREADY_EXISTS,
                            ErrorCode.PASTA_SAUCE_ALREADY_EXISTS,
                            ErrorContext.of("ingredientId", id)
                    );
                }
            }
        }

        Set<Long> allowedIds = new HashSet<>();
        if (!CollectionUtils.isEmpty(allowed)) {
            for (PastaAllowedIngredientRequest request : allowed) {
                Long id = request.ingredientId();
                if (id == null) continue;
                parseMoney(request.extraPrice(), "allowedIngredients.extraPrice");

                if (!allowedIds.add(id)) {
                    throw new ConflictException(
                            DUPLICATE_ALLOWED_INGREDIENT,
                            ErrorCode.DUPLICATE_ALLOWED_INGREDIENT,
                            ErrorContext.of("ingredientId", id)
                    );
                }
            }
        }

        Set<Long> intersection = sauceIds.stream()
                .filter(allowedIds::contains)
                .collect(Collectors.toSet());

        if (!intersection.isEmpty()) {
            Long first = intersection.iterator().next();
            throw new ConflictException(
                    INGREDIENT_CANNOT_BOTH_ALLOWED_AND_BASE,
                    ErrorCode.INGREDIENT_IN_BASE_AND_ALLOWED,
                    ErrorContext.of("ingredientId", first, "count", intersection.size())
            );
        }
    }

    private List<PastaSauce> mapSaucesFromRequest(List<PastaSauceRequest> requests, Pasta pasta) {
        if (requests == null) return List.of();

        return requests.stream()
                .map(req -> PastaSauce.builder()
                        .pasta(pasta)
                        .ingredient(requireIngredient(req.ingredientId()))
                        .extraPrice(parseMoney(req.extraPrice(), "sauces.extraPrice"))
                        .spicyLevel(parseEnum(SpicyLevel.class, req.spicyLevel(), "sauces.spicyLevel"))
                        .build())
                .toList();
    }

    private List<PastaAllowedIngredient> mapAllowedIngredientsFromRequest(
            List<PastaAllowedIngredientRequest> requests,
            Pasta pasta
    ) {
        if (requests == null) return List.of();

        return requests.stream()
                .map(req -> PastaAllowedIngredient.builder()
                        .pasta(pasta)
                        .ingredient(requireIngredient(req.ingredientId()))
                        .extraPrice(parseMoney(req.extraPrice(), "allowedIngredients.extraPrice"))
                        .build())
                .toList();
    }

    private BigDecimal parseMoney(String value, String field) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(
                    INVALID_PRICE,
                    ErrorCode.INVALID_PRICE,
                    ErrorContext.of("field", field, "value", value)
            );
        }
        try {
            BigDecimal bd = new BigDecimal(value);
            if (bd.signum() < 0) {
                throw new BadRequestException(
                        INVALID_PRICE_MUST_BE_POSITIVE,
                        ErrorCode.INVALID_PRICE,
                        ErrorContext.of("field", field, "value", value)
                );
            }
            return bd;
        } catch (NumberFormatException ex) {
            throw new BadRequestException(
                    INVALID_PRICE_FORMAT,
                    ErrorCode.INVALID_PRICE,
                    ErrorContext.of("field", field, "value", value)
            );
        }
    }

    private <E extends Enum<E>> E parseEnum(Class<E> type, String value, String field) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(
                    INVALID_ENUM_VALUE,
                    ErrorCode.INVALID_ENUM,
                    ErrorContext.of("field", field, "value", value)
            );
        }
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(
                    INVALID_ENUM_VALUE_WITH_VALUE + value,
                    ErrorCode.INVALID_ENUM,
                    ErrorContext.of("field", field, "value", value)
            );
        }
    }
}
