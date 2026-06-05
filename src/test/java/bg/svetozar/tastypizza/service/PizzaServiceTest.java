package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.model.dto.pizza.PizzaRequest;
import bg.svetozar.tastypizza.model.dto.pizza.PizzaDto;
import bg.svetozar.tastypizza.model.entity.Pizza;
import bg.svetozar.tastypizza.model.entity.Product;
import bg.svetozar.tastypizza.model.enums.ProductType;
import bg.svetozar.tastypizza.model.enums.SpicyLevel;
import bg.svetozar.tastypizza.repository.IngredientRepository;
import bg.svetozar.tastypizza.repository.PizzaAllowedIngredientRepository;
import bg.svetozar.tastypizza.repository.PizzaIngredientRepository;
import bg.svetozar.tastypizza.repository.PizzaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PizzaServiceTest {

    @Mock
    private PizzaRepository pizzaRepository;

    @Mock
    private ProductService productService;

    @Mock
    private LocalizedTextService localizedTextService;

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private PizzaIngredientRepository pizzaIngredientRepository;

    @Mock
    private PizzaAllowedIngredientRepository pizzaAllowedIngredientRepository;

    private PizzaService pizzaService;

    @BeforeEach
    void setUp() {
        pizzaService = new PizzaService(
                pizzaRepository,
                productService,
                localizedTextService,
                ingredientRepository,
                pizzaIngredientRepository,
                pizzaAllowedIngredientRepository
        );
    }

    @Test
    void createUsesEnglishTranslationAsMasterProductValues() {
        Product product = product(10L);
        stubPizzaSave();
        stubEnglishResolution();
        when(productService.createProduct(
                eq("English Pizza"),
                eq("English Description"),
                eq(new BigDecimal("12.50")),
                eq(ProductType.PIZZA),
                eq("image-data")
        )).thenReturn(product);

        pizzaService.create(request(Map.of(
                "name", linkedMap("en", "English Pizza", "bg", "Българска пица"),
                "description", linkedMap("en", "English Description", "bg", "Българско описание")
        )));

        verify(productService).createProduct(
                "English Pizza",
                "English Description",
                new BigDecimal("12.50"),
                ProductType.PIZZA,
                "image-data"
        );
    }

    @Test
    void createSavesConfirmedTranslationsForProduct() {
        Product product = product(11L);
        stubPizzaSave();
        stubEnglishResolution();
        when(productService.createProduct(any(), any(), any(), any(), any())).thenReturn(product);

        pizzaService.create(request(Map.of(
                "name", linkedMap("en", "English Pizza", "bg", "Българска пица"),
                "description", linkedMap("en", "English Description", "bg", "Българско описание")
        )));

        verify(localizedTextService).saveTranslations(
                eq("PRODUCT"),
                eq(11L),
                any(),
                any(),
                eq(Map.of("name", "English Pizza", "description", "English Description"))
        );
    }

    @Test
    void createAllowsMissingNonEnglishTranslations() {
        Product product = product(12L);
        stubPizzaSave();
        stubEnglishResolution();
        when(productService.createProduct(any(), any(), any(), any(), any())).thenReturn(product);

        pizzaService.create(request(Map.of(
                "name", linkedMap("en", "English Pizza"),
                "description", linkedMap("en", "English Description")
        )));

        verify(localizedTextService).saveTranslations(
                eq("PRODUCT"),
                eq(12L),
                any(),
                any(),
                eq(Map.of("name", "English Pizza", "description", "English Description"))
        );
    }

    @Test
    void getByIdUsesRequestedLanguageWhenTranslationExists() {
        Product product = product(20L);
        Pizza pizza = pizza(product);
        when(pizzaRepository.findByIdFull(20L)).thenReturn(Optional.of(pizza));
        when(pizzaIngredientRepository.findAllByPizzaWithIngredient(pizza)).thenReturn(List.of());
        when(pizzaAllowedIngredientRepository.findAllByPizzaWithIngredient(pizza)).thenReturn(List.of());
        when(localizedTextService.getTranslationOrDefault("PRODUCT", 20L, "name", "bg", "English Pizza"))
                .thenReturn("Българска пица");
        when(localizedTextService.getTranslationOrDefault("PRODUCT", 20L, "description", "bg", "English Description"))
                .thenReturn("Българско описание");

        PizzaDto dto = pizzaService.getById(20L, "bg");

        assertEquals("Българска пица", dto.name());
        assertEquals("Българско описание", dto.description());
    }

    @Test
    void getByIdFallsBackToEnglishWhenTranslationIsMissing() {
        Product product = product(21L);
        Pizza pizza = pizza(product);
        when(pizzaRepository.findByIdFull(21L)).thenReturn(Optional.of(pizza));
        when(pizzaIngredientRepository.findAllByPizzaWithIngredient(pizza)).thenReturn(List.of());
        when(pizzaAllowedIngredientRepository.findAllByPizzaWithIngredient(pizza)).thenReturn(List.of());
        when(localizedTextService.getTranslationOrDefault("PRODUCT", 21L, "name", "de", "English Pizza"))
                .thenReturn("English Pizza");
        when(localizedTextService.getTranslationOrDefault("PRODUCT", 21L, "description", "de", "English Description"))
                .thenReturn("English Description");

        PizzaDto dto = pizzaService.getById(21L, "de");

        assertEquals("English Pizza", dto.name());
        assertEquals("English Description", dto.description());
    }

    @Test
    void getByIdUsesEnglishMasterValuesWhenLangIsEnglish() {
        Product product = product(22L);
        Pizza pizza = pizza(product);
        when(pizzaRepository.findByIdFull(22L)).thenReturn(Optional.of(pizza));
        when(pizzaIngredientRepository.findAllByPizzaWithIngredient(pizza)).thenReturn(List.of());
        when(pizzaAllowedIngredientRepository.findAllByPizzaWithIngredient(pizza)).thenReturn(List.of());
        when(localizedTextService.getTranslationOrDefault("PRODUCT", 22L, "name", "en", "English Pizza"))
                .thenReturn("English Pizza");
        when(localizedTextService.getTranslationOrDefault("PRODUCT", 22L, "description", "en", "English Description"))
                .thenReturn("English Description");

        PizzaDto dto = pizzaService.getById(22L, "en");

        assertEquals("English Pizza", dto.name());
        assertEquals("English Description", dto.description());
        verify(localizedTextService).getTranslationOrDefault("PRODUCT", 22L, "name", "en", "English Pizza");
        verify(localizedTextService).getTranslationOrDefault("PRODUCT", 22L, "description", "en", "English Description");
    }

    private PizzaRequest request(Map<String, Map<String, String>> translations) {
        return new PizzaRequest(
                "",
                "",
                "12.50",
                "image-data",
                "MILD",
                List.of(),
                List.of(),
                List.of(),
                translations,
                null
        );
    }

    private void stubPizzaSave() {
        when(pizzaRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void stubEnglishResolution() {
        when(localizedTextService.resolveEnglishField(any(), any(), eq("name"), any())).thenReturn("English Pizza");
        when(localizedTextService.resolveEnglishField(any(), any(), eq("description"), any())).thenReturn("English Description");
    }

    private Product product(Long id) {
        return Product.builder()
                .id(id)
                .name("English Pizza")
                .description("English Description")
                .basePrice(new BigDecimal("12.50"))
                .type(ProductType.PIZZA)
                .imageUrl("image-url")
                .build();
    }

    private Pizza pizza(Product product) {
        return Pizza.builder()
                .product(product)
                .spicyLevel(SpicyLevel.MILD)
                .variants(List.of())
                .ingredients(List.of())
                .allowedIngredients(List.of())
                .build();
    }

    private Map<String, String> linkedMap(String key, String value) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put(key, value);
        return map;
    }

    private Map<String, String> linkedMap(String key1, String value1, String key2, String value2) {
        Map<String, String> map = linkedMap(key1, value1);
        map.put(key2, value2);
        return map;
    }
}
